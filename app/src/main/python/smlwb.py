import dns.message
import dns.rdatatype
import requests
import base64
import socket
import threading
import time
import random

listen_PORT = 4525

num_fragment = 300
fragment_sleep = 0.001

log_every_N_sec = 30

is_logging = False

DNS_url = 'https://sky.rethinkdns.com/?dns='

offline_DNS = {
    'sky.rethinkdns.com': '172.67.162.27'
}

my_socket_timeout = 21
first_time_sleep = 0.1
accept_time_sleep = 0.01

DNS_cache = {}
IP_DL_traffic = {}
IP_UL_traffic = {}


class DnsOverFragment:
    def __init__(self):
        self.url = DNS_url
        self.req = requests.session()
        self.fragment_proxy = {
            'https': 'http://127.0.0.1:' + str(listen_PORT)
        }

    def query(self, server_name):

        offline_ip = offline_DNS.get(server_name, None)
        if offline_ip is not None:
            if is_logging:
                print('offline DNS -->', server_name, offline_ip)
            return offline_ip

        cache_ip = DNS_cache.get(server_name, None)
        if cache_ip is not None:
            if is_logging:
                print('cached DNS -->', server_name, cache_ip)
            return cache_ip

        query_params = {
            'type': 'A',
            'ct': 'application/dns-message',
        }
        if is_logging:
            print(f'online DNS Query', server_name)
        try:
            query_message = dns.message.make_query(server_name, 'A')
            query_wire = query_message.to_wire()
            query_base64 = base64.urlsafe_b64encode(query_wire).decode('utf-8')
            query_base64 = query_base64.replace('=', '')

            query_url = self.url + query_base64
            ans = self.req.get(query_url, params=query_params, headers={'accept': 'application/dns-message'},
                               proxies=self.fragment_proxy)

            if ans.status_code == 200 and ans.headers.get('content-type') == 'application/dns-message':
                answer_msg = dns.message.from_wire(ans.content)

                resolved_ip = None
                for x in answer_msg.answer:
                    if x.rdtype == dns.rdatatype.A:
                        resolved_ip = x[0].address
                        DNS_cache[server_name] = resolved_ip
                        if is_logging:
                            print("################# DNS Cache is : ####################")
                            print(DNS_cache)
                            # later.
                            print("#####################################################")
                        break
                if is_logging:
                    print(f'online DNS --> Resolved {server_name} to {resolved_ip}')
                return resolved_ip
            else:
                if is_logging:
                    print(f'Error: {ans.status_code} {ans.reason}')
        except Exception as e:
            if is_logging:
                print(repr(e))


def my_downstream(backend_sock, client_sock):
    this_ip = backend_sock.getpeername()[0]
    if this_ip not in IP_DL_traffic:
        IP_DL_traffic[this_ip] = 0

    first_flag = True
    while True:
        try:
            if first_flag:
                first_flag = False
                data = backend_sock.recv(16384)
                if data:
                    client_sock.sendall(data)
                    IP_DL_traffic[this_ip] = IP_DL_traffic[this_ip] + len(data)
                else:
                    raise Exception('backend pipe close at first')

            else:
                data = backend_sock.recv(16384)
                if data:
                    client_sock.sendall(data)
                    IP_DL_traffic[this_ip] = IP_DL_traffic[this_ip] + len(data)
                else:
                    raise Exception('backend pipe close')

        except Exception as e:
            time.sleep(2)
            backend_sock.close()
            client_sock.close()
            return False


def extract_server_name_and_port(data):
    host_and_port = str(data).split()[1]
    host, port = host_and_port.split(':')
    return host, int(port)


