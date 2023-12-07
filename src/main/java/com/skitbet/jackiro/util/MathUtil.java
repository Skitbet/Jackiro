package com.skitbet.jackiro.util;

public class MathUtil {
    public static Boolean percentageChance(double chance) {
        return Math.random() <= chance;
    }
}
