package com.beagledata.gaea.workbench.util;

import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.regex.Matcher;

/**
 * @Auther: yinrj
 * @Date: 0008 2020/6/8 18:19
 * @Description: 加载.class文件并生成模型
 */
public class UploadFileClassLoader extends ClassLoader {
    private String path;//加载类的路径

    //指定父类加载器
    public UploadFileClassLoader(ClassLoader parent, String path) {
        super(parent);//显示指定父类加载器
        this.path = path;
    }

    public UploadFileClassLoader(String path) {
        super();//让系统类加载器成为该类的父加载器
        this.path = path;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = loadClassData(name);
        return defineClass(null, classBytes, 0, classBytes.length);
    }

    private byte[] loadClassData(String name){
        name = name.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
        String filePath = this.path + name + ".class";
        File file = SafelyFiles.newFile(filePath);
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (Exception e) {
            return null;
        }
    }
}
