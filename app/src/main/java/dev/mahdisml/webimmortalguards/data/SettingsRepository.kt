package dev.mahdisml.webimmortalguards.data

import android.content.Context
import dev.mahdisml.webimmortalguards.Core

class SettingsRepository (ctx:Context) {
    private val core = Core(ctx)

    suspend fun getGreetingsState():Boolean{
        core.load("greeting_passed").let {
            return if (it != null) {
                return (it == "true")
            } else {
                false
            }
        }
    }
    suspend fun setGreetingsState(state:Boolean){
        core.save("greeting_passed",state.toString())
    }
}