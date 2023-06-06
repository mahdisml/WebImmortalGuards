package dev.mahdisml.webimmortalguards

import android.content.Context
import dev.mahdisml.webimmortalguards.core.AppCore

class Core(private val ctx: Context): AppCore(ctx,"prefsKey") {
    companion object{}
    init {}
}