package com.syrup.meteor.util;

import org.bukkit.Location;

public class StringUtil {

    public static String locationToString(Location location) {
        //코틀린 확장함수만 있었어도..ㅜㅜㅜㅜ
        return location == null ? "null" : "{X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ() + " World: " + location.getWorld().getName() + "}";
    }
}
