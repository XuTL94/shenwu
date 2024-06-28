package com.game.play.utils;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;

@Slf4j
public class OpencvUtils {

    /**
     * 匹配获取离中心最近的坐标（单目标）
     * @param screenImgPath 句柄窗口截图
     * @param targetImgPath 目标图
     * @param threshold 匹配阈值  一般大于0.8
     * @return 离中心最近的匹配点
     */
    public static Point findImgNearsXY(String screenImgPath, String targetImgPath, double threshold) {
        return findNearestImageXY(screenImgPath, List.of(targetImgPath), threshold, null, true);
    }

    /**
     * 匹配获取离中心最近的坐标（多目标）
     */
    public static Point findImgNearsXY(String screenImgPath, List<String> targetImgPaths, double threshold) {
        return findNearestImageXY(screenImgPath, targetImgPaths, threshold, null, true);
    }

    /**
     * 匹配获取离中心最近的坐标（多目标），在指定区域内
     * @param region 指定的矩形区域（相对于screen）
     */
    public static Point findImgNearsXYInRegion(String screenImgPath, List<String> targetImgPaths, double threshold, Rect region) {
        return findNearestImageXY(screenImgPath, targetImgPaths, threshold, region, true);
    }

    /**
     * 匹配获取坐标（多目标），不计算离中心最近
     */
    public static Point findImgXY(String screenImgPath, List<String> targetImgPaths, double threshold, Rect region) {
        return findNearestImageXY(screenImgPath, targetImgPaths, threshold, region, false);
    }


    /**
     * 匹配获取离中心最近的怪物图片坐标，忽略头上有战斗图标的怪物
     * @param screenImgPath 句柄窗口截图
     * @param targetImgPath 怪物图像路径
     * @param ignoreIconImgPath 忽略战斗图标图像路径
     * @param threshold 匹配阈值
     * @return 离中心最近且头上没有战斗图标的怪物的匹配点
     */
    public static Point findImgNearsXYIgonreIcon(String screenImgPath, String targetImgPath, String ignoreIconImgPath, double threshold) {
        Mat screen = Imgcodecs.imread(screenImgPath);
        Mat target = Imgcodecs.imread(targetImgPath);
        Mat icon = Imgcodecs.imread(ignoreIconImgPath);

        if (screen.empty() || target.empty() || icon.empty()) {
            log.error("无法读取屏幕截图或目标图像或战斗图标图像");
            return null;
        }

        int resultCols = screen.cols() - target.cols() + 1;
        int resultRows = screen.rows() - target.rows() + 1;
        Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);
        Point nearestPoint = null;
        double minDistance = Double.MAX_VALUE;
        Point screenCenter = new Point(screen.width() / 2.0, screen.height() / 2.0);

        try {
            Imgproc.matchTemplate(screen, target, result, Imgproc.TM_CCOEFF_NORMED);

            while (true) {
                Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
                log.info("匹配最大值：{}, 位置：{}", mmr.maxVal, mmr.maxLoc);

                if (mmr.maxVal >= threshold) {
                    Point matchPoint = new Point(mmr.maxLoc.x + (double) target.width() / 2, mmr.maxLoc.y + (double) target.height() / 2);
                    log.info("匹配点：{}", matchPoint);

                    // 检查该匹配点上方是否有战斗图标
                    if (!isIconAbove(screen, icon, matchPoint, target.height() / 2, threshold)) {
                        double distance = MathUtils.distance(matchPoint, screenCenter);

                        if (distance < minDistance) {
                            nearestPoint = matchPoint;
                            minDistance = distance;
                        }
                    }

                    // 将匹配区域置零以避免重复匹配
                    Imgproc.rectangle(result, mmr.maxLoc,
                            new Point(mmr.maxLoc.x + target.cols(), mmr.maxLoc.y + target.rows()),
                            new Scalar(0), -1);
                } else {
                    break;
                }
            }

        } catch (Exception e) {
            log.error("模板匹配过程中出错", e);
        }

