package dev.mahdisml.webimmortalguards.ui

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mahdisml.webimmortalguards.data.SettingsRepository
import dev.mahdisml.webimmortalguards.net.VpnController
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    var pageState :Int? by mutableStateOf(null) //0=greeting,1=home,2=settings
    private var checkPageStateJob: Job? = null
    private var greetingsDoneJob: Job? = null
    private var vpnController : VpnController? = null

    var vpnState :Boolean by mutableStateOf(false)
            private set

    fun setVpnState(ctx:Context){
        val prepared = android.net.VpnService.prepare(ctx)
        if (prepared == null) {
            if (vpnController == null) {
                vpnController = VpnController(ctx)
            }
            vpnState = if (vpnState) {
                vpnController?.endVpn()
                false
            } else {
                vpnController?.startVpn()
                true
            }
        }else{
            (ctx as Activity).startActivityForResult(prepared,0x0F)
        }
    }
    fun checkVpnState(ctx:Context){
        viewModelScope.launch {
            if(vpnController == null){
                vpnController = VpnController(ctx)
            }
            vpnController?.let {
                vpnState = it.isOn()
            }
        }
    }

    fun checkPageState(ctx:Context){
        checkPageStateJob?.cancel()
        checkPageStateJob = viewModelScope.launch{
            pageState = if (SettingsRepository(ctx).getGreetingsState()){
                1
            }else{
                0
            }

        }
    }
    fun greetingsDone(ctx:Context){
        greetingsDoneJob?.cancel()
        greetingsDoneJob = viewModelScope.launch{
            SettingsRepository(ctx).setGreetingsState(true)
            checkPageState(ctx)
        }
    }
}