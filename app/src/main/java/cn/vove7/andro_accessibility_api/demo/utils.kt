package cn.vove7.andro_accessibility_api.demo

import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*
import java.util.Random
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * # utils
 *
 * Created on 2020/6/11
 * @author Vove
 */


fun launchWithExpHandler(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = GlobalScope.launch(context + ExceptionHandler, start, block)


val ExceptionHandler by lazy {
    CoroutineExceptionHandler { _, throwable ->
        toast("执行失败： ${throwable.message ?: "$throwable"}")
        throwable.printStackTrace()
    }
}

val mainHandler by lazy {
    Handler(Looper.getMainLooper())
}

fun runOnUi(block: () -> Unit) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        block()
    } else {
        mainHandler.post(block)
    }
}

suspend fun delayRandom(timeMillis: Long){
    val randomTime = (-200..700).random()
    delay(timeMillis + randomTime.toLong())
}
fun toast(m: String) =
    runOnUi {
        Toast.makeText(DemoApp.INS, m, Toast.LENGTH_SHORT).show()
    }

fun stopBackgroundProcess(act:ComponentActivity,packageName:String){
    val am = act.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    am.killBackgroundProcesses(packageName)
}

fun getClosePosition(ctx:Context):String{
    val preferences = ctx.getSharedPreferences("sp_database", Context.MODE_PRIVATE)
    return preferences.getString("position","986.109") ?:""
}

fun saveClosePosition(ctx:Context,position:String){
    val preferences = ctx.getSharedPreferences("sp_database", Context.MODE_PRIVATE)
    preferences.edit().apply {
        putString("position",position)
    }.apply()
}
