package com.clover.sandbox.matrixman;

import java.io.File;

public class FileManip {
    static File GetFileOrNull(String path) {
        File f = new File(path.replaceAll("~", System.getProperty("user.home").toString()));

        if (!f.exists()) {
            return null;
        } else {
            return f;
        }
    }
}
