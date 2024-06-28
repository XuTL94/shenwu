package com.game.play.service.shimen;

import com.game.play.enums.WindowsMessage;
import com.game.play.utils.CaptureUtils;
import com.game.play.utils.OpencvUtils;
import com.game.play.utils.WindowsUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Point;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class shimen {

    /**
     * 检查是否已接师门任务
     * 如果未接师门任务，则打开ctrl+Y 点击师门任务（匹配图色，满足条件则进行点击，然后等待5s,获取任务）
     * 识别师门任务类型
     * 1. 收集物资
     * 2. 捕抓宠物
     * 3. 援助同门
     */

// 识别师门任务类型的模板路径
    private static final String TEMPLATE_TASK1 = "templates/task_collect.png";
    private static final String TEMPLATE_TASK2 = "templates/task_capture_pet.png";
    private static final String TEMPLATE_TASK3 = "templates/task_assist.png";

    // 匹配阈值
    private static final double THRESHOLD = 0.8;

    /**
     * 检查是否已接师门任务
     * 如果未接师门任务，则打开Ctrl+Y点击师门任务
     */
    public void checkAndAcceptShimenTask() {
        // 获取游戏窗口句柄
        WinDef.HWND hWnd = WindowsUtils.getWindowHandle("游戏窗口名称", null);
        if (hWnd == null) {
            throw new RuntimeException("未找到游戏窗口");
        }

        String screenImgPath = CaptureUtils.captureWindow(hWnd);

        // 获取师门任务
        Point taskPoint = OpencvUtils.findImgXY(screenImgPath, List.of(TEMPLATE_TASK1, TEMPLATE_TASK2, TEMPLATE_TASK3), THRESHOLD, null);
        if (taskPoint == null) {
            System.out.println("师门任务已完成:");
        }


    }

    /**
     * 打开Ctrl+Y并点击师门任务
     */
    private void openShimenTask(WinDef.HWND hWnd) {
        // 模拟按下Ctrl+Y
        sendKeyPress(User32.VK_CONTROL);
        sendKeyPress(User32.VK_Y);
        sendKeyRelease(User32.VK_Y);
        sendKeyRelease(User32.VK_CONTROL);

        // 等待界面刷新
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 截图并识别师门任务
        String screenImgPath = CaptureUtils.captureWindow(hWnd);
        if (screenImgPath == null) {
            throw new RuntimeException("截图失败");
        }

        Point taskPoint = OpencvUtils.findImgXY(screenImgPath, List.of(TEMPLATE_TASK1, TEMPLATE_TASK2, TEMPLATE_TASK3), THRESHOLD, null);
        if (taskPoint != null) {
            // 点击师门任务
            click(hWnd, taskPoint);
            try {
                Thread.sleep(5000); // 等待5秒获取任务
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            throw new RuntimeException("未找到师门任务");
        }
    }

    /**
     * 识别师门任务类型
     */
    private String recognizeTaskType(String screenImgPath) {
        if (OpencvUtils.findImgXY(screenImgPath, List.of(TEMPLATE_TASK1), THRESHOLD, null) != null) {
            return "收集物资";
        } else if (OpencvUtils.findImgXY(screenImgPath, List.of(TEMPLATE_TASK2), THRESHOLD, null) != null) {
            return "捕抓宠物";
        } else if (OpencvUtils.findImgXY(screenImgPath, List.of(TEMPLATE_TASK3), THRESHOLD, null) != null) {
            return "援助同门";
        }
        return "未知任务类型";
    }

    /**
     * 模拟点击指定位置
     */
    private void click(WinDef.HWND hWnd, Point point) {
        int x = (int) point.x;
        int y = (int) point.y;
        int lParam = (y << 16) | (x & 0xFFFF);
        log.info("发送鼠标点击到窗口，坐标：({}, {})，lParam：{}", x, y, lParam);
        User32.INSTANCE.PostMessage(hWnd, WindowsMessage.WM_LBUTTONDOWN.getValue(), new WinDef.WPARAM(1), new WinDef.LPARAM(lParam));
        User32.INSTANCE.PostMessage(hWnd, WindowsMessage.WM_LBUTTONUP.getValue(), new WinDef.WPARAM(1), new WinDef.LPARAM(lParam));
    }

    /**
     * 发送按键按下事件
     */
    private void sendKeyPress(byte key) {
        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki.wVk = key;
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
    }

    /**
     * 发送按键释放事件
     */
    private void sendKeyRelease(byte key) {
        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki.wVk = key;
        input.input.ki.dwFlags = new WinDef.DWORD(WinUser.KEYBDINPUT.KEYEVENTF_KEYUP);
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
    }
}