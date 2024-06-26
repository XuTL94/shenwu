package com.game.play.config;

import com.game.play.utils.CaptureUtils;
import com.game.play.utils.WindowsUtils;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.awt.*;
import java.awt.event.InputEvent;
import java.nio.file.Paths;

import com.sun.jna.platform.win32.WinDef;

public class GameAutomation {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    public static void main(String[] args) throws Exception {
        // 获取窗口句柄和截图路径
        WinDef.HWND handle = WindowsUtils.getWindowHandle("战盟", "");
        String screenImgPath = CaptureUtils.captureWindow(handle);

        // 模板匹配
        String templateImgPath = "E:\\code\\shenwu\\Snipaste_2024-06-26_23-44-01.png"; // 模板图像路径
        Point matchLocation = findImageOnScreen(screenImgPath, templateImgPath);

        if (matchLocation != null) {
            System.out.println("模板匹配成功，位置：" + matchLocation);
            // 模拟点击
            Robot robot = new Robot();
            robot.mouseMove((int) matchLocation.x, (int) matchLocation.y);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } else {
            System.out.println("未找到模板匹配");
        }
    }

    public static Point findImageOnScreen(String screenImgPath, String templateImgPath) {
        Mat screen = Imgcodecs.imread(screenImgPath);
        Mat template = Imgcodecs.imread(templateImgPath);

        int resultCols = screen.cols() - template.cols() + 1;
        int resultRows = screen.rows() - template.rows() + 1;
        Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);

        Imgproc.matchTemplate(screen, template, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        if (mmr.maxVal >= 0.8) { // 设定匹配阈值
            Point matchPoint = new Point(mmr.maxLoc.x + template.width() / 2, mmr.maxLoc.y + template.height() / 2);
            return matchPoint;
        }
        return null;
    }
}
