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
public class ShimenFinishTest4 {

    private static final Logger log = LoggerFactory.getLogger(ShimenFinishTest3.class);

    // 日程栏任务图片路径列表
    public static Map<String, String> agendaBarImgPathList = new HashMap<>();
    // 公共使用图片路径列表
    public static Map<String, String> commonImgList = new HashMap<>();
    // 给予物资图片路径列表
    public static List<String> giveWuziList = new ArrayList<>();
    // 师门类型集合（判断任务栏是否存在师门任务）
    public static List<String> smTypeImgPathList = new ArrayList<>();
    // 执行师门任务相关集合
    public static Map<String, String> smDoList = new HashMap<>();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // 加载各类图片路径
        loadImages("agendaBar", agendaBarImgPathList);
        loadImages("giveWuzi", giveWuziList);
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
        //String dir = System.getProperty("user.dir") + File.separator + directoryName;
        String dir = "E:\\code\\yunpu\\shenwu\\imgs" + File.separator + directoryName;
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
        String dir = "E:\\code\\yunpu\\shenwu\\imgs" + File.separator + directoryName;
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

    @Test
    void capture() {
        List<WinDef.HWND> windowHandles = WindowsUtils.getWindowHandles("幻唐志 - 二", null);
        WinDef.HWND hwnd = windowHandles.get(0);
        String path = CaptureUtils.captureWindow(hwnd, false);

    }

    @Test
    void capture2() {
        List<WinDef.HWND> windowHandles = WindowsUtils.getWindowHandles("幻唐志 - 二", null);
        WinDef.HWND hwnd = windowHandles.get(0);
        String path = CaptureUtils.captureWindow(hwnd, false);

        /*List<Point> allOneImagesXY = OpencvUtils.findAllOneImagesXY(path, giveWuziList, 0.8, null);
        System.out.println(allOneImagesXY);*/

        String yuanZu = smDoList.get("wuzType");
        Point point = OpencvUtils.findImageXY(path, yuanZu, 0.8, null);
        System.out.println(point);


        /*String popUp = commonImgList.get("popUp");
        Point point = OpencvUtils.findImageXY(path, popUp, 0.8, null);
        System.out.println(point);*/
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
        /*Point agendaBarSmPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd,false), agendaBarImgPathList.get("smUnFinish.png"), 0.8, null);
        if (ObjectUtil.isNotEmpty(agendaBarSmPoint)) {
            // 获取师门任务
            WindowsUtils.sendMouseClick(hwnd, (int) agendaBarSmPoint.x, (int) (agendaBarSmPoint.y), 1);
            // 估测回归师门获取师门所需时间
            TimeUnit.MILLISECONDS.sleep(5000);
        }*/
        /*String path = CaptureUtils.captureWindow(hwnd,false, false);
        String yuanZu = smDoList.get("wuzType");
        Point point = OpencvUtils.findImageXY(path, yuanZu, 0.8, null);
        System.out.println(point);*/
        // 循环执行师门任务，直到所有任务完成
        while (true) {
            // 判断当前是否有师门任务在进行中
            boolean taskInProgress = ObjectUtil.isNotEmpty(OpencvUtils.findAllOneImagesXY(CaptureUtils.captureWindow(hwnd, false), smTypeImgPathList, 0.8, null));
            if (!taskInProgress) {
                log.info("师门任务已全部完成");
                break;
            }

            log.info("师门任务进行中...");
            boolean finishOneFlag = false;

            // 执行各种类型的师门任务
            finishOneFlag = executeSMTask(hwnd, "wuzType", () -> handleCollectionTask(hwnd)) ||
                    executeSMTask(hwnd, "chongWuType", () -> handlePetTask(hwnd)) ||
                    handleSimpleTask(hwnd, "lingXuType", "未找到师门任务：灵虚裂痕", "师门任务执行中：灵虚裂痕", true) ||
                    handleSimpleTask(hwnd, "shouXiType", "未找到师门任务：挑战首席", "师门任务执行中：挑战首席", true) ||
                    handleSimpleTask(hwnd, "yuanZuType", "未找到师门任务：援助同门", "师门任务执行中：援助同门", true);

            if (!finishOneFlag) {
                log.error("未能识别当前任务类型，停止任务执行");
                break;
            }
        }
    }

