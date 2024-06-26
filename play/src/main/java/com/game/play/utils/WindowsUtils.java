package com.game.play.utils;

import com.game.core.exception.BusinessException;
import com.game.core.utils.StringUtils;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * window API操作工具类
 */
@Slf4j
public class WindowsUtils {

    /**
     * 查询单个句柄（单开游戏）
     *
     * @param wText
     * @param cName
     * @return
     */
    public static WinDef.HWND getWindowHandle(String wText, String cName) {
        User32 user32 = User32.INSTANCE;
        WinDef.HWND hWnd = null;
        if (StringUtils.isNotEmpty(cName)) {
            hWnd = user32.FindWindow(cName, null);
        } else {
            hWnd = user32.FindWindow(null, wText);
        }
        if (hWnd != null) {
            char[] windowTextChars = new char[512];
            user32.GetWindowText(hWnd, windowTextChars, 512);
            String windowText = Native.toString(windowTextChars);

            char[] classNameChars = new char[512];
            user32.GetClassName(hWnd, classNameChars, 512);
            String className = Native.toString(classNameChars);

            log.info("找到句柄: {}, 窗口标题: {}, 类名: {}", hWnd, windowText, className);
        }
        return hWnd;
    }


    /**
     * 查询句柄List (多开游戏)
     *
     * @param wText
     * @param cName
     * @return
     */
    public static List<WinDef.HWND> getWindowHandles(String wText, String cName) {
        User32 user32 = User32.INSTANCE;
        List<WinDef.HWND> hwndList = new ArrayList<>();

        user32.EnumWindows((hWnd, data) -> {
            char[] windowTextChars = new char[512];
            user32.GetWindowText(hWnd, windowTextChars, 512);
            String windowText = Native.toString(windowTextChars);

            char[] classNameChars = new char[512];
            user32.GetClassName(hWnd, classNameChars, 512);
            String className = Native.toString(classNameChars);

            if (windowText.contains(wText)) {
                if ((StringUtils.isNotEmpty(cName) && !className.equals(cName))) {
                    return true;
                }
                hwndList.add(hWnd);
                log.info("句柄: {}, 窗口标题: {}, 类名: {}", hWnd, windowText, className);
            }
            return true;
        }, null);

        log.info("包含'{}'的窗口句柄集合:", wText);
        hwndList.forEach(hwnd -> log.info("{}", hwnd));

        return hwndList;
    }
}
