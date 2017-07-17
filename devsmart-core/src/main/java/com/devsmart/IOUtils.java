package com.devsmart;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class IOUtils {

    public interface IOProgress {
        void onProgress(int bytesWritten);
    }

    public static final int DEFAULT_BUFFER_SIZE = 32768;

    public static void pump(InputStream in, OutputStream out) throws IOException {
        pump(in, out, DEFAULT_BUFFER_SIZE, null, true, true);
    }

    public static void pump(InputStream in, OutputStream out,
                            boolean autoCloseInput, boolean autoCloseOutput) throws IOException {
        pump(in, out, DEFAULT_BUFFER_SIZE, null, autoCloseInput, autoCloseOutput);
    }

    public static void pump(InputStream in, OutputStream out, int bufSize, IOProgress progress,
                            boolean autoCloseInput, boolean autoCloseOutput) throws IOException {

        byte[] buffer = new byte[bufSize];
        int bytesRead;

        try {
            while((bytesRead = in.read(buffer, 0, bufSize)) > 0){
                out.write(buffer, 0, bytesRead);
                if(progress != null){
                    progress.onProgress(bytesRead);
                }
            }
        } finally {
            if(autoCloseInput){
                in.close();
            }
            if(autoCloseOutput){
                out.close();
            }
        }
    }

    public static void deleteTree(File dir) {
        File[] files;
        if(dir != null && (files = dir.listFiles()) != null) {
            for(File f : files){
                if(f.isDirectory()){
                    deleteTree(f);
                } else {
                    f.delete();
                }
            }
            dir.delete();
        }
    }

    public static void unzipFile(InputStream zipInputstream, File dir, boolean shouldCloseInputstream) throws IOException {
        if(!dir.exists()){
            dir.mkdirs();
        }

        ZipInputStream zin = new ZipInputStream(zipInputstream);
        try {
            ZipEntry entry = null;
            while ((entry = zin.getNextEntry()) != null) {
                final String name = entry.getName();
                File newFile = new File(dir, name);

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();

                    FileOutputStream fout = new FileOutputStream(newFile);
                    try {
                        byte[] data = new byte[DEFAULT_BUFFER_SIZE];
                        int count;
                        while ((count = zin.read(data, 0, DEFAULT_BUFFER_SIZE)) != -1) {
                            fout.write(data, 0, count);
                        }
                    } finally {
                        fout.close();
                    }
                }
            }
        } finally {
            if(shouldCloseInputstream) {
                zin.close();
            }
        }

    }

}