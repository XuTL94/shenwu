package com.game.play.utils;

import com.sun.jna.platform.win32.WinDef;

import java.util.ArrayList;
import java.util.List;

public class ShenwuUtils {

    // 存储已绑定的神武窗口句柄
    public static List<String> haveHandlesBind = new ArrayList<>();


    // 获取未绑定的窗口句柄
    public static WinDef.HWND getAvailableHwnd() {
        List<WinDef.HWND> windowHandles = WindowsUtils.getWindowHandles("幻唐志 - ", null);
        for (WinDef.HWND hwnd : windowHandles) {
            String hwndId = hwnd.toString();
            if (!haveHandlesBind.contains(hwndId)) {
                haveHandlesBind.add(hwndId);
                return hwnd;
            }
        }
        return null;
    }

}
