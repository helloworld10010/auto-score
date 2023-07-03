package cn.vove7.andro_accessibility_api.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import cn.vove7.andro_accessibility_api.AccessibilityApi
import cn.vove7.andro_accessibility_api.demo.actions.*
import cn.vove7.andro_accessibility_api.demo.databinding.ActivityMainBinding
import cn.vove7.auto.core.AutoApi
import cn.vove7.auto.core.utils.jumpAccessibilityServiceSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val nnScoreAction = NNScoreAction()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val actions = mutableListOf(
            nnScoreAction,
//            PickScreenText(),
//            SiblingTestAction(),
//            DrawableAction(),
//            WaitAppAction(),
//            SelectTextAction(),
//            ViewFinderWithLambda(),
//            TextMatchAction(),
//            ClickTextAction(),
//            TraverseAllAction(),
//            SmartFinderAction(),
//            CoroutineStopAction(),
//            ToStringTestAction(),
//            InstrumentationSendKeyAction(),
//            InstrumentationSendTextAction(),
//            InstrumentationInjectInputEventAction(),
//            InstrumentationShotScreenAction(),
//            SendImeAction(),
//            ContinueGestureAction(),
            object : Action() {
                override val name = "Stop"
                override suspend fun run(act: ComponentActivity) {
                    actionJob?.cancel()
                }
            }
        )

        binding.listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, actions)
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val text = binding.editText.text.toString()

            try {
                if (text.contains(".")) {
                    val pair = text.split(".")
                    if (pair[0].toIntOrNull() is Int && pair[1].toIntOrNull() is Int) {
                        CLOSE_BUTTON_POSITION = Pair(pair[0].toInt(), pair[1].toInt())
                    } else {
                        throw IllegalStateException("坐标格式不正确，请填入以英文句号分割的两个整数")
                    }
                } else {
                    throw IllegalStateException("坐标格式不正确，请填入以英文句号分割的两个整数")
                }
            }catch (e:Exception){
                toast(e.message?:"")
            }

            saveClosePosition(this,text)
            onActionClick(actions[position])
        }
        binding.acsCb.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && !AccessibilityApi.isServiceEnable) {
                buttonView.isChecked = false
                jumpAccessibilityServiceSettings(AccessibilityApi.BASE_SERVICE_CLS)
            }
        }

        val dataFromLocal = getClosePosition(this)
        binding.editText.setText(dataFromLocal)
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.acsCb.isChecked = AccessibilityApi.isServiceEnable
        binding.acsCb.isEnabled = AutoApi.serviceType != AutoApi.SERVICE_TYPE_INSTRUMENTATION

        binding.workMode.text = "工作模式：${
            mapOf(
                AutoApi.SERVICE_TYPE_NONE to "无",
                AutoApi.SERVICE_TYPE_ACCESSIBILITY to "无障碍",
                AutoApi.SERVICE_TYPE_INSTRUMENTATION to "Instrumentation",
            )[AutoApi.serviceType]
        } "
    }

    var actionJob: Job? = null

    private fun onActionClick(action: Action) {
        if (action.name == "Stop") {
            actionJob?.cancel()
            return
        }
        if (actionJob?.isCompleted.let { it != null && !it }) {
            toast("有正在运行的任务")
            return
        }
        actionJob = launchWithExpHandler {
            action.run(this@MainActivity)
        }
        actionJob?.invokeOnCompletion {
            if (it is CancellationException) {
                toast("取消执行")
            } else if (it == null) {
                toast("执行结束")
            }
        }
    }

    override fun onDestroy() {
        actionJob?.cancel()
        super.onDestroy()
    }
}
