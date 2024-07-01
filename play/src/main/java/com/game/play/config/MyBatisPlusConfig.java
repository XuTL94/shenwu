package com.game.play.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {
        // 输入和输出路径
        String inputFilePath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\sm1.png";
        String outputFilePath = "E:\\code\\yunpu\\shenwu\\temp\\biaoji\\result.png";

        // 读取图像
        Mat image = Imgcodecs.imread(inputFilePath);

        if (image.empty()) {
            System.out.println("图片无法加载，请检查路径。");
            return;
        }

        // 将BGR颜色空间转换为HSV颜色空间
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);

        // 设置黄色的HSV阈值范围
        Scalar lowerYellow = new Scalar(25, 100, 100); // HSV lower bound
        Scalar upperYellow = new Scalar(35, 255, 255); // HSV upper bound

        // 进行颜色过滤
        Mat mask = new Mat();
        Core.inRange(hsvImage, lowerYellow, upperYellow, mask);

        // 创建一个带有Alpha通道的结果图像
        Mat result = new Mat(image.size(), CvType.CV_8UC4);

        // 遍历每个像素并设置Alpha通道
        for (int i = 0; i < image.rows(); i++) {
            for (int j = 0; j < image.cols(); j++) {
                double[] pixel = image.get(i, j);
                double[] maskPixel = mask.get(i, j);

                if (maskPixel[0] != 0) {
                    // 保留颜色部分，设置Alpha为255（不透明）
                    result.put(i, j, pixel[0], pixel[1], pixel[2], 255);
                } else {
                    // 其他颜色设置为透明
                    result.put(i, j, 0, 0, 0, 0);
                }
            }
        }

        // 将结果保存到指定路径
        Imgcodecs.imwrite(outputFilePath, result);

        System.out.println("处理完成，结果已保存到：" + outputFilePath);
    }
}