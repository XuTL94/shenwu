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

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShimenFinishTest {


    private static final Logger log = LoggerFactory.getLogger(ShimenTest.class);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static String quedingIconPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\quedingIcon.png"; // 确定按钮
    static String zhandouchangdiPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\zhandouchangdi.png"; // 战斗场地
    static String commitTaskIconPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\commitTaskIcon.png"; // 手动提交任务标识
    static String jiaoyizhongxinIcon = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\jiaoyizhongxinIcon.png"; // 交易中心已打开标识
    static String collectionIconPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\collectionIcon.png"; // 物质购买标识

    static String popUpIconPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\popUpIcon.png"; // 弹窗标识

    static String smUnFinishPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\smUnFinish.png"; // 师门未完成标识
    static String smFinishPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\smFinish.png"; // 师门已完成标识
    static String smFinishReplyIconPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\smFinishReplyIcon.png"; // 师门已完成标识

    static String toDoSMCollectionPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMCollection.png"; // 去做师门：物质收集
    static String commitCollectSMPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\commitCollectSM.png"; // 去做师门：物质收集手动提交
    static String givePopUpsIconPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\givePopUpsIcon.png"; // 打开给予弹窗

    static String toDoSMchongwuPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMchongwu.png"; // 去做师门：宠物购买
    static String toDoSMLingxuPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMLingxu.png"; // 去做师门：灵虚裂痕
    static String toDoSMShouxiPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMShouxi.png"; // 去做师门：挑战首席
    static String toDoSMYuanzuPath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMYuanzu.png"; // 去做师门：援助同门

    static List<String> giveCollectionList = List.of("E:\\code\\yunpu\\shenwu\\temp\\biaoji\\zhenlujiu.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\babaozhou.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\taxueyanwo.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\shedanjiu.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\zuishengmengsi.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\guihunlu.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMYuanzu.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\xianglaxia.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\yuchanwan.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\changshoumian.png");


    static List<String> smTypeIconList = List.of(
            "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMCollection.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMchongwu.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMLingxu.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMShouxi.png"
            , "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\toDoSMYuanzu.png");


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

        // 判断是否已接收师门任务
        while (ObjectUtil.isNotEmpty(OpencvUtils2.findAllOneImgsXY(CaptureUtils.captureWindow(hwnd), smTypeIconList, 0.8))) {
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
    static boolean ToDoSMCollectionTest(WinDef.HWND hwnd) {


        Point point = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), toDoSMCollectionPath, 0.8, null);

        if (point == null) {
            log.info("未找到师门任务：收集物质");
            return false;
        }
        log.info("师门任务执行中：收集物质");

        boolean finishFlag = true;

        while (finishFlag){
            // 判断是否已完成
            Point smFinishPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), smFinishReplyIconPath, 0.8, null);
            if (ObjectUtil.isNotEmpty(smFinishPoint)) {
                // 获取师门回复寻路坐标
                Point replyPoint = OpencvUtils2.findColorCoordinate(CaptureUtils.captureWindow(hwnd), (int) smFinishPoint.x, (int) (smFinishPoint.y + 5), 150, 15, new Scalar(254, 254, 0));
                // 寻找寻路以及点击
                if (ObjectUtil.isNotEmpty(replyPoint)) {

                    try {
                        WindowsUtils.sendMouseClick(hwnd, (int) replyPoint.x, (int) (replyPoint.y), 1);

                        // 每10s检测是否已完成提交：弹窗关闭标识，如果5分钟没结束，则默认结束
                        int elapsedTime = 0;
                        int checkInterval = 10000; // 每10秒检测一次
                        int timeout = 1000 * 60 * 5; // 5分钟超时
                        boolean isBattleEnded = false;

                        while (elapsedTime < timeout) {

                            TimeUnit.MILLISECONDS.sleep(checkInterval);
                            elapsedTime += checkInterval;

                            // 是否需要手动提交物质

                            Point commitTaskIconPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), commitTaskIconPath, 0.8, null);
                            if(ObjectUtil.isNotEmpty(commitTaskIconPoint)){
                                log.info("提交任务坐标，{} {}",commitTaskIconPoint.x,commitTaskIconPoint.y);
                                WindowsUtils.sendMouseClick(hwnd, (int) commitTaskIconPoint.x, (int) commitTaskIconPoint.y, 1);

                                // 是否需要再次确认收到提交
                                Point commitCollectSMPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), commitCollectSMPath, 0.8, null);
                                if(ObjectUtil.isNotEmpty(commitCollectSMPoint)){
                                    WindowsUtils.sendMouseClick(hwnd, (int) commitCollectSMPoint.x, (int) commitCollectSMPoint.y, 1);
                                }

                                Point givePopUpsPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), givePopUpsIconPath, 0.8, null);
                                if (ObjectUtil.isNotEmpty(givePopUpsPoint)) {
                                    List<Point> collectionPoints = OpencvUtils2.findAllOneImgsXY(CaptureUtils.captureWindow(hwnd), giveCollectionList, 0.8);
                                    if (collectionPoints != null) {
                                        for (Point p : collectionPoints) {
                                            WindowsUtils.sendMouseClick(hwnd, (int) p.x, (int) (p.y), 1);
                                        }
                                    }
                                    Point quedingIconPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), quedingIconPath, 0.8, null);
                                    // 点击确定提交物质
                                    WindowsUtils.sendMouseClick(hwnd, (int) quedingIconPoint.x, (int) quedingIconPoint.y, 1);

                                    Point endPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), popUpIconPath, 0.8, null);
                                    if (endPoint != null) {
                                        WindowsUtils.sendMouseClick(hwnd, (int) endPoint.x, (int) (endPoint.y), 1);
                                        isBattleEnded = true;
                                        finishFlag = false;
                                        log.info("物质已提交师门");
                                        break;
                                    }
                                    break;
                                }
                            }else{
                                Point endPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), popUpIconPath, 0.8, null);
                                if (endPoint != null) {
                                    WindowsUtils.sendMouseClick(hwnd, (int) endPoint.x, (int) (endPoint.y), 1);
                                    isBattleEnded = true;
                                    finishFlag = false;
                                    log.info("物质已提交师门");
                                    break;
                                }
                            }
                            log.info("物质未提交师门，已等待时间：" + (elapsedTime / 1000) + "秒");
                        }

                        if (!isBattleEnded) {
                            log.info("物质已提交师门，默认结束");
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }

            }
            // 未完成则进行购买物质
            else {
                // 打开交易中心 22 307
                WindowsUtils.sendMouseClick(hwnd, 22, 307, 1);
                // 点击购买  坐标
                WindowsUtils.sendMouseClick(hwnd, 566, 620, 3);
                // 右键关闭弹窗
                WindowsUtils.sendRightMouseClick(hwnd, 170, 260, 1);
            }
        }


        return true;
    }


    // 师门类型  宠物购买
    static boolean ToDoSMChongwu(WinDef.HWND hwnd) throws InterruptedException {

        Point point = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), toDoSMchongwuPath, 0.8, null);

        if (point == null) {
            log.info("未找到师门任务：宠物购买");
            return false;
        }
        log.info("师门任务执行中：宠物购买");
        // 点击宠物头像   820  60
        WindowsUtils.sendMouseClick(hwnd, 820, 60, 1);
        // 下拉最底下
        WindowsUtils.sendMouseClick(hwnd, 460, 350, 3);
        // 点击获取更多宠物
        WindowsUtils.sendMouseClick(hwnd, 350, 350, 1);
        // 点击信誉购买
        WindowsUtils.sendMouseClick(hwnd, 700, 570, 1);

        WindowsUtils.sendKeyEvent(hwnd, 0x1B); // 0x1B 是 ESC 键的虚拟键代码

        // 回归师门
        Point smFinishPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), smFinishReplyIconPath, 0.8, null);
        if (ObjectUtil.isNotEmpty(smFinishPoint)) {
            // 获取师门回复寻路坐标
            Point replyPoint = OpencvUtils2.findColorCoordinate(CaptureUtils.captureWindow(hwnd), (int) smFinishPoint.x, (int) (smFinishPoint.y + 5), 150, 15, new Scalar(254, 254, 0));
            // 寻找寻路以及点击
            if (replyPoint != null) {

                WindowsUtils.sendMouseClick(hwnd, (int) replyPoint.x, (int) (replyPoint.y), 1);
                // 每10s检测是否已完成提交：弹窗关闭标识，如果5分钟没结束，则默认结束
                int elapsedTime = 0;
                int checkInterval = 10000; // 每10秒检测一次
                int timeout = 1000 * 60 * 5; // 5分钟超时
                boolean isBattleEnded = false;

                while (elapsedTime < timeout) {
                    TimeUnit.MILLISECONDS.sleep(checkInterval);
                    elapsedTime += checkInterval;
                    Point endPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), popUpIconPath, 0.8, null);
                    if (endPoint != null) {
                        WindowsUtils.sendMouseClick(hwnd, (int) endPoint.x, (int) (endPoint.y), 1);
                        isBattleEnded = true;
                        log.info("宠物已提交师门");
                        break;
                    }
                    log.info("宠物已提交师门，已等待时间：" + (elapsedTime / 1000) + "秒");
                }

                if (!isBattleEnded) {
                    log.info("宠物已提交师门，默认结束");
                }
            }
        }
        return true;
    }

    // 师门类型  灵虚裂痕
    static boolean ToDoSMLingxu(WinDef.HWND hwnd) throws InterruptedException {
        Point point = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), toDoSMLingxuPath, 0.8, null);

        if (point == null) {
            log.info("未找到师门任务：灵虚裂痕");
            return false;
        }
        log.info("师门任务执行中：灵虚裂痕");
        // 点击灵虚裂痕
        WindowsUtils.sendMouseClick(hwnd, (int) point.x, (int) (point.y), 1);

        // 每10s检测是否已经战斗结束：弹窗关闭标识，如果5分钟没结束，则默认结束
        int elapsedTime = 0;
        int checkInterval = 10000; // 每10秒检测一次
        int timeout = 1000 * 60 * 5; // 5分钟超时
        boolean isBattleEnded = false;

        while (elapsedTime < timeout) {



            TimeUnit.MILLISECONDS.sleep(checkInterval);
            elapsedTime += checkInterval;
            Point endPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), popUpIconPath, 0.8, null);
            if (endPoint != null) {

                // 是否在战斗中
                Point zhandouIngPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), zhandouchangdiPath, 0.8, null);
                if (zhandouIngPoint != null) {
                    continue;
                }

                WindowsUtils.sendMouseClick(hwnd, (int) endPoint.x, (int) (endPoint.y), 1);
                isBattleEnded = true;
                log.info("战斗已结束");
                break;
            }
            log.info("战斗进行中，已等待时间：" + (elapsedTime / 1000) + "秒");
        }

        if (!isBattleEnded) {
            log.info("战斗超时，默认结束");
        }

        return true;
    }

    // 师门类型  挑战首席
    static boolean ToDoSMShouxi(WinDef.HWND hwnd) throws InterruptedException {
        String screenImg = CaptureUtils.captureWindow(hwnd);
        Point point = OpencvUtils2.findImgXY(screenImg, toDoSMShouxiPath, 0.8, null);

        if (point == null) {
            log.info("未找到师门任务：挑战首席");
            return false;
        }
        log.info("师门任务执行中：挑战首席");


        // 每10s检测是否已经战斗结束：弹窗关闭标识，如果5分钟没结束，则默认结束
        int elapsedTime = 0;
        int checkInterval = 10000; // 每10秒检测一次
        int timeout = 1000 * 60 * 5; // 5分钟超时
        boolean isBattleEnded = false;

        while (elapsedTime < timeout) {

            // 点击首席战斗
            WindowsUtils.sendMouseClick(hwnd, (int) point.x, (int) (point.y), 1);

            TimeUnit.MILLISECONDS.sleep(checkInterval);
            elapsedTime += checkInterval;
            Point endPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), popUpIconPath, 0.8, null);
            if (endPoint != null) {

                // 是否在战斗中
                Point zhandouIngPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), zhandouchangdiPath, 0.8, null);
                if (zhandouIngPoint != null) {
                    continue;
                }

                WindowsUtils.sendMouseClick(hwnd, (int) endPoint.x, (int) (endPoint.y), 1);
                isBattleEnded = true;
                log.info("战斗已结束");
                break;
            }
            log.info("战斗进行中，已等待时间：" + (elapsedTime / 1000) + "秒");
        }

        if (!isBattleEnded) {
            log.info("战斗超时，默认结束");
        }

        return true;
    }

    // 师门类型  援助同门
    static boolean ToDoSMYuanzu(WinDef.HWND hwnd) throws InterruptedException {
        String screenImg = CaptureUtils.captureWindow(hwnd);
        Point point = OpencvUtils2.findImgXY(screenImg, toDoSMYuanzuPath, 0.8, null);

        if (point == null) {
            log.info("未找到师门任务：援助同门");
            return false;
        }
        log.info("师门任务执行中：援助同门");


        // 点击首席战斗
        WindowsUtils.sendMouseClick(hwnd, (int) point.x, (int) (point.y), 1);

        // 每10s检测是否已经战斗结束：弹窗关闭标识，如果5分钟没结束，则默认结束
        int elapsedTime = 0;
        int checkInterval = 10000; // 每10秒检测一次
        int timeout = 1000 * 60 * 5; // 5分钟超时
        boolean isBattleEnded = false;

        while (elapsedTime < timeout) {
            TimeUnit.MILLISECONDS.sleep(checkInterval);
            elapsedTime += checkInterval;
            Point endPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), popUpIconPath, 0.8, null);
            if (endPoint != null) {

                // 是否在战斗中
                Point zhandouIngPoint = OpencvUtils2.findImgXY(CaptureUtils.captureWindow(hwnd), zhandouchangdiPath, 0.8, null);
                if (zhandouIngPoint != null) {
                    continue;
                }

                WindowsUtils.sendMouseClick(hwnd, (int) endPoint.x, (int) (endPoint.y), 1);
                isBattleEnded = true;
                log.info("战斗已结束");
                break;
            }
            log.info("战斗进行中，已等待时间：" + (elapsedTime / 1000) + "秒");
        }

        if (!isBattleEnded) {
            log.info("战斗超时，默认结束");
        }
        return true;
    }

}
