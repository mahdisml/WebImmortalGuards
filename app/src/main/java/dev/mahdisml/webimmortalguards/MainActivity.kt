package dev.mahdisml.webimmortalguards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.mahdisml.webimmortalguards.Strings.setting_1
import dev.mahdisml.webimmortalguards.Strings.setting_1_1
import dev.mahdisml.webimmortalguards.Strings.version
import dev.mahdisml.webimmortalguards.core.AppCore
import dev.mahdisml.webimmortalguards.core.AppCore.Companion.coloredShadow
import dev.mahdisml.webimmortalguards.ui.MainViewModel
import dev.mahdisml.webimmortalguards.ui.theme.Cinder
import dev.mahdisml.webimmortalguards.ui.theme.Gold
import dev.mahdisml.webimmortalguards.ui.theme.GoldDark
import dev.mahdisml.webimmortalguards.ui.theme.GoldDarker
import dev.mahdisml.webimmortalguards.ui.theme.Silver
import dev.mahdisml.webimmortalguards.ui.theme.SilverDarker
import dev.mahdisml.webimmortalguards.ui.theme.Ubuntu_Regular
import dev.mahdisml.webimmortalguards.ui.theme.WebImmortalGuardsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val ctx = LocalContext.current
            val mainViewModel :MainViewModel = viewModel()

            LaunchedEffect(true) {
                mainViewModel.checkPageState(ctx)
            }

            WebImmortalGuardsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Cinder
                ) {
                    when(mainViewModel.pageState){
                        0 -> {Greeting()}
                        1 -> {Home()}
                        2 -> {Settings()}
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalTextApi::class)
@Composable
fun Greeting() {
    val ctx = LocalContext.current
    val mainViewModel:MainViewModel = viewModel()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.greeting),
                contentDescription = Strings.greeting_image_des,
                modifier = Modifier
                    .coloredShadow(color = Gold, alpha = 0.6f)
                    .fillMaxHeight(0.45f)
                    .wrapContentSize()
                    .clip(RoundedCornerShape(33.dp))
            )
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = Strings.greeting_text,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(GoldDark, Gold,GoldDark)
                    )
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 33.dp),
                fontSize = 17.sp
            )
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    mainViewModel.greetingsDone(ctx)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinder,
                    contentColor = Silver
                ),
                border = BorderStroke(1.dp, Gold)
            ) {
                Text(
                    text = Strings.greeting_button_text,
                    textAlign = TextAlign.Center
                )
            }
        }
    }



}
@OptIn(ExperimentalTextApi::class)
@Composable
fun Home() {
    val ctx = LocalContext.current
    val mainViewModel:MainViewModel = viewModel()

    LaunchedEffect(true){
        mainViewModel.checkVpnState(ctx)
    }

    var firstResume by remember { mutableStateOf(true)}
    AppCore.OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (!firstResume) {
                    mainViewModel.checkVpnState(ctx)
                } else {
                    firstResume = false
                }
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Image(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = Strings.home_setting_des,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(all = 20.dp)
                .clickable {
                    mainViewModel.pageState = 2
                },
            colorFilter = ColorFilter.tint(GoldDarker)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 70.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = Strings.home_image_des,
                modifier = Modifier.fillMaxHeight(0.495f)
            )
            Text(
                text = Strings.app_name,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(GoldDark, Gold,GoldDark)
                    ),
                    fontFamily = Ubuntu_Regular
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 33.dp)
                    .padding(bottom = (40).dp),
                fontSize = 25.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Switch(
                modifier = Modifier
                    .scale(1.5f)
                    .coloredShadow(
                        color = Gold,
                        alpha = if (mainViewModel.vpnState) 0.4f else 0.1f
                    ),
                checked = mainViewModel.vpnState,
                onCheckedChange = {
                    mainViewModel.setVpnState(ctx)
                },
                colors = SwitchDefaults.colors(
                    uncheckedBorderColor = GoldDark,
                    checkedBorderColor = Gold,

                    uncheckedThumbColor = Gold,
                    checkedThumbColor = Cinder,

                    uncheckedTrackColor = Cinder,
                    checkedTrackColor = Gold

                )
            )
            Spacer(modifier = Modifier.height(50.dp))
            Text(
                text = if (mainViewModel.vpnState) Strings.home_text_on else Strings.home_text_off,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = if (mainViewModel.vpnState) listOf(GoldDark, Gold,GoldDark) else listOf(GoldDarker,GoldDarker)
                    )
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 33.dp),
                fontSize = 17.sp
            )
        }
    }
}
@Preview(device = "id:pixel_6_pro")
@Composable
fun Settings() {
    val mainViewModel:MainViewModel = viewModel()

    var logoState by remember { mutableStateOf(0) }

    BackHandler {
        mainViewModel.pageState = 1
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.back),
            contentDescription = Strings.setting_back,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(all = 20.dp)
                .clickable {
                    mainViewModel.pageState = 1
                },
            colorFilter = ColorFilter.tint(GoldDarker)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight(0.70f)
                .fillMaxWidth(0.80f)
                .coloredShadow(
                    color = Gold,
                    alpha = 0.1f
                )
                .clip(RoundedCornerShape(33.dp))
                .background(Cinder),
            contentAlignment = Alignment.Center
        ){
            Column(modifier = Modifier.align(Alignment.TopCenter),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
                Spacer(modifier = Modifier.height(30.dp))
                Box(modifier = Modifier.fillMaxWidth(0.85f)) {
                    Text(modifier = Modifier.align(Alignment.CenterStart),text = setting_1, color = Silver, textAlign = TextAlign.Center, fontSize = 14.sp)
                    Button(
                        onClick = {

                        },
                        enabled = false,
                        modifier=Modifier.align(Alignment.CenterEnd) ,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Cinder,
                            contentColor = Silver,
                            disabledContainerColor = Cinder,
                            disabledContentColor = SilverDarker
                        ),
                        border = BorderStroke(1.dp, Gold)
                    ) {
                        Text(
                            text = setting_1_1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Column(modifier = Modifier.align(Alignment.BottomCenter),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Image(
                    modifier= Modifier
                        .fillMaxWidth(0.25f)
                        .fillMaxHeight(0.1f)
                        .wrapContentSize()
                        .clickable {
                            logoState++
                        },
                    painter = if (logoState >= 12) painterResource(id = R.drawable.heart) else painterResource(id = R.drawable.ic_mahdisml_emblem),
                    contentDescription = Strings.setting_mahdisml
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = version, color = Silver, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

}

