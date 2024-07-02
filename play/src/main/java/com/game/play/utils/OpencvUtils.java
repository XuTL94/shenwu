package com.game.play.utils;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OpencvUtils {

    /**
     * 单图匹配
     */
    public static Point findImageXY(String screenImgPath, String targetImgPath, double threshold, Rect region) {
        return findImageXY(screenImgPath, List.of(targetImgPath), threshold, region);
    }

    /**
     * 每张图片匹配到即跳过下一张继续匹配
     */
    public static List<Point> findAllOneImagesXY(String screenImgPath, List<String> targetImgPaths, double threshold, Rect region) {
        return findImagesXY(screenImgPath, targetImgPaths, threshold, region, false);
    }

    /**
     * 遍历匹配所有，响应所有大于阀值的
     */
    private static List<Point> findAllImagesXY(String screenImgPath, List<String> targetImgPaths, double threshold, Rect region) {
        return findImagesXY(screenImgPath, targetImgPaths, threshold, region, true);
    }





    /**
     * 单图或多图匹配，返回一个坐标
     * @return 匹配点
     */
    private static Point findImageXY(String screenImgPath, List<String> targetImgPaths, double threshold, Rect region) {

        Mat screen = Imgcodecs.imread(screenImgPath);

        // 指定区域, region 不传，则根据 screen 进行规划区域
        Rect adjustedRegion = (region != null) ? adjustRegion(screen, region) : new Rect(0, 0, screen.width(), screen.height());
        Mat screenRegion = new Mat(screen, adjustedRegion);

        for (String targetImgPath : targetImgPaths) {
            Mat target = Imgcodecs.imread(targetImgPath);

            Mat result = new Mat(screenRegion.rows() - target.rows() + 1, screenRegion.cols() - target.cols() + 1, CvType.CV_32FC1);
            Imgproc.matchTemplate(screenRegion, target, result, Imgproc.TM_CCOEFF_NORMED);

            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
            if (mmr.maxVal >= threshold) {
                // 计算目标中间XY
                Point matchPoint = new Point(mmr.maxLoc.x + (double) target.width() / 2, mmr.maxLoc.y + (double) target.height() / 2);
                // 补充区域计算
                return new Point(matchPoint.x + adjustedRegion.x, matchPoint.y + adjustedRegion.y);
            }
        }

        return null;
    }



    /**
     * 在指定矩形区域内查找指定颜色的像素坐标
     * @param screenImgPath 原始屏幕图片路径
     * @param startX 矩形区域左上角 x 坐标
     * @param startY 矩形区域左上角 y 坐标
     * @param width 矩形区域宽度
     * @param height 矩形区域高度
     * @param color 指定的颜色，以Scalar形式表示（BGR格式）
     * @return 找到的第一个匹配颜色的像素绝对坐标，未找到返回null
     */
    public static Point findColorCoordinate(String screenImgPath, int startX, int startY, int width, int height, Scalar color) {
        Mat screen = Imgcodecs.imread(screenImgPath);
        if (screen.empty()) {
            System.err.println("无法读取屏幕截图：" + screenImgPath);
            return null;
        }

        // 确保矩形区域不超出屏幕边界
        int endX = Math.min(startX + width, screen.cols());
        int endY = Math.min(startY + height, screen.rows());

        // 提取矩形区域
        Mat roi = new Mat(screen, new Rect(startX, startY, endX - startX, endY - startY));

        // 创建掩模并在掩模上执行颜色匹配
        Mat mask = new Mat();
        Core.inRange(roi, color, color, mask);

        // 查找第一个匹配颜色的像素坐标
        for (int y = 0; y < mask.rows(); y++) {
            for (int x = 0; x < mask.cols(); x++) {
                if (mask.get(y, x)[0] == 255) { // 找到匹配的颜色像素
                    return new Point(startX + x, startY + y); // 返回绝对坐标
                }
            }
        }

        return null; // 未找到匹配的颜色像素
    }




    /**
     * 多图匹配，返回多个坐标
     * @param region  指定区域
     * @param isLimit 是否每张图只匹配一次
     * @return
     */
    private static List<Point> findImagesXY(String screenImgPath, List<String> targetImgPaths, double threshold, Rect region, boolean isLimit) {

        Mat screen = Imgcodecs.imread(screenImgPath);

        // 指定区域,region不传，则根据screen进行规划区域
        Rect adjustedRegion = (region != null) ? adjustRegion(screen, region) : new Rect(0, 0, screen.width(), screen.height());
        Mat screenRegion = new Mat(screen, adjustedRegion);

        List<Point> points = new ArrayList<>();
        for (String targetImgPath : targetImgPaths) {
            Mat target = Imgcodecs.imread(targetImgPath);

            Mat result = new Mat(screenRegion.rows() - target.rows() + 1, screenRegion.cols() - target.cols() + 1, CvType.CV_32FC1);
            Imgproc.matchTemplate(screenRegion, target, result, Imgproc.TM_CCOEFF_NORMED);

            while (true) {
                Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
                if (mmr.maxVal >= threshold) {
                    // 计算目标中间XY
                    Point matchPoint = new Point(mmr.maxLoc.x + (double) target.width() / 2, mmr.maxLoc.y + (double) target.height() / 2);
                    // 补充区域计算
                    points.add(new Point(matchPoint.x + adjustedRegion.x, matchPoint.y + adjustedRegion.y));

                    // 是否需要遍历该图所有坐标
                    if (!isLimit) break;

                    // 将匹配区域置零以避免重复匹配
                    Imgproc.rectangle(result, mmr.maxLoc, new Point(mmr.maxLoc.x + target.cols(), mmr.maxLoc.y + target.rows()), new Scalar(0), -1);
                } else {
                    break;
                }
            }
        }

        return points;
    }



    /**
     * 区域设置
     */
    private static Rect adjustRegion(Mat screen, Rect region) {
        int x = Math.max(0, region.x);
        int y = Math.max(0, region.y);
        int width = Math.min(region.width, screen.width() - x);
        int height = Math.min(region.height, screen.height() - y);
        return new Rect(x, y, width, height);
    }
}
