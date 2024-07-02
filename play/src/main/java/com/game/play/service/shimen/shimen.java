package com.game.play.service.shimen;

import com.game.play.utils.CaptureUtils;
import com.game.play.utils.WindowsUtils;
import com.sun.jna.platform.win32.WinDef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class shimen {

    /**
     * 1.打开日程 25,150坐标    点击师门  250 260
     * 2.按下ESC退出键，避免已接师门任务，跑交易中心
     * 3.识别师门类型
     */


    public static void main(String[] args) {
        List<WinDef.HWND> windowHandles = WindowsUtils.getWindowHandles("幻唐志 - 二", null);
        WinDef.HWND hwnd = windowHandles.get(0);
        // Simulate pressing the Escape key
        /*User32.INSTANCE.PostMessage(hwnd, WinUser.WM_KEYDOWN, new WinDef.WPARAM(0x1B), new WinDef.LPARAM(0)); // VK_ESCAPE = 0x1B
        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_KEYUP, new WinDef.WPARAM(0x1B), new WinDef.LPARAM(0));   // VK_ESCAPE = 0x1B*/
        CaptureUtils.captureWindow(hwnd);

    }


}