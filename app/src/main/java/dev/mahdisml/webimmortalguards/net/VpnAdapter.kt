package dev.mahdisml.webimmortalguards.net

import com.chaquo.python.PyObject
import com.chaquo.python.Python
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class VpnAdapter () {
    private var pythonJob: Job? = null
    private var smlwb: PyObject? = null

    fun start(){
        stop()
        try {
            pythonJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    smlwb = Python.getInstance().getModule("smlwb")
                    smlwb!!.callAttr("main")
                } catch (e: Exception) {
                    stop()
                }
            }
        } catch (e: Exception) {
            stop()
        }
    }
    fun stop(){
        pythonJob?.cancel("stop")
        smlwb = null
        pythonJob = null
    }

    fun isOn():Boolean{
        return (pythonJob != null) && (smlwb != null)
    }
}