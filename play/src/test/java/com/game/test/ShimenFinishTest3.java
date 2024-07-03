package com.game.test;

import cn.hutool.core.util.ObjectUtil;
import com.game.core.exception.BusinessException;
import com.game.play.utils.CaptureUtils;
import com.game.play.utils.OpencvUtils;
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

/**
 * 测试类：ShimenFinishTest3
 * 功能：自动完成游戏中的师门任务
 */
public class ShimenFinishTest3 {

    private static final Logger log = LoggerFactory.getLogger(ShimenFinishTest3.class);

    // 日程栏任务图片路径列表
    public static Map<String, String> agendaBarImgPathList = new HashMap<>();
    // 公共使用图片路径列表
    public static Map<String, String> commonImgList = new HashMap<>();
    // 给予物资图片路径列表
    public static List<String> giveWuziImgPathList = new ArrayList<>();
    // 师门类型集合（判断任务栏是否存在师门任务）
    public static List<String> smTypeImgPathList = new ArrayList<>();
    // 执行师门任务相关集合
    public static Map<String, String> smDoList = new HashMap<>();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // 加载各类图片路径
        loadImages("agendaBar", agendaBarImgPathList);
        loadImages("giveWuzi", giveWuziImgPathList);
        loadImages("sm/smType", smTypeImgPathList);
        loadImages("common", commonImgList);
        loadImages("sm", smDoList);
    }

    /**
     * 从指定目录加载图片路径到列表中
     *
     * @param directoryName 目录名
     * @param list          存储路径的列表
     */
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

    /**
     * 从指定目录加载图片路径到映射中
     *
     * @param directoryName 目录名
     * @param map           存储路径的映射
     */
    private static void loadImages(String directoryName, Map<String, String> map) {
        String dir = "E:\\yunpu\\shenwu\\imgs" + File.separator + directoryName;
        try (Stream<Path> paths = Files.list(Paths.get(dir))) {
            map.putAll(paths.filter(Files::isRegularFile)
                    .collect(Collectors.toMap(
                            path -> path.getFileName().toString().replaceFirst("[.][^.]+$", ""),
                            Path::toString)));
        } catch (Exception e) {
            log.error("读取目录失败{}", dir, e);
        }
    }

    /**
     * 测试方法：点击指定位置的鼠标右键
     */
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
     * 自动完成师门任务的方法
     * 该方法会循环检查并完成各类师门任务，直到任务全部完成
     */
    @Test
    void finishSM() throws InterruptedException {
        WinDef.HWND hwnd = ShenwuUtils.getAvailableHwnd();
        if (hwnd == null) {
            log.error("未找到可用的句柄，请先启动游戏");
            return;
        }

        // 首次检查是否有未完成的师门任务
        Point agendaBarSmPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), agendaBarImgPathList.get("smUnFinish.png"), 0.8, null);
        if (ObjectUtil.isNotEmpty(agendaBarSmPoint)) {
            // 获取师门任务
            WindowsUtils.sendMouseClick(hwnd, (int) agendaBarSmPoint.x, (int) (agendaBarSmPoint.y), 1);
            // 估测回归师门获取师门所需时间
            TimeUnit.MILLISECONDS.sleep(5000);
        }

        // 循环执行师门任务，直到所有任务完成
        while (true) {
            // 判断当前是否有师门任务在进行中
            boolean taskInProgress = ObjectUtil.isNotEmpty(OpencvUtils.findAllOneImagesXY(CaptureUtils.captureWindow(hwnd), smTypeImgPathList, 0.8, null));
            if (!taskInProgress) {
                log.info("师门任务已全部完成");
                break;
            }

            log.info("师门任务进行中...");
            boolean finishOneFlag = false;

            // 执行各种类型的师门任务
            finishOneFlag = ToDoSMCollectionTest(hwnd) || ToDoSMChongwu(hwnd) || ToDoSMLingxu(hwnd) || ToDoSMShouxi(hwnd) || ToDoSMYuanzu(hwnd);

            if (!finishOneFlag) {
                log.error("未能识别当前任务类型，停止任务执行");
                break;
            }
        }
    }

    /**
     * 师门任务类型：物质收集
     *
     * @param hwnd 游戏窗口句柄
     * @return 是否成功执行任务
     * @throws InterruptedException
     */
    static boolean ToDoSMCollectionTest(WinDef.HWND hwnd) throws InterruptedException {
        Point point = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), smDoList.get("toDoSMCollectionPath"), 0.8, null);
        if (ObjectUtil.isEmpty(point)) {
            log.info("未找到师门任务：收集物质");
            return false;
        }
        log.info("师门任务执行中：收集物质");

        // 物质购买，并且检测是否购买已完成以及自动寻路
        executeTask(hwnd, smDoList.get("smFinishReplyIconPath"), () -> {
            // 打开交易中心
            WindowsUtils.sendMouseClick(hwnd, 22, 307, 1);
            WindowsUtils.sendMouseClick(hwnd, 566, 620, 3);
            WindowsUtils.sendKeyEvent(hwnd, 0x1B);
        }, false);

        // 是否需要手动提交任务（出现弹窗提示任务提交）
        Point commitTaskIconPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), commonImgList.get("commitTaskIconPath"), 0.8, null);
        if (ObjectUtil.isNotEmpty(commitTaskIconPoint)) {
            WindowsUtils.sendMouseClick(hwnd, (int) commitTaskIconPoint.x, (int) commitTaskIconPoint.y, 1);

            // 点击物质提交
            Point commitCollectSMPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), smDoList.get("commitCollectSMPath"), 0.8, null);
            if (ObjectUtil.isNotEmpty(commitCollectSMPoint)) {
                WindowsUtils.sendMouseClick(hwnd, (int) commitCollectSMPoint.x, (int) commitCollectSMPoint.y, 1);
            }

            // 打开给予后，选择物质
            Point givePopUpsPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), commonImgList.get("givePopUpsIconPath"), 0.8, null);
            if (ObjectUtil.isNotEmpty(givePopUpsPoint)) {
                List<Point> collectionPoints = OpencvUtils.findAllOneImagesXY(CaptureUtils.captureWindow(hwnd), giveWuziImgPathList, 0.8, null);
                if (ObjectUtil.isNotEmpty(collectionPoints)) {
                    for (Point p : collectionPoints) {
                        WindowsUtils.sendMouseClick(hwnd, (int) p.x, (int) p.y, 1);
                    }
                }
                // 物质提交
                Point quedingIconPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), commonImgList.get("quedingIconPath"), 0.8, null);
                WindowsUtils.sendMouseClick(hwnd, (int) quedingIconPoint.x, (int) quedingIconPoint.y, 1);
            }
        }

        log.info("师门任务：收集物质已完成");
        return true;
    }

    /**
     * 师门任务类型：宠物购买
     *
     * @param hwnd 游戏窗口句柄
     * @return 是否成功执行任务
     * @throws InterruptedException
     */
    static boolean ToDoSMChongwu(WinDef.HWND hwnd) throws InterruptedException {
        Point point = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), smDoList.get("toDoSMchongwuPath"), 0.8, null);
        if (ObjectUtil.isEmpty(point)) {
            log.info("未找到师门任务：宠物购买");
            return false;
        }
        log.info("师门任务执行中：宠物购买");

        // 购买宠物以及回复寻路
        executeTask(hwnd, smDoList.get("smFinishReplyIconPath"), () -> {
            WindowsUtils.sendMouseClick(hwnd, 820, 60, 1);
            WindowsUtils.sendMouseClick(hwnd, 460, 350, 3);
            WindowsUtils.sendMouseClick(hwnd, 350, 350, 1);
            WindowsUtils.sendMouseClick(hwnd, 700, 570, 1);
            WindowsUtils.sendKeyEvent(hwnd, 0x1B);
        }, false);

        log.info("师门任务：宠物购买已完成");
        return true;
    }

    /**
     * 师门任务类型：灵虚裂痕
     *
     * @param hwnd 游戏窗口句柄
     * @return 是否成功执行任务
     * @throws InterruptedException
     */
    static boolean ToDoSMLingxu(WinDef.HWND hwnd) throws InterruptedException {
        return handleSimpleTask(hwnd, "toDoSMLingxuPath", "未找到师门任务：灵虚裂痕", "师门任务执行中：灵虚裂痕", true);
    }

    /**
     * 师门任务类型：挑战首席
     *
     * @param hwnd 游戏窗口句柄
     * @return 是否成功执行任务
     * @throws InterruptedException
     */
    static boolean ToDoSMShouxi(WinDef.HWND hwnd) throws InterruptedException {
        return handleSimpleTask(hwnd, "toDoSMShouxiPath", "未找到师门任务：挑战首席", "师门任务执行中：挑战首席", true);
    }

    /**
     * 师门任务类型：援助同门
     *
     * @param hwnd 游戏窗口句柄
     * @return 是否成功执行任务
     * @throws InterruptedException
     */
    static boolean ToDoSMYuanzu(WinDef.HWND hwnd) throws InterruptedException {
        return handleSimpleTask(hwnd, "toDoSMYuanzuPath", "未找到师门任务：援助同门", "师门任务执行中：援助同门", true);
    }

    /**
     * 执行简单的师门任务
     *
     * @param hwnd         游戏窗口句柄
     * @param taskPath     任务路径
     * @param notFoundLog  未找到任务时的日志
     * @param executingLog 执行任务时的日志
     * @param isFight      是否为战斗任务
     * @return 是否成功执行任务
     * @throws InterruptedException
     */
    static boolean handleSimpleTask(WinDef.HWND hwnd, String taskPath, String notFoundLog, String executingLog, boolean isFight) throws InterruptedException {
        Point point = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), smDoList.get(taskPath), 0.8, null);
        if (ObjectUtil.isEmpty(point)) {
            log.info(notFoundLog);
            return false;
        }
        log.info(executingLog);
        WindowsUtils.sendMouseClick(hwnd, (int) point.x, (int) point.y, 1);
        executeTask(hwnd, commonImgList.get("popUpIconPath"), () -> {
        }, isFight);
        return true;
    }

    /**
     * 执行任务的辅助方法
     *
     * @param hwnd           游戏窗口句柄
     * @param finishIconPath 完成任务的图标路径
     * @param taskAction     执行任务的操作
     * @param isFight        是否为战斗任务
     * @throws InterruptedException
     */
    static void executeTask(WinDef.HWND hwnd, String finishIconPath, Runnable taskAction, boolean isFight) throws InterruptedException {
        int executionTime = 0;
        int checkInterval = 10000; // 每10秒检测一次
        int timeout = 300000; // 5分钟超时

        while (executionTime < timeout) {
            // 执行任务操作
            taskAction.run();

            // 检查任务是否完成
            Point smFinishPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), finishIconPath, 0.8, null);
            if (ObjectUtil.isNotEmpty(smFinishPoint)) {
                // 如果找到完成任务的图标，则任务已完成
                Point replyPoint = OpencvUtils.findColorCoordinate(CaptureUtils.captureWindow(hwnd), (int) smFinishPoint.x, (int) (smFinishPoint.y + 5), 150, 15, new Scalar(254, 254, 0));
                if (ObjectUtil.isNotEmpty(replyPoint)) {
                    WindowsUtils.sendMouseClick(hwnd, (int) replyPoint.x, (int) replyPoint.y, 1);

                    // 如果是战斗任务，检查战斗是否结束
                    if (isFight) {
                        Point zhandouIngPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), commonImgList.get("zhandouchangdiPath"), 0.8, null);
                        if (ObjectUtil.isEmpty(zhandouIngPoint)) {
                            WindowsUtils.sendKeyEvent(hwnd, 0x1B);
                            log.info("任务已结束");
                            return;
                        }
                    } else {
                        WindowsUtils.sendKeyEvent(hwnd, 0x1B);
                        return;
                    }
                }
            }

            executionTime += checkInterval;
            TimeUnit.MILLISECONDS.sleep(checkInterval); // 等待下一次检查
        }

        log.error("任务超时: 停止");
        throw new BusinessException("任务超时");
    }
}
