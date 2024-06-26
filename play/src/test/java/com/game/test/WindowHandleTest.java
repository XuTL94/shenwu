package com.game.test;

import com.game.play.utils.CaptureUtils;
import com.game.play.utils.WindowsUtils;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class WindowHandleTest {

    @Test
    void getWinDef() {
        User32 user32 = User32.INSTANCE;
        List<WinDef.HWND> hwndList = new ArrayList<>();
        // 枚举所有顶级窗口并输出窗口标题包含"幻唐志"的窗口句柄、窗口标题和类名
        user32.EnumWindows((hWnd, data) -> {
            // 获取窗口标题
            char[] windowText = new char[512];
            user32.GetWindowText(hWnd, windowText, 512);
            String wText = Native.toString(windowText);

            // 获取窗口类名
            char[] className = new char[512];
            user32.GetClassName(hWnd, className, 512);
            String cName = Native.toString(className);

            // 如果窗口标题包含"幻唐志"，则添加到集合中
            if (wText.contains("幻唐志")) {
                hwndList.add(hWnd);
                System.out.println(String.format("句柄: %s, 窗口标题: %s, 类名: %s", hWnd, wText, cName));
            }
            return true; // 返回true继续枚举
        }, null);

        // 打印集合中的句柄
        System.out.println("包含'幻唐志'的窗口句柄集合:");
        for (WinDef.HWND hwnd : hwndList) {
            System.out.println(hwnd);
        }
    }


    private static final int DIB_RGB_COLORS = 0;

    @Test
    public void testWindowCapture() {
        String targetWindowTitle = "战盟";
        String outputFileNamePrefix = "test_capture";

        // 获取 temp 目录
        Path tempDir = Paths.get("").toAbsolutePath().resolve("temp");
        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectory(tempDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 获取包含特定标题的窗口句柄
        User32 user32 = User32.INSTANCE;
        List<WinDef.HWND> hwndList = new ArrayList<>();
        user32.EnumWindows((hWnd, data) -> {
            char[] windowText = new char[512];
            user32.GetWindowText(hWnd, windowText, 512);
            String wText = Native.toString(windowText);

            char[] className = new char[512];
            user32.GetClassName(hWnd, className, 512);
            String cName = Native.toString(className);

            if (wText.equals(targetWindowTitle)) {
                hwndList.add(hWnd);
                System.out.printf("句柄: %s, 窗口标题: %s, 类名: %s%n", hWnd, wText, cName);
            }
            return true;
        }, null);

        if (hwndList.isEmpty()) {
            System.out.println("未找到包含指定标题的窗口！");
            return;
        }

        // 捕捉每个符合条件的窗口图像
        int index = 0;
        for (WinDef.HWND hwnd : hwndList) {
            captureWindow(hwnd, tempDir.resolve(outputFileNamePrefix + "_" + index + ".png").toString());
            index++;
        }
    }

    public void captureWindow(WinDef.HWND hWnd, String outputFileName) {
        User32 user32 = User32.INSTANCE;
        GDI32 gdi32 = GDI32.INSTANCE;

        // 获取窗口尺寸
        WinDef.RECT rect = new WinDef.RECT();
        user32.GetWindowRect(hWnd, rect);
        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        // 创建DC
        WinDef.HDC hdcWindow = user32.GetDC(hWnd);
        WinDef.HDC hdcMemDC = gdi32.CreateCompatibleDC(hdcWindow);
        WinDef.HBITMAP hBitmap = gdi32.CreateCompatibleBitmap(hdcWindow, width, height);
        WinNT.HANDLE hOld = gdi32.SelectObject(hdcMemDC, hBitmap);

        // 使用 PrintWindow 捕捉窗口图像
        user32.PrintWindow(hWnd, hdcMemDC, 0);

        // 获取位图数据
        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biSize = 40;
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height;
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = 0;

        Memory buffer = new Memory(width * height * 4);
        gdi32.GetDIBits(hdcWindow, hBitmap, 0, height, buffer, bmi, DIB_RGB_COLORS);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int bufferOffset = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int b = buffer.getByte(bufferOffset) & 0xFF;
                int g = buffer.getByte(bufferOffset + 1) & 0xFF;
                int r = buffer.getByte(bufferOffset + 2) & 0xFF;
                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
                bufferOffset += 4;
            }
        }

        gdi32.SelectObject(hdcMemDC, hOld);
        gdi32.DeleteDC(hdcMemDC);
        user32.ReleaseDC(hWnd, hdcWindow);
        gdi32.DeleteObject(hBitmap);

        try {
            File outputfile = new File(outputFileName);
            ImageIO.write(image, "png", outputfile);
            System.out.println("截图已保存到: " + outputFileName);
            assertTrue(outputfile.exists(), "捕捉图像文件未保存");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
