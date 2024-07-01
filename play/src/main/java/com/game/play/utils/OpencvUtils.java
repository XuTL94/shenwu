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
     * 匹配获取坐标（单目标），不计算离中心最近
     * region为空，则不需要指定区域匹配
     */
    public static Point findImgXY(String screenImgPath, String targetImgPath, double threshold, Rect region) {
        return findNearestImageXY(screenImgPath, List.of(targetImgPath), threshold, region, false);
    }

    /**
     * 匹配获取坐标（多目标），不计算离中心最近
     */
    public static Point findImgsXY(String screenImgPath, List<String> targetImgPaths, double threshold, Rect region) {
        return findNearestImageXY(screenImgPath, targetImgPaths, threshold, region, false);
    }

    /**
     * 匹配获取所有符合条件的坐标（多目标）
     * @param screenImgPath 句柄窗口截图
     * @param targetImgPaths 目标图集合
     * @param threshold 匹配阈值  一般大于0.8
     * @return 所有匹配点的集合
     */
    public static List<Point> findAllOneImgsXY(String screenImgPath, List<String> targetImgPaths, double threshold) {
        return findAllOneImagesXY(screenImgPath, targetImgPaths, threshold, null);
    }

    /**
     * 匹配获取所有符合条件的坐标（多目标）
     * @param screenImgPath 句柄窗口截图
     * @param targetImgPaths 目标图集合
     * @param threshold 匹配阈值  一般大于0.8
     * @return 所有匹配点的集合
     */
    public static List<Point> findAllImgsXY(String screenImgPath, List<String> targetImgPaths, double threshold) {
        return findAllImagesXY(screenImgPath, targetImgPaths, threshold, null);
    }

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

    /**
     * 匹配获取所有符合条件的坐标（单目标）
     * @param screenImgPath 句柄窗口截图
     * @param targetImgPath 目标图
     * @param threshold 匹配阈值  一般大于0.8
     * @return 所有匹配点的集合
     */
    public static List<Point> findAllImgXY(String screenImgPath, String targetImgPath, double threshold) {
        return findAllImagesXY(screenImgPath, List.of(targetImgPath), threshold, null);
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

    private static List<Point> findAllImagesXY(String screenImgPath, List<String> targetImgPaths, double threshold, Rect region) {
        Mat screen = Imgcodecs.imread(screenImgPath);

        if (screen.empty()) {
            log.error("无法读取屏幕截图");
            return null;
        }

        Rect adjustedRegion = region != null ? adjustRegion(screen, region) : new Rect(0, 0, screen.width(), screen.height());
        Mat screenRegion = new Mat(screen, adjustedRegion);

        List<Point> points = new ArrayList<>();

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
                    log.info("匹配最大值：{}, 起始点位置：{}", mmr.maxVal, mmr.maxLoc);

                    if (mmr.maxVal >= threshold) {
                        Point matchPoint = new Point(mmr.maxLoc.x + (double) target.width() / 2, mmr.maxLoc.y + (double) target.height() / 2);
                        log.info("匹配点：{}", matchPoint);
                        points.add(new Point(matchPoint.x + adjustedRegion.x, matchPoint.y + adjustedRegion.y));

                        // 将匹配区域置零以避免重复匹配
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

        return points;
    }

    /**
     * 每张图片匹配到即跳过下一张继续匹配
     * @param screenImgPath
     * @param targetImgPaths
     * @param threshold
     * @param region
     * @return
     */
    public static List<Point> findAllOneImagesXY(String screenImgPath, List<String> targetImgPaths, double threshold, Rect region) {
        Mat screen = Imgcodecs.imread(screenImgPath);

        if (screen.empty()) {
            log.error("无法读取屏幕截图");
            return null;
        }

        Rect adjustedRegion = region != null ? adjustRegion(screen, region) : new Rect(0, 0, screen.width(), screen.height());
        Mat screenRegion = new Mat(screen, adjustedRegion);

        List<Point> points = new ArrayList<>();

        try {
            for (String targetImgPath : targetImgPaths) {
                Mat target = Imgcodecs.imread(targetImgPath);

                if (target.empty()) {
                    log.error("无法读取目标图像: {}", targetImgPath);
                    continue;
                }

                Mat result = new Mat(screenRegion.rows() - target.rows() + 1, screenRegion.cols() - target.cols() + 1, CvType.CV_32FC1);
                Imgproc.matchTemplate(screenRegion, target, result, Imgproc.TM_CCOEFF_NORMED);

                boolean foundMatch = false;
                while (true) {
                    Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
                    log.info("匹配最大值：{}, 起始点位置：{}", mmr.maxVal, mmr.maxLoc);
                    if (mmr.maxVal >= threshold) {
                        Point matchPoint = new Point(mmr.maxLoc.x + (double) target.width() / 2, mmr.maxLoc.y + (double) target.height() / 2);
                        points.add(new Point(matchPoint.x + adjustedRegion.x, matchPoint.y + adjustedRegion.y));
                        foundMatch = true;

                        // 将匹配区域置零以避免重复匹配
                        Imgproc.rectangle(result, mmr.maxLoc,
                                new Point(mmr.maxLoc.x + target.cols(), mmr.maxLoc.y + target.rows()),
                                new Scalar(0), -1);

                        break;
                    } else {
                        break;
                    }
                }

                // 如果找到匹配，跳过当前目标图像
                if (foundMatch) {
                    continue;
                }
            }

        } catch (Exception e) {
            log.error("模板匹配过程中出错", e);
            return null;
        }

        return points;
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
}