    /**
     * 执行师门任务的通用方法
     *
     * @param hwnd        游戏窗口句柄
     * @param taskPath    任务路径
     * @param taskHandler 任务处理函数
     * @return 是否成功执行任务
     * @throws InterruptedException
     */
    static boolean executeSMTask(WinDef.HWND hwnd, String taskPath, TaskHandler taskHandler) throws InterruptedException {
        Point point = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd, false), smDoList.get(taskPath), 0.8, null);
        if (ObjectUtil.isEmpty(point)) {
            log.info("未找到任务：" + taskPath);
            return false;
        }
        log.info("任务执行中：" + taskPath);
        //WindowsUtils.sendMouseClick(hwnd, (int) point.x, (int) point.y, 1);
        taskHandler.handle();
        return true;
    }

    /**
     * 师门任务类型：物质收集
     *
     * @param hwnd 游戏窗口句柄
     * @throws InterruptedException
     */
    static void handleCollectionTask(WinDef.HWND hwnd) throws InterruptedException {
        executeTask(hwnd, smDoList.get("smFinishReplyIconPath"), () -> {
            // 打开交易中心
            WindowsUtils.sendMouseClick(hwnd, 22, 307, 1);
            WindowsUtils.sendMouseClick(hwnd, 566, 620, 3);
            WindowsUtils.sendKeyEvent(hwnd, 0x1B);
        }, false);

        checkAndCommitTask(hwnd, () -> {
            // 是否需要手动提交任务
            Point commitTaskIconPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd, false), commonImgList.get("commitTaskIconPath"), 0.8, null);
            if (ObjectUtil.isNotEmpty(commitTaskIconPoint)) {
                WindowsUtils.sendMouseClick(hwnd, (int) commitTaskIconPoint.x, (int) commitTaskIconPoint.y, 1);

                // 是否需要手动点击物质提交
                Point commitCollectSMPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd, false), smDoList.get("commitCollectSMPath"), 0.8, null);
                if (ObjectUtil.isNotEmpty(commitCollectSMPoint)) {
                    WindowsUtils.sendMouseClick(hwnd, (int) commitCollectSMPoint.x, (int) commitCollectSMPoint.y, 1);
                }

                // 打开给予后，选择物质
                Point givePopUpsPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd, false), commonImgList.get("givePopUpsIconPath"), 0.8, null);
                if (ObjectUtil.isNotEmpty(givePopUpsPoint)) {
                    List<Point> collectionPoints = OpencvUtils.findAllOneImagesXY(CaptureUtils.captureWindow(hwnd, false), giveWuziList, 0.8, null);
                    if (ObjectUtil.isNotEmpty(collectionPoints)) {
                        for (Point p : collectionPoints) {
                            WindowsUtils.sendMouseClick(hwnd, (int) p.x, (int) p.y, 1);
                        }
                    }
                    // 物质提交
                    Point quedingIconPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd, false), commonImgList.get("quedingIconPath"), 0.8, null);
                    WindowsUtils.sendMouseClick(hwnd, (int) quedingIconPoint.x, (int) quedingIconPoint.y, 1);
                }
            }
        });
    }

    /**
     * 师门任务类型：宠物购买
     *
     * @param hwnd 游戏窗口句柄
     * @throws InterruptedException
     */
    static void handlePetTask(WinDef.HWND hwnd) throws InterruptedException {
        executeTask(hwnd, smDoList.get("finishReply"), () -> {
            WindowsUtils.sendMouseClick(hwnd, 820, 60, 1);
            WindowsUtils.sendMouseClick(hwnd, 460, 350, 3);
            WindowsUtils.sendMouseClick(hwnd, 350, 350, 1);
            WindowsUtils.sendMouseClick(hwnd, 700, 570, 1);
            WindowsUtils.sendKeyEvent(hwnd, 0x1B);
        }, false);

        checkAndCommitTask(hwnd, () -> {
            Point smFinishPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd, false), smDoList.get("finishReply"), 0.8, null);
            if (ObjectUtil.isNotEmpty(smFinishPoint)) {
                WindowsUtils.sendMouseClick(hwnd, (int) smFinishPoint.x, (int) smFinishPoint.y, 1);
            }
        });
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
        Point point = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd, false), smDoList.get(taskPath), 0.8, null);
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
            Point smFinishPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd, false), finishIconPath, 0.8, null);
            if (ObjectUtil.isNotEmpty(smFinishPoint)) {
                // 如果找到完成任务的图标，则任务已完成
                //Point replyPoint = OpencvUtils.findColorCoordinate(CaptureUtils.captureWindow(hwnd,false), (int) smFinishPoint.x, (int) (smFinishPoint.y + 5), 150, 15, new Scalar(254, 254, 0));
                //if (ObjectUtil.isNotEmpty(replyPoint)) {
                WindowsUtils.sendMouseClick(hwnd, (int) smFinishPoint.x, (int) smFinishPoint.y, 1);

                // 如果是战斗任务，检查战斗是否结束
                if (isFight) {
                    Point zhandouIngPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd, false), commonImgList.get("zhandouchangdiPath"), 0.8, null);
                    if (ObjectUtil.isEmpty(zhandouIngPoint)) {
                        WindowsUtils.sendKeyEvent(hwnd, 0x1B);
                        log.info("任务已结束");
                        return;
                    }
                } else {
                    WindowsUtils.sendKeyEvent(hwnd, 0x1B);
                    return;
                }
                //}
            }

            executionTime += checkInterval;
            TimeUnit.MILLISECONDS.sleep(checkInterval); // 等待下一次检查
        }

        log.error("任务超时: 停止");
        throw new BusinessException("任务超时");
    }

    /**
     * 检查并提交任务的辅助方法
     *
     * @param hwnd             游戏窗口句柄
     * @param commitTaskAction 提交任务的操作
     * @throws InterruptedException
     */
    static void checkAndCommitTask(WinDef.HWND hwnd, Runnable commitTaskAction) throws InterruptedException {
        int executionTime = 0;
        int checkInterval = 10000; // 每10秒检测一次
        int timeout = 300000; // 5分钟超时

        while (executionTime < timeout) {
            commitTaskAction.run();
            executionTime += checkInterval;
            TimeUnit.MILLISECONDS.sleep(checkInterval); // 等待下一次检查
        }
    }

    @FunctionalInterface
    interface TaskHandler {
        void handle() throws InterruptedException;
    }
}
