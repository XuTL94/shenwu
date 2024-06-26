package com.xtl.ebusiness


import androidx.compose.ui.window.*
import com.game.play.AutomationApplication
import org.springframework.boot.SpringApplication
import ui.theme.AppTheme

fun main() = application {
    // 启动 Spring Boot 应用程序
    SpringApplication.run(AutomationApplication::class.java)


    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(),
        undecorated = true
    ) {
        AppTheme {


            // 主界面内容
            ToastUtils.ToastMessage()
            LoadingUtils.LoadingDialog()

        }
    }
}

