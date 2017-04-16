package com.autoopenmoney.util;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by JackShen on 2016/5/21.
 * <p>
 * 本类只用于测试时记录日志。
 * 正式版应该将 SWITCH 设置成false。
 */
public class FileUtil {
    /***
     * 以应用名作为默认日志名
     ***/
    private static String DEFAULT_FILE_NAME = "default.txt";

    /***
     * SWITCH=false 不允许向日志文件中写入；
     * SWITCH=true 允许向日志文件中写入；
     * 详情见 addToFile()。
     */
    //    private static final boolean SWITCH = true;
    private static final boolean SWITCH = false;

    /***
     * 写入以默认文件名为名的日志
     *
     * @param content 日志内容
     */
    public static void writeToLog(String content) {
        writeToLog(content, DEFAULT_FILE_NAME);
    }

    /***
     * 写入日志。
     *
     * @param content  日志内容
     * @param fileName 日志文件名
     */
    public static void writeToLog(String content, final String fileName) {
        if (SWITCH) {
            content += "\n";
            // 判断SD卡是否存在，并且本程序是否拥有SD卡的权限
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                // 获得SD卡的根目录
                File sdCardPath = Environment.getExternalStorageDirectory();

                File file = new File(sdCardPath, fileName);
                // 初始化文件输出流
                FileOutputStream fileOutputStream;
                OutputStreamWriter writer;
                // 以追加模式打开文件输出流(第二个参数设为true为追加模式)
                try {
                    fileOutputStream = new FileOutputStream(file, true);
                    /***
                     * 设置为 UTF-8 编码，防止中文乱码。
                     */
                    writer = new OutputStreamWriter(fileOutputStream, "UTF-8");
                    writer.write(content);
                    /**
                     * 不能忘了关闭文件输出流。
                     */
                    writer.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