class ThreadedServer(object):
    def __init__(self, host, port):
        self.DoH = DnsOverFragment()
        self.host = host
        self.port = port
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.sock.bind((self.host, self.port))

    def listen(self):
        self.sock.listen(
            128)

        while True:
            client_sock, client_addr = self.sock.accept()
            client_sock.settimeout(my_socket_timeout)

            time.sleep(accept_time_sleep)
            thread_up = threading.Thread(target=self.my_upstream, args=(client_sock,))
            thread_up.daemon = True
            thread_up.start()

    def handle_client_request(self, client_socket):
        data = client_socket.recv(16384)

        if data[:7] == b'CONNECT':
            server_name, server_port = extract_server_name_and_port(data)
        elif (data[:3] == b'GET') or (data[:4] == b'POST'):
            q_line = str(data).split('\r\n')
            q_url = q_line[0].split()[1]
            q_url = q_url.replace('http://', 'https://')
            if is_logging:
                print('redirect http to HTTPS', q_url)
            response_data = 'HTTP/1.1 302 Found\r\nLocation: ' + q_url + '\r\nProxy-agent: MyProxy/1.0\r\n\r\n'
            client_socket.sendall(response_data.encode())
            client_socket.close()
            return None
        else:
            if is_logging:
                print('Unknown Method', str(data[:10]))
            response_data = b'HTTP/1.1 400 Bad Request\r\nProxy-agent: MyProxy/1.0\r\n\r\n'
            client_socket.sendall(response_data)
            client_socket.close()
            return None

        if is_logging:
            print(server_name, '-->', server_port)

        try:
            server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            server_socket.settimeout(my_socket_timeout)
            server_socket.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
            try:
                socket.inet_aton(server_name)
                server_ip = server_name
            except socket.error:
                server_ip = self.DoH.query(server_name)

            server_socket.connect((server_ip, server_port))
            response_data = b'HTTP/1.1 200 Connection established\r\nProxy-agent: MyProxy/1.0\r\n\r\n'
            client_socket.sendall(response_data)
            return server_socket
        except Exception as e:
            if is_logging:
                print(repr(e))
            response_data = b'HTTP/1.1 502 Bad Gateway\r\nProxy-agent: MyProxy/1.0\r\n\r\n'
            client_socket.sendall(response_data)
            client_socket.close()
            server_socket.close()
            return None

    def my_upstream(self, client_sock):
        first_flag = True
        backend_sock = self.handle_client_request(client_sock)

        if backend_sock is None:
            client_sock.close()
            return False

        this_ip = backend_sock.getpeername()[0]
        if this_ip not in IP_UL_traffic:
            IP_UL_traffic[this_ip] = 0

        while True:
            try:
                if first_flag:
                    first_flag = False

                    time.sleep(first_time_sleep)
                    data = client_sock.recv(16384)

                    if data:
                        thread_down = threading.Thread(target=my_downstream, args=(backend_sock, client_sock))
                        thread_down.daemon = True
                        thread_down.start()
                        send_data_in_fragment(data, backend_sock)
                        IP_UL_traffic[this_ip] = IP_UL_traffic[this_ip] + len(data)

                    else:
                        raise Exception('cli syn close')

                else:
                    data = client_sock.recv(16384)
                    if data:
                        backend_sock.sendall(data)
                        IP_UL_traffic[this_ip] = IP_UL_traffic[this_ip] + len(data)
                    else:
                        raise Exception('cli pipe close')

            except Exception as e:
                time.sleep(2)
                client_sock.close()
                backend_sock.close()
                return False


def send_data_in_fragment(data, sock):
    l_data = len(data)
    indices = random.sample(range(1, l_data - 1), num_fragment - 1)
    indices.sort()

    i_pre = 0
    for i in indices:
        fragment_data = data[i_pre:i]
        i_pre = i
        sock.sendall(fragment_data)
        time.sleep(fragment_sleep)
    fragment_data = data[i_pre:l_data]
    sock.sendall(fragment_data)
    if is_logging:
        print('----------finish------------')


def main():
    print("Now listening at: 127.0.0.1:" + str(listen_PORT))
    ThreadedServer('', listen_PORT).listen()