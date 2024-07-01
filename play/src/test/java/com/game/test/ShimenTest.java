package com.game.test;

import cn.hutool.core.util.ObjectUtil;
import com.game.play.utils.CaptureUtils;
import com.game.play.utils.OpencvUtils;
import com.game.play.utils.ShenwuUtils;
import com.game.play.utils.WindowsUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShimenTest {

    private static final Logger log = LoggerFactory.getLogger(ShimenTest.class);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    // 完成师门任务
    @Test
    void finishSM() throws InterruptedException {
        List<WinDef.HWND> windowHandles = WindowsUtils.getWindowHandles("幻唐志 - ", null);

        WinDef.HWND hwnd = windowHandles.get(0);
        String screenImgPath = CaptureUtils.captureWindow(hwnd);


        String templateImgPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\smFinish.png";
        Point initialPoint = OpencvUtils.findImgXY(screenImgPath, templateImgPath, 0.8, null);

        if (initialPoint == null) {
            log.error("师门任务未完成");
            return;
        }

        log.info("师门任务完成  初始匹配点：{}", initialPoint);

        Point colorPoint = OpencvUtils.findColorCoordinate(screenImgPath, (int) initialPoint.x, (int) (initialPoint.y + 5), 150, 15, new Scalar(254, 254, 0));

        // 寻找寻路以及点击
        if (colorPoint != null) {

            int relativeX = (int) colorPoint.x;
            int relativeY = (int) colorPoint.y - 30; //减去30 发送给句柄的的这个标识，没算上标题的

            log.info("师门任务完成  计算出的相对点击坐标: ({}, {})", relativeX, relativeY);

            // 跑回师门
            WindowsUtils.sendMouseClick(hwnd, relativeX, relativeY,1);

            // 等待跑到师门
            TimeUnit.MILLISECONDS.sleep(5000);

            // 点击提交任务
            WindowsUtils.sendMouseClick(hwnd, 400-5, 532 - 30,1);


            WindowsUtils.sendMouseClick(hwnd, 505, 426 - 30,1);



            // 判断是否出现物质提交
            String commitFlagImgPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\commitCollectionIcon1.png";
            Point allImgXY = OpencvUtils.findImgXY(CaptureUtils.captureWindow(hwnd), templateImgPath, 0.8,null);

            if(ObjectUtil.isNotEmpty(allImgXY)){
                List<String> strings = List.of("E:\\code\\yunpu\\shenwu\\temp\\biaoji\\zhenlujiu.png", "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\babaozhou.png",
                        "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\taxueyanwo.png","E:\\code\\yunpu\\shenwu\\temp\\biaoji\\shedanjiu.png"
                        ,"E:\\code\\yunpu\\shenwu\\temp\\biaoji\\zuishengmengsi.png","E:\\code\\yunpu\\shenwu\\temp\\biaoji\\guihunlu.png");
                List<Point> aaallImgXY = OpencvUtils.findAllOneImgsXY(CaptureUtils.captureWindow(hwnd), strings, 0.80);
                if (aaallImgXY != null) {
                    for (Point point : aaallImgXY) {
                        TimeUnit.MILLISECONDS.sleep(500);
                        WindowsUtils.sendMouseClick(hwnd, (int) point.x, (int) (point.y - 30),1);

                    }
                }

                WindowsUtils.sendMouseClick(hwnd, 383, 606 - 30,1);

                // 发送 ESC 键事件到窗口
                //WindowsUtils.sendKeyEvent(hwnd, 0x1B); // 0x1B 是 ESC 键的虚拟键代码

            }


        } else {
            log.info("在矩形区域内未找到指定颜色的像素");
        }
    }


    // 师门类型  物质收集
    @Test
    void ToDoSMCollection() throws InterruptedException {
        List<WinDef.HWND> windowHandles = WindowsUtils.getWindowHandles("幻唐志 - ", null);

        WinDef.HWND hwnd = windowHandles.get(0);
        String screenImgPath = CaptureUtils.captureWindow(hwnd);


        String templateImgPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMCollection.png";
        Point initialPoint = OpencvUtils.findImgXY(screenImgPath, templateImgPath, 0.8, null);

        if (initialPoint == null) {
            log.error("师门任务未找到");
            return;
        }

        // 打开交易中心 22 307
        int relativeX = 22;
        int relativeY = 307 - 30; // 交易中心坐标
        log.info("打开交易中心  坐标: ({}, {})", 22, 307 - 30);
        WindowsUtils.sendMouseClick(hwnd, relativeX, relativeY,1);
        TimeUnit.MILLISECONDS.sleep(300);

        // 判断是否包含需要购买的物质
        String collectionIconPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\collectionIcon.png";
        TimeUnit.MILLISECONDS.sleep(300);
        /*Point point = OpencvUtils.findImgXY(CaptureUtils.captureWindow(hwnd), collectionIconPath, 0.8, null);
        if (ObjectUtil.isNotEmpty(point)) {*/
            // 点击购买  坐标
            int x = 566;
            int y = 620 - 30; // 交易中心坐标

            log.info("打开交易中心  坐标: ({}, {})", relativeX, relativeY);
            WindowsUtils.sendMouseClick(hwnd, x, y,3);
            // 发送 ESC 键事件到窗口
            WindowsUtils.sendKeyEvent(hwnd, 0x1B); // 0x1B 是 ESC 键的虚拟键代码
        //}

    }

    // 师门类型  购买宠物
    @Test
    void ToDoSMChongwu() throws InterruptedException {
        List<WinDef.HWND> windowHandles = WindowsUtils.getWindowHandles("幻唐志 - ", null);

        WinDef.HWND hwnd = windowHandles.get(0);
        String screenImgPath = CaptureUtils.captureWindow(hwnd);


        String templateImgPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMchongwu.png";
        Point initialPoint = OpencvUtils.findImgXY(screenImgPath, templateImgPath, 0.8, null);

        if (initialPoint == null) {
            log.error("师门任务未找到");
            return;
        }

        // 点击宠物头像   820  60
        WindowsUtils.sendMouseClick(hwnd, 820, 60-30,1);
        // 下拉最底下
        WindowsUtils.sendMouseClick(hwnd, 460, 350-30,1);
        // 点击获取更多宠物
        WindowsUtils.sendMouseClick(hwnd, 350, 350-30,1);
        // 点击信誉购买
        WindowsUtils.sendMouseClick(hwnd, 700-5, 570-30,1);
        WindowsUtils.sendKeyEvent(hwnd, 0x1B);

    }


    // 师门类型  灵虚裂痕
    @Test
    void ToDoSMLingxu() throws InterruptedException {
        List<WinDef.HWND> windowHandles = WindowsUtils.getWindowHandles("幻唐志 - ", null);

        WinDef.HWND hwnd = windowHandles.get(0);
        String screenImgPath = CaptureUtils.captureWindow(hwnd);


        String templateImgPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMLingxu.png";
        Point initialPoint = OpencvUtils.findImgXY(screenImgPath, templateImgPath, 0.8, null);

        if (initialPoint == null) {
            log.error("师门任务未找到");
            return;
        }

        // 点击灵虚裂痕
        WindowsUtils.sendMouseClick(hwnd, (int) initialPoint.x-5, (int) (initialPoint.y-30),1);
        // 判断战斗是否已完成
        WindowsUtils.sendKeyEvent(hwnd, 0x1B);

    }


    // 师门类型  调整首席
    @Test
    void ToDoSMShouxi() throws InterruptedException {
        List<WinDef.HWND> windowHandles = WindowsUtils.getWindowHandles("幻唐志 - ", null);

        WinDef.HWND hwnd = windowHandles.get(0);
        String screenImgPath = CaptureUtils.captureWindow(hwnd);


        String templateImgPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\shouxiIcon.png";
        Point initialPoint = OpencvUtils.findImgXY(screenImgPath, templateImgPath, 0.8, null);

        if (initialPoint == null) {
            log.error("师门任务未找到");
            return;
        }
        // 点击首席战斗
        WindowsUtils.sendMouseClick(hwnd, (int) initialPoint.x-5, (int) (initialPoint.y-30),1);

        // 判断战斗是否已完成
        //WindowsUtils.sendKeyEvent(hwnd, 0x1B);
        String guanbiPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\guanbiIcon.png"; // 战斗完成标识图像路径
        boolean isFinished = false;
        long startTime = System.currentTimeMillis();
        long maxDuration = 5 * 60 * 1000; // 5分钟

        while (System.currentTimeMillis() - startTime < maxDuration) {
            Point finishPoint = OpencvUtils.findImgXY(CaptureUtils.captureWindow(hwnd), guanbiPath, 0.8, null);
            if (finishPoint != null) {
                isFinished = true;
                break;
            }
            Thread.sleep(10000); // 每10秒检查一次
        }

        if (!isFinished) {
            log.info("战斗未完成，超时处理为已完成");
        } else {
            log.info("战斗已完成");
        }

    }



    @Test
    void capture() {
        List<WinDef.HWND> windowHandles = WindowsUtils.getWindowHandles("幻唐志 - 二", null);
        WinDef.HWND hwnd = windowHandles.get(0);
        // Simulate pressing the Escape key
        /*User32.INSTANCE.PostMessage(hwnd, WinUser.WM_KEYDOWN, new WinDef.WPARAM(0x1B), new WinDef.LPARAM(0)); // VK_ESCAPE = 0x1B
        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_KEYUP, new WinDef.WPARAM(0x1B), new WinDef.LPARAM(0));   // VK_ESCAPE = 0x1B*/
        CaptureUtils.captureWindow(hwnd);
    }

    @Test
    void sendMsg() {
        List<WinDef.HWND> windowHandles = WindowsUtils.getWindowHandles("幻唐志 - 二", null);
        WinDef.HWND hwnd = windowHandles.get(0);
        // Simulate pressing the Escape key
        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_KEYDOWN, new WinDef.WPARAM(0x1B), new WinDef.LPARAM(0)); // VK_ESCAPE = 0x1B
        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_KEYUP, new WinDef.WPARAM(0x1B), new WinDef.LPARAM(0));   // VK_ESCAPE = 0x1B
    }


    // 完成师门任务
    @Test
    void matchingWithColorCheck() {
        List<WinDef.HWND> windowHandles = WindowsUtils.getWindowHandles("幻唐志 - ", null);
        if (windowHandles.isEmpty()) {
            log.error("未找到窗口句柄");
            return;
        }

        WinDef.HWND hwnd = windowHandles.get(0);
        String screenImgPath = CaptureUtils.captureWindow(hwnd);

        // 获取窗口的矩形区域
        WinDef.RECT rect = new WinDef.RECT();
        if (User32.INSTANCE.GetWindowRect(hwnd, rect)) {
            int width = rect.right - rect.left;
            int height = rect.bottom - rect.top;
            log.info("窗口宽度: {}, 窗口高度: {}", width, height);
        } else {
            log.error("获取窗口矩形区域失败");
            return;
        }

        if (screenImgPath == null || screenImgPath.isEmpty()) {
            log.error("截图失败");
            return;
        }

        String templateImgPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\sm-finish.png";
        Point initialPoint = OpencvUtils.findImgXY(screenImgPath, templateImgPath, 0.8, null);

        if (initialPoint == null) {
            log.error("未找到初始匹配点");
            return;
        }

        log.info("初始匹配点：{}", initialPoint);

        Point colorPoint = OpencvUtils.findColorCoordinate(screenImgPath, (int) initialPoint.x, (int) (initialPoint.y + 5), 150, 10, new Scalar(254, 254, 0));

        if (colorPoint != null) {
            log.info("在屏幕截图 {} 内找到指定颜色的像素绝对坐标：{}", screenImgPath, colorPoint);

            int relativeX = (int) colorPoint.x;
            int relativeY = (int) colorPoint.y - 30; //减去30 发送给句柄的的这个标识，没算上标题的

            log.info("计算出的相对点击坐标: ({}, {})", relativeX, relativeY);

            // 发送鼠标移动和点击事件到窗口
            WindowsUtils.sendMouseClick(hwnd, relativeX, relativeY,1);

        } else {
            log.info("在矩形区域内未找到指定颜色的像素");
        }
    }


    private void printWindowInfo(WinDef.HWND hwnd) {
        WinUser.WINDOWINFO info = new WinUser.WINDOWINFO();
        User32.INSTANCE.GetWindowInfo(hwnd, info);

        System.out.println("窗口句柄: " + hwnd);
        System.out.println("窗口坐标: " + info.rcWindow.left + "," + info.rcWindow.top + " - " + info.rcWindow.right + "," + info.rcWindow.bottom);
        System.out.println("客户区坐标: " + info.rcClient.left + "," + info.rcClient.top + " - " + info.rcClient.right + "," + info.rcClient.bottom);
        System.out.println("窗口样式: " + info.dwStyle);
        System.out.println("窗口扩展样式: " + info.dwExStyle);
        System.out.println("窗口状态: " + info.dwWindowStatus);
        System.out.println("窗口边框宽度: " + info.cxWindowBorders);
        System.out.println("窗口边框高度: " + info.cyWindowBorders);
        System.out.println("窗口类Atom: " + info.atomWindowType);
        System.out.println("窗口版本: " + info.wCreatorVersion);
    }
}
