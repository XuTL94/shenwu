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


    // 完成师门任务
    @Test
    void finishSM() throws InterruptedException {

        WinDef.HWND hwnd = ShenwuUtils.getAvailableHwnd();
        if (hwnd == null) {
            log.error("未找到可用的句柄，请先启动游戏");
            return;
        }

        // 判断师门任务是否已完成
        Point agendaBarSmPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), agendaBarImgPathList.get("smUnFinish.png"), 0.8, null);
        if(ObjectUtil.isNotEmpty(agendaBarSmPoint)){
            // 获取师门任务
            WindowsUtils.sendMouseClick(hwnd, (int) agendaBarSmPoint.x, (int) (agendaBarSmPoint.y), 1);
            // 估测回归师门获取师门所需时间
            TimeUnit.MILLISECONDS.sleep(5000);
        }

        // 判断是否已接收师门任务
        while (ObjectUtil.isNotEmpty(OpencvUtils.findAllOneImagesXY(CaptureUtils.captureWindow(hwnd), smTypeImgPathList, 0.8,null))) {
            log.info("师门任务进行中...");
            boolean finishOneFlag = false;
            // 先判断是否是物质收集
            finishOneFlag = ToDoSMCollectionTest(hwnd);

            // 如果不是物质收集则继续判断是否是购买宠物
            if (!finishOneFlag) {
                finishOneFlag = ToDoSMChongwu(hwnd);
            }
            // 如果不是物质收集则继续判断是否是灵虚裂痕
            if (!finishOneFlag) {
                finishOneFlag = ToDoSMLingxu(hwnd);
            }

            if (!finishOneFlag) {
                finishOneFlag = ToDoSMShouxi(hwnd);
            }

            if (!finishOneFlag) {
                finishOneFlag = ToDoSMYuanzu(hwnd);
            }
        }


        log.info("师门任务已完成");
    }


    // 师门类型  物质收集
    static boolean ToDoSMCollectionTest(WinDef.HWND hwnd) throws InterruptedException {


        Point point = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), smDoList.get("toDoSMCollectionPath"), 0.8, null);

        if (ObjectUtil.isEmpty(point)) {
            log.info("未找到师门任务：收集物质");
            return false;
        }
        log.info("师门任务执行中：收集物质");


        // 每10s检测是否已完成提交：弹窗关闭标识，如果5分钟没结束，则默认结束
        int executionTime = 0;
        int checkInterval = 1000 * 10; // 每10秒检测一次
        int timeout = 1000 * 60 * 5; // 5分钟超时

        while (executionTime < timeout) {

            // 是否已完成  回复师门
            Point smFinishPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), smDoList.get("smFinishReplyIconPath"), 0.8, null);
            // 未完成购买，先进行购买
            if(ObjectUtil.isEmpty(smFinishPoint)){
                // 打开交易中心 22 307
                WindowsUtils.sendMouseClick(hwnd, 22, 307, 1);
                // 点击购买  坐标
                WindowsUtils.sendMouseClick(hwnd, 566, 620, 3);
                // 退出弹窗
                WindowsUtils.sendKeyEvent(hwnd, 0x1B);
            }
            // 已完成购买，寻路提交
            else {
                // 获取师门回复寻路坐标
                Point replyPoint = OpencvUtils.findColorCoordinate(CaptureUtils.captureWindow(hwnd), (int) smFinishPoint.x, (int) (smFinishPoint.y + 5), 150, 15, new Scalar(254, 254, 0));
                if (ObjectUtil.isNotEmpty(replyPoint)) {
                    WindowsUtils.sendMouseClick(hwnd, (int) replyPoint.x, (int) (replyPoint.y), 1);

                    // 是否需要手动提交物质
                    Point commitTaskIconPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), commonImgList.get("commitTaskIconPath"), 0.8, null);
                    if (ObjectUtil.isNotEmpty(commitTaskIconPoint)) {
                        log.info("手动提交物质坐标，{} {}", commitTaskIconPoint.x, commitTaskIconPoint.y);
                        WindowsUtils.sendMouseClick(hwnd, (int) commitTaskIconPoint.x, (int) commitTaskIconPoint.y, 1);

                        // 是否需要再次确认收到提交
                        Point commitCollectSMPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), smDoList.get("commitCollectSMPath"), 0.8, null);
                        if (ObjectUtil.isNotEmpty(commitCollectSMPoint)) {
                            WindowsUtils.sendMouseClick(hwnd, (int) commitCollectSMPoint.x, (int) commitCollectSMPoint.y, 1);
                        }

                        // 是否已打开给予
                        Point givePopUpsPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), commonImgList.get("givePopUpsIconPath"), 0.8, null);
                        if (ObjectUtil.isNotEmpty(givePopUpsPoint)) {
                            List<Point> collectionPoints = OpencvUtils.findAllOneImagesXY(CaptureUtils.captureWindow(hwnd), giveWuziImgPathList, 0.8,null);
                            if (collectionPoints != null) {
                                for (Point p : collectionPoints) {
                                    WindowsUtils.sendMouseClick(hwnd, (int) p.x, (int) (p.y), 1);
                                }
                            }
                            Point quedingIconPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), commonImgList.get("quedingIconPath"), 0.8, null);
                            // 点击确定提交物质
                            WindowsUtils.sendMouseClick(hwnd, (int) quedingIconPoint.x, (int) quedingIconPoint.y, 1);

                            Point endPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), commonImgList.get("popUpIconPath"), 0.8, null);
                            if(ObjectUtil.isNotEmpty(endPoint)){
                                WindowsUtils.sendKeyEvent(hwnd, 0x1B);
                                log.info("物质已提交师门");
                                break;

                            }
                        }
                    } else {
                        Point endPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), commonImgList.get("popUpIconPath"), 0.8, null);
                        if(ObjectUtil.isNotEmpty(endPoint)){
                            WindowsUtils.sendKeyEvent(hwnd, 0x1B);
                            log.info("物质已提交师门");
                            break;
                        }
                    }
                }
            }

            executionTime += checkInterval;
            TimeUnit.MILLISECONDS.sleep(checkInterval);
        }

        log.info("收集物质已提交师门，已等待时间：" + (executionTime / 1000) + "秒");


        return true;
    }


    // 师门类型  宠物购买
    static boolean ToDoSMChongwu(WinDef.HWND hwnd) throws InterruptedException {

        Point point = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), smDoList.get("toDoSMchongwuPath"), 0.8, null);

        if (point == null) {
            log.info("未找到师门任务：宠物购买");
            return false;
        }
        log.info("师门任务执行中：宠物购买");

        // 每10s检测是否已完成提交：弹窗关闭标识，如果5分钟没结束，则默认结束
        int executionTime = 0;
        int checkInterval = 1000 * 10; // 每10秒检测一次
        int timeout = 1000 * 60 * 5; // 5分钟超时

        while (executionTime < timeout) {

            // 是否已完成  回复师门
            Point smFinishPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), smDoList.get("smFinishReplyIconPath"), 0.8, null);
            // 未完成购买，先进行购买
            if(ObjectUtil.isEmpty(smFinishPoint)){
                // 购买宠物
                // 点击宠物头像   820  60
                WindowsUtils.sendMouseClick(hwnd, 820, 60, 1);
                // 下拉最底下
                WindowsUtils.sendMouseClick(hwnd, 460, 350, 3);
                // 点击获取更多宠物
                WindowsUtils.sendMouseClick(hwnd, 350, 350, 1);
                // 点击信誉购买
                WindowsUtils.sendMouseClick(hwnd, 700, 570, 1);
                WindowsUtils.sendKeyEvent(hwnd, 0x1B);
            }
            // 已完成购买，寻路提交
            else{
                // 获取点击寻路坐标
                Point replyPoint = OpencvUtils.findColorCoordinate(CaptureUtils.captureWindow(hwnd), (int) smFinishPoint.x, (int) (smFinishPoint.y + 5), 150, 15, new Scalar(254, 254, 0));
                if (ObjectUtil.isNotEmpty(replyPoint)) {
                    WindowsUtils.sendMouseClick(hwnd, (int) replyPoint.x, (int) (replyPoint.y), 1);
                }
            }

            // 判断是否已完成
            Point endPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), commonImgList.get("popUpIconPath"), 0.8, null);
            if (ObjectUtil.isEmpty(endPoint)) {
                WindowsUtils.sendKeyEvent(hwnd, 0x1B);
                log.info("宠物已提交师门");
                break;
            }
            executionTime += checkInterval;
            TimeUnit.MILLISECONDS.sleep(checkInterval);
        }

        log.info("宠物已提交师门，已等待时间：" + (executionTime / 1000) + "秒");
        return true;
    }

    // 师门类型  灵虚裂痕
    static boolean ToDoSMLingxu(WinDef.HWND hwnd) throws InterruptedException {
        Point point = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), smDoList.get("toDoSMLingxuPath"), 0.8, null);

        if (point == null) {
            log.info("未找到师门任务：灵虚裂痕");
            return false;
        }
        log.info("师门任务执行中：灵虚裂痕");
        // 点击灵虚裂痕
        WindowsUtils.sendMouseClick(hwnd, (int) point.x, (int) (point.y), 1);
        completeTask(hwnd,true);

        return true;
    }

    // 师门类型  挑战首席
    static boolean ToDoSMShouxi(WinDef.HWND hwnd) throws InterruptedException {

        Point point = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), smDoList.get("toDoSMShouxiPath"), 0.8, null);

        if (point == null) {
            log.info("未找到师门任务：挑战首席");
            return false;
        }
        log.info("师门任务执行中：挑战首席");

        // 点击首席  寻路战斗
        WindowsUtils.sendMouseClick(hwnd, (int) point.x, (int) (point.y), 1);
        completeTask(hwnd,true);

        return true;
    }

    // 师门类型  援助同门
    static boolean ToDoSMYuanzu(WinDef.HWND hwnd) throws InterruptedException {

        Point point = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), smDoList.get("toDoSMYuanzuPath"), 0.8, null);
        if (point == null) {
            log.info("未找到师门任务：援助同门");
            return false;
        }
        log.info("师门任务执行中：援助同门");

        // 点击援助  寻路战斗
        WindowsUtils.sendMouseClick(hwnd, (int) point.x, (int) (point.y), 1);
        completeTask(hwnd,true);

        return true;
    }


    /**
     * 任务是否已完成
     * isFight： true战斗场景
     */
    static private boolean completeTask(WinDef.HWND hwnd,boolean isFight) throws InterruptedException {

        int executionTime = 0;
        int checkInterval = 1000 * 10; // 每10秒检测一次
        int timeout = 1000 * 60 * 5; // 5分钟超时

        while (executionTime < timeout) {
            TimeUnit.MILLISECONDS.sleep(checkInterval);
            executionTime += checkInterval;
            // 是否已对话弹窗 （完成之前点击对话会出现，完成后也会出现）
            Point endPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), commonImgList.get("popUpIconPath"), 0.8, null);
            if (ObjectUtil.isNotEmpty(endPoint)) {
                // 是否战斗场景,是否还在战斗过程中
                if(isFight){
                    // 识别到时战斗场景，则还在战斗过程中
                    Point zhandouIngPoint = OpencvUtils.findImageXY(CaptureUtils.captureWindow(hwnd), commonImgList.get("zhandouchangdiPath"), 0.8, null);
                    if (ObjectUtil.isEmpty(zhandouIngPoint)) {
                        WindowsUtils.sendKeyEvent(hwnd, 0x1B); // 0x1B 是 ESC 键的虚拟键代码
                        log.info("任务已结束");
                        return true;
                    }
                    continue;
                }
                WindowsUtils.sendKeyEvent(hwnd, 0x1B);
                return true;
            }
            log.info("任务进行中，已等待时间：" + (executionTime / 1000) + "秒");
        }

        log.info("任务超时: 停止");
        throw new BusinessException("任务超时");
    }
}
