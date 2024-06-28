package com.game.play.utils;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Point;

@Slf4j
public class MathUtils {

    /**
     * 计算两点之间的距离
     * @param p1 点1
     * @param p2 点2
     * @return 距离
     */
    public static double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
}
