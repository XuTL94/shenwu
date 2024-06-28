package com.game.play.enums;

/**
 * 枚举常用的 Windows 消息常量
 */
public enum WindowsMessage {
    WM_LBUTTONDOWN(0x0201), // 鼠标左键按下消息
    WM_LBUTTONUP(0x0202),   // 鼠标左键抬起消息
    WM_RBUTTONDOWN(0x0204), // 鼠标右键按下消息
    WM_RBUTTONUP(0x0205),   // 鼠标右键抬起消息
    WM_KEYDOWN(0x0100),     // 按键按下消息
    WM_KEYUP(0x0101),       // 按键抬起消息
    WM_CLOSE(0x0010),       // 窗口关闭消息
    WM_QUIT(0x0012),        // 退出消息
    WM_SETTEXT(0x000C),     // 设置文本消息
    WM_GETTEXT(0x000D),     // 获取文本消息
    WM_GETTEXTLENGTH(0x000E), // 获取文本长度消息
    WM_COMMAND(0x0111);     // 命令消息

    private final int value;

    WindowsMessage(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
