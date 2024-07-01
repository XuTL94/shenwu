package com.game.test;

import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class OpenCVTest {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    // 抠图
    @Test
    public void CutoutsBackgroud() {
        // 输入图像路径
        String inputImagePath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\smFinish.png";
        // 输出图像路径
        String outputImagePath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\smFinish1.png";

        // 读取图像
        Mat image = Imgcodecs.imread(inputImagePath, Imgcodecs.IMREAD_UNCHANGED);
        if (image.empty()) {
            System.err.println("无法读取图像: " + inputImagePath);
            return;
        }

        // 将图像转换为HSV格式
        Mat imageHSV = new Mat();
        Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_BGR2HSV);

        // 创建一个黑色背景的图像
        Mat outputImage = new Mat(image.size(), CvType.CV_8UC4, new Scalar(0, 0, 0, 0));

        // 指定颜色的范围（黄色和白色）
        Scalar lowerBoundYellow = new Scalar(20, 100, 100); // 黄色下界
        Scalar upperBoundYellow = new Scalar(30, 255, 255); // 黄色上界

        Scalar lowerBoundWhite = new Scalar(0, 0, 200); // 白色下界
        Scalar upperBoundWhite = new Scalar(180, 25, 255); // 白色上界

        // 创建掩模
        Mat maskYellow = new Mat();
        Core.inRange(imageHSV, lowerBoundYellow, upperBoundYellow, maskYellow);

        Mat maskWhite = new Mat();
        Core.inRange(imageHSV, lowerBoundWhite, upperBoundWhite, maskWhite);

        // 合并掩模
        Mat mask = new Mat();
        Core.bitwise_or(maskYellow, maskWhite, mask);

        // 将掩模应用于原始图像
        Mat imageBGRA = new Mat();
        Imgproc.cvtColor(image, imageBGRA, Imgproc.COLOR_BGR2BGRA);
        imageBGRA.copyTo(outputImage, mask);

        // 保存输出图像
        Imgcodecs.imwrite(outputImagePath, outputImage);

        System.out.println("输出图像已保存: " + outputImagePath);
    }
}
