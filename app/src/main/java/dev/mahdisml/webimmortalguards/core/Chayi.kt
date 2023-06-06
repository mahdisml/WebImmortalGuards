package dev.mahdisml.webimmortalguards.core

object Chayi {

    // Mahdi Safarmohammadloo
    // www.MahdiSML.dev

    private const val DELTA = -0x61c88647

    fun encrypt(data: String, key: String): String? {
        val bytes = encryptToByte(data, key) ?: return null
        return Base64.encode(bytes)
    }

    fun decrypt(data: String, key: String): String? {
        return try {
            val bytes = decrypt(Base64.decode(data), key) ?: return null
            bytes.decodeToString()
        } catch (ex: Exception) {
            null
        }
    }

    private fun mx(sum: Int, y: Int, z: Int, p: Int, e: Int, k: IntArray): Int {
        return (z.ushr(5) xor (y shl 2)) + (y.ushr(3) xor (z shl 4)) xor (sum xor y) + (k[p and 3 xor e] xor z)
    }

    private fun encrypt(data: ByteArray, key: ByteArray): ByteArray =
        data.takeIf { it.isNotEmpty() }
            ?.let {
                encrypt(data.toIntArray(true), key.fixKey().toIntArray(false))
                    .toByteArray(false)
            }
            ?: data

    private fun encryptToByte(data: String, key: String): ByteArray? =
        runCatching {
            encrypt(
                data.encodeToByteArray(throwOnInvalidSequence = true),
                key.encodeToByteArray(throwOnInvalidSequence = true)
            )
        }.getOrNull()

    private fun decrypt(data: ByteArray, key: ByteArray): ByteArray =
        data.takeIf { it.isNotEmpty() }
            ?.let {
                decrypt(data.toIntArray(false), key.fixKey().toIntArray(false))
                    .toByteArray(true)
            } ?: data

    private fun decrypt(data: ByteArray, key: String): ByteArray? =
        kotlin.runCatching { decrypt(data, key.encodeToByteArray(throwOnInvalidSequence = true)) }.getOrNull()

    private fun encrypt(v: IntArray, k: IntArray): IntArray {
        val n = v.size - 1
        if (n < 1) {
            return v
        }
        var p: Int
        var q = 6 + 52 / (n + 1)
        var z = v[n]
        var y: Int
        var sum = 0
        var e: Int
        while (q-- > 0) {
            sum += DELTA
            e = sum.ushr(2) and 3
            p = 0
            while (p < n) {
                y = v[p + 1]
                v[p] += mx(sum, y, z, p, e, k)
                z = v[p]
                p++
            }
            y = v[0]
            v[n] += mx(sum, y, z, p, e, k)
            z = v[n]
        }
        return v
    }

    private fun decrypt(v: IntArray, k: IntArray): IntArray {
        val n = v.size - 1
        if (n < 1) {
            return v
        }
        var p: Int
        val q = 6 + 52 / (n + 1)
        var z: Int
        var y = v[0]
        var sum = q * DELTA
        var e: Int
        while (sum != 0) {
            e = sum.ushr(2) and 3
            p = n
            while (p > 0) {
                z = v[p - 1]
                v[p] -= mx(sum, y, z, p, e, k)
                y = v[p]
                p--
            }
            z = v[n]
            v[0] -= mx(sum, y, z, p, e, k)
            y = v[0]
            sum -= DELTA
        }
        return v
    }
    private fun ByteArray.fixKey(): ByteArray {
        if (size == 16) return this
        val fixedKey = ByteArray(16)

        if (size < 16) {
            copyInto(fixedKey)
        } else {
            copyInto(fixedKey, endIndex = 16)
        }
        return fixedKey
    }

    private fun ByteArray.toIntArray(includeLength: Boolean): IntArray {
        var n = if (size and 3 == 0)
            size.ushr(2)
        else
            size.ushr(2) + 1
        val result: IntArray

        if (includeLength) {
            result = IntArray(n + 1)
            result[n] = size
        } else {
            result = IntArray(n)
        }
        n = size
        for (i in 0 until n) {
            result[i.ushr(2)] = result[i.ushr(2)] or (0x000000ff and this[i].toInt() shl (i and 3 shl 3))
        }
        return result
    }

    private fun IntArray.toByteArray(includeLength: Boolean): ByteArray? {
        var n = size shl 2

        if (includeLength) {
            val m = this[size - 1]
            n -= 4
            if (m < n - 3 || m > n) {
                return null
            }
            n = m
        }
        val result = ByteArray(n)

        for (i in 0 until n) {
            result[i] = this[i.ushr(2)].ushr(i and 3 shl 3).toByte()
        }
        return result
    }
    private object Base64 {
        private val base64EncodeChars = charArrayOf(
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '+', '/'
        )
        private val base64DecodeChars = byteArrayOf(
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
            -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
            15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
            -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1
        )

        fun encode(data: ByteArray): String {
            val sb = StringBuilder()
            val r = data.size % 3
            val len = data.size - r
            var i = 0
            var c: Int
            while (i < len) {
                c = 0x000000ff and data[i++].toInt() shl 16 or (
                        0x000000ff and data[i++].toInt() shl 8) or
                        (0x000000ff and data[i++].toInt())
                sb.append(base64EncodeChars[c shr 18])
                sb.append(base64EncodeChars[c shr 12 and 0x3f])
                sb.append(base64EncodeChars[c shr 6 and 0x3f])
                sb.append(base64EncodeChars[c and 0x3f])
            }
            if (r == 1) {
                c = 0x000000ff and data[i].toInt()
                sb.append(base64EncodeChars[c shr 2])
                sb.append(base64EncodeChars[c and 0x03 shl 4])
                sb.append("==")
            } else if (r == 2) {
                c = 0x000000ff and data[i++].toInt() shl 8 or
                        (0x000000ff and data[i].toInt())
                sb.append(base64EncodeChars[c shr 10])
                sb.append(base64EncodeChars[c shr 4 and 0x3f])
                sb.append(base64EncodeChars[c and 0x0f shl 2])
                sb.append("=")
            }
            return sb.toString()
        }

        fun decode(str: String): ByteArray {
            val data = str.encodeToByteArray()
            val len = data.size
            val buf = mutableListOf<Byte>()
            var i = 0
            var b1: Int
            var b2: Int
            var b3: Int
            var b4: Int
            while (i < len) {

                /* b1 */do {
                    b1 = base64DecodeChars[data[i++].toInt()].toInt()
                } while (i < len && b1 == -1)
                if (b1 == -1) {
                    break
                }

                /* b2 */do {
                    b2 = base64DecodeChars[data[i++].toInt()].toInt()
                } while (i < len && b2 == -1)
                if (b2 == -1) {
                    break
                }
                buf.add((b1 shl 2 or (b2 and 0x30 ushr 4)).toByte())

                /* b3 */do {
                    b3 = data[i++].toInt()
                    if (b3 == 61) {
                        return buf.toByteArray()
                    }
                    b3 = base64DecodeChars[b3].toInt()
                } while (i < len && b3 == -1)
                if (b3 == -1) {
                    break
                }
                buf.add((b2 and 0x0f shl 4 or (b3 and 0x3c ushr 2)).toByte())

                /* b4 */do {
                    b4 = data[i++].toInt()
                    if (b4 == 61) {
                        return buf.toByteArray()
                    }
                    b4 = base64DecodeChars[b4].toInt()
                } while (i < len && b4 == -1)
                if (b4 == -1) {
                    break
                }
                buf.add((b3 and 0x03 shl 6 or b4).toByte())
            }
            return buf.toByteArray()
        }
    }
}