        return nearestPoint;
    }

    private static Point findNearestImageXY(String screenImgPath, List<String> targetImgPaths, double threshold, Rect region, boolean findNearest) {
        Mat screen = Imgcodecs.imread(screenImgPath);

        if (screen.empty()) {
            log.error("无法读取屏幕截图");
            return null;
        }

        Rect adjustedRegion = region != null ? adjustRegion(screen, region) : new Rect(0, 0, screen.width(), screen.height());
        Mat screenRegion = new Mat(screen, adjustedRegion);

        Point nearestPoint = null;
        double minDistance = Double.MAX_VALUE;
        Point regionCenter = new Point(adjustedRegion.width / 2.0, adjustedRegion.height / 2.0);

        try {
            for (String targetImgPath : targetImgPaths) {
                Mat target = Imgcodecs.imread(targetImgPath);

                if (target.empty()) {
                    log.error("无法读取目标图像: {}", targetImgPath);
                    continue;
                }

                Mat result = new Mat(screenRegion.rows() - target.rows() + 1, screenRegion.cols() - target.cols() + 1, CvType.CV_32FC1);
                Imgproc.matchTemplate(screenRegion, target, result, Imgproc.TM_CCOEFF_NORMED);

                while (true) {
                    Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
                    log.info("匹配最大值：{}, 位置：{}", mmr.maxVal, mmr.maxLoc);

                    if (mmr.maxVal >= threshold) {
                        Point matchPoint = new Point(mmr.maxLoc.x + (double) target.width() / 2, mmr.maxLoc.y + (double) target.height() / 2);
                        log.info("匹配点：{}", matchPoint);

                        // 是否需要计算中间坐标
                        if (!findNearest) {
                            return new Point(matchPoint.x + adjustedRegion.x, matchPoint.y + adjustedRegion.y);
                        }

                        double distance = MathUtils.distance(matchPoint, regionCenter);

                        if (distance < minDistance) {
                            nearestPoint = new Point(matchPoint.x + adjustedRegion.x, matchPoint.y + adjustedRegion.y);
                            minDistance = distance;
                        }

                        Imgproc.rectangle(result, mmr.maxLoc,
                                new Point(mmr.maxLoc.x + target.cols(), mmr.maxLoc.y + target.rows()),
                                new Scalar(0), -1);
                    } else {
                        break;
                    }
                }
            }

        } catch (Exception e) {
            log.error("模板匹配过程中出错", e);
            return null;
        }

        return nearestPoint;
    }

    private static Rect adjustRegion(Mat screen, Rect region) {
        return new Rect(
                Math.max(0, region.x),
                Math.max(0, region.y),
                Math.min(region.width, screen.width() - region.x),
                Math.min(region.height, screen.height() - region.y)
        );
    }

    /**
     * 判断在给定点上方是否匹配到战斗图标
     * @param screen 屏幕截图
     * @param icon 战斗图标图像
     * @param point 基准点
     * @param offsetY 上方偏移量（怪物高度）
     * @param threshold 匹配阈值
     * @return 是否匹配到战斗图标
     */
    private static boolean isIconAbove(Mat screen, Mat icon, Point point, int offsetY, double threshold) {
        int resultCols = screen.cols() - icon.cols() + 1;
        int resultRows = screen.rows() - icon.rows() + 1;
        Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);

        // 创建一个ROI，在基准点上方（怪物高度）处
        Rect roi = new Rect(
                (int) point.x - icon.width() / 2, // 计算icon的左上角X坐标
                (int) point.y - offsetY - icon.height(), // 计算icon的左上角Y坐标
                icon.width(), // 设置矩形区域的宽度
                icon.height() // 设置矩形区域的高度
        );

        if (roi.x < 0 || roi.y < 0 || roi.x + roi.width > screen.width() || roi.y + roi.height > screen.height()) {
            return false; // 如果ROI超出了图像边界，则直接返回不匹配
        }

        Mat roiMat = screen.submat(roi);
        Imgproc.matchTemplate(roiMat, icon, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        log.info("战斗图标匹配最大值：{}, 位置：{}", mmr.maxVal, mmr.maxLoc);
        return mmr.maxVal >= threshold;
    }
}
