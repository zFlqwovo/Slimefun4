package com.xzavier0722.mc.plugin.slimefun4.storage.util;

import java.io.File;

public class FileUtils {
    public static boolean checkDirectoryExists(File dir) {
        return dir.exists() && dir.isDirectory() && dir.listFiles() != null && dir.listFiles().length > 0;
    }
}
