package com.claw.common.tool;

/**
 * @author Sakura
 * @date 2024/8/30 10:28
 */
public class HaversineCalculator {

    private static final double EARTH_RADIUS = 6371000; // 地球半径，单位：米

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将纬度和经度从度转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // 计算纬度和经度差值
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // 使用Haversine公式计算距离
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 计算并返回距离
        return EARTH_RADIUS * c;
    }

    public static void main(String[] args) {
        // 示例：计算北京（纬度：39.9042，经度：116.4074）和上海（纬度：31.2304，经度：121.4737）之间的距离
        double distance = calculateDistance(39.9042, 116.4074, 31.2304, 121.4737);
        System.out.println("Distance between Beijing and Shanghai: " + distance + " meters");
    }
}
