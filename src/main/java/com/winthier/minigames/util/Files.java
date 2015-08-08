package com.winthier.minigames.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Files {
    public static File createTempDir(String prefix, String suffix, File dir) throws IOException {
        final File tmp = File.createTempFile(prefix, suffix, dir);
        if(!tmp.delete()) {
            throw new IOException("Could not delete temp file: " + tmp.getAbsolutePath());
        }
        if(!tmp.mkdir()) {
            throw new IOException("Could not create temp directory: " + tmp.getAbsolutePath());
        }
        return (tmp);
    }

    public static void copyDir(File src, File dst) throws IOException {
        for (File file : src.listFiles()) {
            if (file.isFile()) {
                copyFile(file, new File(dst, file.getName()));
            } else if (file.isDirectory()) {
                File dir = new File(dst, file.getName());
                dir.mkdir();
                copyDir(file, dir);
            }
        }
    }

    public static void copyFile(File src, File dst) throws IOException {
        dst.createNewFile();
        FileChannel in = new FileInputStream(src).getChannel();
        FileChannel out = new FileOutputStream(dst).getChannel();
        out.transferFrom(in, 0, in.size());
        in.close();
        out.close();
    }

    // public static void deleteFile(File file) throws IOException {
    //         if (file.isDirectory()) {
    //                 for (File sub : file.listFiles()) {
    //                         deleteFile(sub);
    //                 }
    //                 file.delete();
    //         } else if (file.isFile()) {
    //                 file.delete();
    //         }
    // }
}
