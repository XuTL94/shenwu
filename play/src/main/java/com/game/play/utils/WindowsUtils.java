package com.game.play.utils;

import com.game.core.exception.BusinessException;
import com.game.core.utils.StringUtils;
import com.game.play.enums.WindowsMessage;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * window API操作工具类
 */
@Slf4j
public class WindowsUtils {

    /**
     * 发送鼠标事件到指定窗口。
     *
     * @param hwnd      窗口句柄
     * @param x         相对X坐标
     * @param y         相对Y坐标
     * @param eventType 鼠标事件类型（MOUSE_MOVE, LBUTTON_DOWN, LBUTTON_UP）
     */
    public static void sendMouseEvent(WinDef.HWND hwnd, int x, int y, WindowsMessage eventType) {
        WinDef.LPARAM lParam = new WinDef.LPARAM((y << 16) | (x & 0xFFFF));
        User32.INSTANCE.PostMessage(hwnd, eventType.getValue(), new WinDef.WPARAM(0), lParam);
    }

    /**
     * 发送鼠标移动和点击事件到指定窗口。
     * 包括鼠标移动、左键按下和松开事件，自动添加适当的延迟。
     *
     * @param hwnd  窗口句柄
     * @param x     相对X坐标
     * @param y     相对Y坐标
     * @param times 点击次数
     */
    public static void sendMouseClick(WinDef.HWND hwnd, int x, int y, int times) {

        try {
            y = y - 30;
            for (int i = 0; i < times; i++) {
                // 移动鼠标
                sendMouseEvent(hwnd, x, y, WindowsMessage.MOUSE_MOVE);
                // 添加短暂延迟
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // 按下左键
                sendMouseEvent(hwnd, x, y, WindowsMessage.LBUTTON_DOWN);
                // 松开左键
                sendMouseEvent(hwnd, x, y, WindowsMessage.LBUTTON_UP);

                TimeUnit.MILLISECONDS.sleep(1000);

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 发送鼠标右键点击事件到指定窗口。
     * 包括鼠标移动、右键按下和松开事件，自动添加适当的延迟。
     *
     * @param hwnd  窗口句柄
     * @param x     相对X坐标
     * @param y     相对Y坐标
     * @param times 点击次数
     */
    public static void sendRightMouseClick(WinDef.HWND hwnd, int x, int y, int times) {

        try {
            y = y - 30;  // 调整Y坐标，去掉标题高度

            for (int i = 0; i < times; i++) {
                // 移动鼠标
                sendMouseEvent(hwnd, x, y, WindowsMessage.MOUSE_MOVE);
                // 添加短暂延迟
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // 按下右键
                sendMouseEvent(hwnd, x, y, WindowsMessage.RBUTTON_DOWN);
                // 松开右键
                sendMouseEvent(hwnd, x, y, WindowsMessage.RBUTTON_UP);
                TimeUnit.MILLISECONDS.sleep(100);
            }

            TimeUnit.MILLISECONDS.sleep(1000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    /**
     * 发送按键事件到指定窗口。
     *
     * @param hwnd 窗口句柄
     * @param key  按键代码（虚拟键代码）
     */
    public static void sendKeyEvent(WinDef.HWND hwnd, int key) {
        // 发送按下事件
        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_KEYDOWN, new WinDef.WPARAM(key), new WinDef.LPARAM(0));

        // 添加短暂延迟
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 发送松开事件
        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_KEYUP, new WinDef.WPARAM(key), new WinDef.LPARAM(0));
    }


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

            // 获取窗口大小
            WinDef.RECT rect = new WinDef.RECT();
            user32.GetWindowRect(hWnd, rect);
            int width = rect.right - rect.left;
            int height = rect.bottom - rect.top;

            log.info("找到句柄: {}  ,  窗口标题: {}   ,   类名: {}   ,   窗口大小: {}x{}", hWnd, windowText, className, width, height);
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

                // 获取窗口大小
                WinDef.RECT rect = new WinDef.RECT();
                user32.GetWindowRect(hWnd, rect);
                int width = rect.right - rect.left;
                int height = rect.bottom - rect.top;
                log.info("找到句柄: {}  ,  窗口标题: {}   ,   类名: {}   ,   窗口大小: {}x{}", hWnd, windowText, className, width, height);
            }
            return true;
        }, null);

        log.info("包含'{}'的窗口句柄集合:", wText);
        hwndList.forEach(hwnd -> log.info("{}", hwnd));

        return hwndList;
    }
}
