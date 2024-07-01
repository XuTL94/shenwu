package com.game.play.enums;

/**
 * 枚举常用的 Windows 消息常量
 */
public enum WindowsMessage {
    MOUSE_MOVE(0x0200), // 鼠标移动事件。
    LBUTTON_DOWN(0x0201), // 鼠标左键按下事件。
    LBUTTON_UP(0x0202), // 鼠标左键松开事件。
    RBUTTON_DOWN(0x0204), // 鼠标右键按下事件。
    RBUTTON_UP(0x0205), // 鼠标右键松开事件。
    F1(0x70);


    private final int value;

    WindowsMessage(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
