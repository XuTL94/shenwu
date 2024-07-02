package com.game.test;

import cn.hutool.core.util.ObjectUtil;
import com.game.play.utils.CaptureUtils;
import com.game.play.utils.OpencvUtils2;
import com.game.play.utils.ShenwuUtils;
import com.game.play.utils.WindowsUtils;
import com.sun.jna.platform.win32.WinDef;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShimenFinishTest2 {

    private static final Logger log = LoggerFactory.getLogger(ShimenTest.class);

    public static Map<String, String> agendaBarImgPathList = new HashMap<>(); // 日程栏任务
    public static Map<String, String> commonImgList = new HashMap<>(); // 公共使用
    public static List<String> giveWuziImgPathList = new ArrayList<>(); // 给予物资
    public static List<String> smTypeImgPathList = new ArrayList<>(); // 类型类型集合（判断任务栏是否存在师门任务）
    public static Map<String, String> smDoList = new HashMap<>(); // 执行师门任务相关集合

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        loadImages("agendaBar", agendaBarImgPathList);
        loadImages("giveWuzi", giveWuziImgPathList);
        loadImages("sm/smType", smTypeImgPathList);
        loadImages("common", commonImgList);
        loadImages("sm", smDoList);
    }

    private static void loadImages(String directoryName, List<String> list) {
        String dir = System.getProperty("user.dir") + File.separator + directoryName;
        try (Stream<Path> paths = Files.list(Paths.get(dir))) {
            list.addAll(paths.filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("读取目录失败{}", dir, e);
        }
    }

    private static void loadImages(String directoryName, Map<String, String> map) {
        String dir = "E:\\yunpu\\shenwu\\imgs" + File.separator + directoryName;
        try (Stream<Path> paths = Files.list(Paths.get(dir))) {
            map.putAll(paths.filter(Files::isRegularFile)
                    .collect(Collectors.toMap(
                            path -> path.getFileName().toString().replaceFirst("[.][^.]+$", ""), // 去掉文件类型后缀
                            Path::toString))); // 使用完整路径作为值
        } catch (Exception e) {
            log.error("读取目录失败{}", dir, e);
        }
    }

    @Test
    void test() {
        WinDef.HWND hwnd = ShenwuUtils.getAvailableHwnd();
        if (hwnd == null) {
            log.error("未找到可用的句柄，请先启动游戏");
            return;
        }
        WindowsUtils.sendRightMouseClick(hwnd, 170, 260, 3);
    }

    /**
     * 完成师门任务
     *
     * @throws InterruptedException
     */
    @Test
    void finishSM() throws InterruptedException {

        WinDef.HWND hwnd = ShenwuUtils.getAvailableHwnd();
        if (hwnd == null) {
            log.error("未找到可用的句柄，请先启动游戏");
            return;
        }

        // 循环处理任务，直到没有任务类型图标匹配
        while (ObjectUtil.isNotEmpty(OpencvUtils2.findAllOneImgsXY(CaptureUtils.captureWindow(hwnd), smTypeImgPathList, 0.8))) {
            log.info("师门任务进行中...");
            boolean finishOneFlag = false;

            // 处理物质收集任务
            if (handleTask(hwnd, smDoList.get("toDoSMCollectionPath"), this::handleSMCollection)) {
                finishOneFlag = true;
            }
            // 处理宠物购买任务
            if (!finishOneFlag && handleTask(hwnd, smDoList.get("toDoSMchongwuPath"), this::handleSMChongwu)) {
                finishOneFlag = true;
            }
            // 处理灵虚裂痕任务
            if (!finishOneFlag && handleTask(hwnd, smDoList.get("toDoSMLingxuPath"), this::completeTask)) {
                finishOneFlag = true;
            }
            // 处理挑战首席任务
            if (!finishOneFlag && handleTask(hwnd, smDoList.get("toDoSMShouxiPath"), this::completeTask)) {
                finishOneFlag = true;
            }
            // 处理援助同门任务
            if (!finishOneFlag && handleTask(hwnd, smDoList.get("toDoSMYuanzuPath"), this::completeTask)) {
                finishOneFlag = true;
            }

            if (!finishOneFlag) {
                log.warn("未能找到任何匹配的师门任务图标");
                break;
            }
        }
        log.info("师门任务已完成");
    }

    /**
     * 通用任务处理方法
     */
    private boolean handleTask(WinDef.HWND hwnd, String taskIconPath, TaskHandler taskHandler) throws InterruptedException {
        Point point = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), taskIconPath, 0.8, null);
        if (point == null) {
            return false;
        }
        log.info("师门任务执行中：{}", taskIconPath);
        return taskHandler.handle(hwnd);
    }

    /**
     * 处理物质收集任务
     */
    private boolean handleSMCollection(WinDef.HWND hwnd) throws InterruptedException {
        while (true) {
            Point smFinishPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), smDoList.get("smFinishReplyIconPath"), 0.8, null);
            if (ObjectUtil.isNotEmpty(smFinishPoint)) {
                Point replyPoint = OpencvUtils2.findColorCoordinate(CaptureUtils.captureWindow(hwnd), (int) smFinishPoint.x, (int) (smFinishPoint.y + 5), 150, 15, new Scalar(254, 254, 0));
                if (ObjectUtil.isNotEmpty(replyPoint)) {
                    WindowsUtils.sendMouseClick(hwnd, (int) replyPoint.x, (int) replyPoint.y, 1);
                    return waitForSubmissionCompletion(hwnd);
                }
            } else {
                handleCollectionPurchase(hwnd);
                return false;
            }
        }
    }

    /**
     * 处理物质购买
     */
    private void handleCollectionPurchase(WinDef.HWND hwnd) {
        WindowsUtils.sendMouseClick(hwnd, 22, 307, 1); // 打开交易中心
        WindowsUtils.sendMouseClick(hwnd, 566, 620, 3); // 点击购买
        WindowsUtils.sendRightMouseClick(hwnd, 170, 260, 1); // 关闭弹窗
    }

    /**
     * 等待提交完成
     */
    private boolean waitForSubmissionCompletion(WinDef.HWND hwnd) throws InterruptedException {
        int elapsedTime = 0;
        int checkInterval = 10000; // 每10秒检测一次
        int timeout = 1000 * 60 * 5; // 5分钟超时

        while (elapsedTime < timeout) {
            TimeUnit.MILLISECONDS.sleep(checkInterval);
            elapsedTime += checkInterval;

            Point commitTaskIconPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), smDoList.get("commitTaskIconPath"), 0.8, null);
            if (ObjectUtil.isNotEmpty(commitTaskIconPoint)) {
                WindowsUtils.sendMouseClick(hwnd, (int) commitTaskIconPoint.x, (int) commitTaskIconPoint.y, 1);
                handleCollectionConfirmation(hwnd);
                return true;
            } else {
                Point endPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), smDoList.get("popUpIconPath"), 0.8, null);
                if (endPoint != null) {
                    WindowsUtils.sendMouseClick(hwnd, (int) endPoint.x, (int) endPoint.y, 1);
                    return true;
                }
            }
            log.info("物质未提交师门，已等待时间：" + (elapsedTime / 1000) + "秒");
        }
        log.info("物质已提交师门，默认结束");
        return false;
    }

    /**
     * 处理物质提交确认
     */
    private void handleCollectionConfirmation(WinDef.HWND hwnd) throws InterruptedException {
        Point commitCollectSMPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), smDoList.get("commitCollectSMPath"), 0.8, null);
        if (ObjectUtil.isNotEmpty(commitCollectSMPoint)) {
            WindowsUtils.sendMouseClick(hwnd, (int) commitCollectSMPoint.x, (int) commitCollectSMPoint.y, 1);
        }

        Point givePopUpsPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), smDoList.get("givePopUpsIconPath"), 0.8, null);
        if (ObjectUtil.isNotEmpty(givePopUpsPoint)) {
            List<Point> collectionPoints = OpencvUtils2.findAllOneImgsXY(CaptureUtils.captureWindow(hwnd), giveWuziImgPathList, 0.8);
            if (collectionPoints != null) {
                for (Point p : collectionPoints) {
                    WindowsUtils.sendMouseClick(hwnd, (int) p.x, (int) p.y, 1);
                }
            }
            Point quedingIconPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), smDoList.get("quedingIconPath"), 0.8, null);
            WindowsUtils.sendMouseClick(hwnd, (int) quedingIconPoint.x, (int) quedingIconPoint.y, 1);

            Point endPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), smDoList.get("popUpIconPath"), 0.8, null);
            if (endPoint != null) {
                WindowsUtils.sendMouseClick(hwnd, (int) endPoint.x, (int) endPoint.y, 1);
            }
        }
    }

    /**
     * 处理宠物购买任务
     */
    private boolean handleSMChongwu(WinDef.HWND hwnd) throws InterruptedException {
        log.info("师门任务执行中：宠物购买");
        WindowsUtils.sendMouseClick(hwnd, 820, 60, 1); // 点击宠物头像位置
        WindowsUtils.sendMouseClick(hwnd, 460, 350, 3); // 下拉最底下
        WindowsUtils.sendMouseClick(hwnd, 350, 350, 1); // 点击获取更多宠物
        WindowsUtils.sendMouseClick(hwnd, 700, 570, 1); // 点击信誉购买
        return completeTask(hwnd);
    }


    /**
     * 任务是否已完成
     */
    private boolean completeTask(WinDef.HWND hwnd) throws InterruptedException {
        int elapsedTime = 0;
        int checkInterval = 10000; // 每10秒检测一次
        int timeout = 1000 * 60 * 5; // 5分钟超时

        while (elapsedTime < timeout) {
            TimeUnit.MILLISECONDS.sleep(checkInterval);
            elapsedTime += checkInterval;

            Point endPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), smDoList.get("popUpIconPath"), 0.8, null);
            if (endPoint != null) {
                Point zhandouIngPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), smDoList.get("zhandouchangdiPath"), 0.8, null);
                if (zhandouIngPoint == null) {
                    //WindowsUtils.sendMouseClick(hwnd, (int) endPoint.x, (int) endPoint.y, 1);
                    WindowsUtils.sendKeyEvent(hwnd, 0x1B); // 0x1B 是 ESC 键的虚拟键代码
                    log.info("任务已结束");
                    return true;
                }
            }
            log.info("任务进行中，已等待时间：" + (elapsedTime / 1000) + "秒");
        }
        log.info("任务超时，默认结束");
        return false;
    }
}
