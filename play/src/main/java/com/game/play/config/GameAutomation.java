package com.game.play.config;

import com.game.play.utils.CaptureUtils;
import com.game.play.utils.WindowsUtils;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.User32;

@Slf4j
public class GameAutomation {

    static String imgGalleryDir = System.getProperty("user.dir") + File.separator + "imgGallery" + File.separator;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // 定义鼠标点击事件常量
    private static final int WM_LBUTTONDOWN = 0x0201;
    private static final int WM_LBUTTONUP = 0x0202;

    public static void main(String[] args) throws Exception {
       /* // 获取窗口句柄和截图路径
        WinDef.HWND handle = WindowsUtils.getWindowHandle("战盟", "");
        String screenImgPath = CaptureUtils.captureWindow(handle);

        // 模板匹配
        String templateImgPath = imgGalleryDir + "23232.png"; // 模板图像路径*/
        String screenImgPath = "E:\\code\\yunpu\\shenwu\\temp\\a.png";
        String templateImgPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\result.png";
        Point matchLocation = findImageOnScreen(screenImgPath, templateImgPath);

        /*if (matchLocation != null) {
            log.info("模板匹配成功，位置：{}", matchLocation);
            // 发送鼠标点击消息到窗口句柄
            sendMouseClick(handle, (int) matchLocation.x, (int) matchLocation.y);
        } else {
            log.info("未找到模板匹配");
        }*/
    }

    public static Point findImageOnScreen(String screenImgPath, String templateImgPath) {
        Mat screen = Imgcodecs.imread(screenImgPath);
        Mat template = Imgcodecs.imread(templateImgPath);

        if (screen.empty() || template.empty()) {
            log.error("无法读取屏幕截图或模板图像");
            return null;
        }

        int resultCols = screen.cols() - template.cols() + 1;
        int resultRows = screen.rows() - template.rows() + 1;
        Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);

        Imgproc.matchTemplate(screen, template, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        log.info("匹配最大值：{}, 位置：{}", mmr.maxVal, mmr.maxLoc);

        if (mmr.maxVal >= 0.8) { // 设定匹配阈值
            // 获取模板匹配位置，返回中心点坐标
            Point matchPoint = new Point(mmr.maxLoc.x + template.width() / 2, mmr.maxLoc.y + template.height() / 2);
            log.info("匹配点：{}", matchPoint);
            return matchPoint;
        }
        return null;
    }

    private static void sendMouseClick(WinDef.HWND hWnd, int x, int y) {
        int lParam = (y << 16) | (x & 0xFFFF);
        log.info("发送鼠标点击到窗口，坐标：({}, {})，lParam：{}", x, y, lParam);
        User32.INSTANCE.PostMessage(hWnd, WM_LBUTTONDOWN, new WinDef.WPARAM(1), new WinDef.LPARAM(lParam));
        User32.INSTANCE.PostMessage(hWnd, WM_LBUTTONUP, new WinDef.WPARAM(1), new WinDef.LPARAM(lParam));
    }
}
