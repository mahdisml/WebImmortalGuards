package dev.mahdisml.webimmortalguards.core

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prefs")

open class AppCore (
    private val ctx: Context,
    private val preferencesKey:String
) {
    companion object {
        @SuppressLint("UnnecessaryComposedModifier")
        fun Modifier.coloredShadow(
            color: Color,
            alpha: Float = 0.2f,
            borderRadius: Dp = 0.dp,
            shadowRadius: Dp = 20.dp,
            offsetY: Dp = 0.dp,
            offsetX: Dp = 0.dp
        ) = composed {
            val shadowColor = color.copy(alpha = alpha).toArgb()
            val transparent = color.copy(alpha= 0f).toArgb()
            this.drawBehind {
                this.drawIntoCanvas {
                    val paint = Paint()
                    val frameworkPaint = paint.asFrameworkPaint()
                    frameworkPaint.color = transparent
                    frameworkPaint.setShadowLayer(
                        shadowRadius.toPx(),
                        offsetX.toPx(),
                        offsetY.toPx(),
                        shadowColor
                    )
                    it.drawRoundRect(
                        0f,
                        0f,
                        this.size.width,
                        this.size.height,
                        borderRadius.toPx(),
                        borderRadius.toPx(),
                        paint
                    )
                }
            }
        }
        @Composable
        fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
            val eventHandler = rememberUpdatedState(onEvent)
            val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

            DisposableEffect(lifecycleOwner.value) {
                val lifecycle = lifecycleOwner.value.lifecycle
                val observer = LifecycleEventObserver { owner, event ->
                    eventHandler.value(owner, event)
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }
        }

    }

    //DataStore

    suspend fun saveIfNotExist(name: String, data: String){
        ctx.dataStore.edit { prefs ->
            if (prefs[stringPreferencesKey(name)] == null) {
                Chayi.encrypt(data,preferencesKey)?.let { it2 ->
                    prefs[stringPreferencesKey(name)] = it2
                }
            }
        }
    }
    suspend fun save(name:String, data:String){
        ctx.dataStore.edit { prefs ->
            Chayi.encrypt(data,preferencesKey)?.let { it2 ->
                prefs[stringPreferencesKey(name)] = it2
            }
        }
    }
    suspend fun load(data:String) : String?{
        return loadFlow(data).first()
    }
    suspend fun remove(name:String){
        ctx.dataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey(name))
        }
    }

    private fun loadFlow(name: String) : Flow<String?> {
        return ctx.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey(name)].let {
                if (it != null){
                    Chayi.decrypt(it,preferencesKey)
                }else {
                    null
                }
            }
        }
    }
    suspend fun clearAllData(){
        ctx.dataStore.edit {
            it.clear()
        }
    }

    // Display

}