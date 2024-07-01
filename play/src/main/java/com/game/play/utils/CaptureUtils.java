package com.game.play.utils;

import cn.hutool.core.util.IdUtil;
import com.sun.jna.Memory;
import com.sun.jna.platform.win32.*;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 截图工具类
 */

@Slf4j
public class CaptureUtils {

    private static final int DIB_RGB_COLORS = 0;

    public static String captureWindow(WinDef.HWND hWnd) {
        User32 user32 = User32.INSTANCE;
        GDI32 gdi32 = GDI32.INSTANCE;

        // 获取窗口尺寸
        WinDef.RECT rect = new WinDef.RECT();
        user32.GetWindowRect(hWnd, rect);
        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        log.info("窗口尺寸: 宽度 = {}, 高度 = {}", width, height);

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
            String tempDir = System.getProperty("user.dir") + File.separator + "temp";
            String tempImgName = tempDir + File.separator + IdUtil.fastUUID() + ".png";
            File outputfile = new File(tempImgName);
            ImageIO.write(image, "png", outputfile);
            //log.info("截图已保存到: {}", tempImgName);
            return tempImgName;
        } catch (IOException e) {
            log.error("捕捉图像失败", e);
            return null;
        }
    }

}
