package com.beagledata.gaea.workbench.rule.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.gaea.ruleengine.common.Constants;
import com.beagledata.gaea.workbench.rule.define.Fact;
import org.apache.commons.lang.StringUtils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Chenyafeng on 2019/1/9.
 */
public class FactUtils {
    private static Set<String> javaKeyWords;
    static {
        String keyWords = "abstract,continue,for,new,switch," +
                "assert,default,goto,package,synchronized," +
                "boolean,do,if,private,this," +
                "break,double,implements,protected,throw," +
                "byte,else,import,public,throws," +
                "case,enum,instanceof,return,transient," +
                "catch,extends,int,short,try," +
                "char,final,interface,static,void," +
                "class,finally,long,strictfp,volatile," +
                "const,float,native,super,while," +
                "true,false,null";  //true false null是单独类型标识, 也不能是属性名称
        javaKeyWords = new HashSet<>(Arrays.asList(keyWords.split(",")));
    }
    public static void main(String[] args) {
        System.out.println(javaKeyWords.contains("null".trim()));
    }

    /**
     * 检验数据模型字段是否为java关键字
     * @param contentJson 数据模型content
     * @param correct 字段合法时返回该值
     */
    public static String validateJavaKeyword(String correct, String contentJson) {
        if (com.beagledata.util.StringUtils.isBlank(contentJson)) {
            return correct;
        }
        JSONObject json = JSONObject.parseObject(contentJson);
        JSONArray fields = json.getJSONArray("fields");
        if (fields == null || fields.size() < 1) {
            return correct;
        }
        boolean isKeyWord = false;
//        StringBuilder builder = new StringBuilder("数据模型字段不能是系统关键字");
//        for (int i = 0; i < fields.size(); i++) {
//            JSONObject data = fields.getJSONObject(i);//页面上的 ID 列，是Fact对象的字段名，在json中存在name属性中
//            if (data == null || com.beagledata.util.StringUtils.isBlank(data.getString("name"))) {
//                continue;
//            }
//            String name = data.getString("name");
//            if (javaKeyWords.contains(name.trim())) {
//                isKeyWord = true;
//                builder.append(",").append(name);
//            }
//        }
//        if (isKeyWord) {
//            builder.append(" 是系统关键字, 请修改ID列中这些字段");
//            return builder.toString();
//        }
        for (int i = 0; i < fields.size(); i++) {
            JSONObject data = fields.getJSONObject(i);//页面上的 ID 列，是Fact对象的字段名，在json中存在name属性中
            if (data == null || com.beagledata.util.StringUtils.isBlank(data.getString("name"))) {
                continue;
            }
            String name = data.getString("name").trim();
            if (javaKeyWords.contains(name)) {
                isKeyWord = true;
                name = String.format("%s%s", name, Constants.FIELD_SUFFIX);
                data.put("name", name);
            }
            if (name.contains("-")) {
                isKeyWord = true;
                name = name.replaceAll("-", Constants.FIELD_LINE);
                data.put("name", name);
            }
        }
        if (isKeyWord) {
            return json.toJSONString();
        }
        return correct;
    }

    /**
     * 读取zip包的数据模型java文件并解析为Fact对象
     * @author chenyafeng
     * @date 2019/1/10
     */
    public static List<Fact> getFactFromZip(String zipPath) throws IOException,IllegalArgumentException{
        List<Fact> list = new ArrayList<>();
        File zf = new File(zipPath);
        if (!zf.exists()) {
            throw new IllegalArgumentException("服务文件不存在");
        }
        int zipDot = zipPath.lastIndexOf('.');
        if ((zipDot <=-1) || (zipDot >= (zipPath.length()))) {
            throw new IllegalArgumentException("Fact文件解析出错");
        }
        //解压后的目录
        String dirFilePath =  zipPath.substring(0, zipDot);
        File dirFile = new File(dirFilePath);
        if (!dirFile.exists()) {
            unZip(zf, dirFilePath);
        }
        loopFile(dirFile, list);
        return list;
    }

    /**
     * 遍历文件夹的java文件
     * @author chenyafeng
     * @date 2019/1/11
     */
    private static void loopFile(File file, List<Fact> list) {
        if (file != null) {
            if (file.isDirectory() && file.listFiles() != null) {
                for (File file1 : file.listFiles()) {
                    loopFile(file1, list);
                }
            } else {
                String fileName = file.getName();
                if (fileName.endsWith(".java")) {
                    String allPath = file.getAbsolutePath();
                    int dot = allPath.lastIndexOf('.');
                    if ((dot <=-1) || (dot >= (allPath.length()))) {
                        throw new IllegalArgumentException("Fact文件解析出错");
                    }
                    String allNoSuffixPath = allPath.substring(0, dot);
                    int simpleNameDot = fileName.lastIndexOf('.');
                    if ((simpleNameDot <=-1) || (simpleNameDot >= (fileName.length()))) {
                        throw new IllegalArgumentException("Fact文件解析出错");
                    }
                    String noSuffixName = fileName.substring(0, simpleNameDot);
                    Fact fact = getFactFromJavaFile(file, allNoSuffixPath, noSuffixName);
                    list.add(fact);
                }
            }
        }
    }

    /**
     * 解析java文件，生成Fact对象
     * @param pakagePath java类包路径+类名,如：com.beagledata.gaea.deploy.fact.Fact_496
     * @return
     */
    public static Fact getFactFromJavaFile(File javaFile, String pakagePath, String noSuffixName) {
        if (!javaFile.exists() || StringUtils.isBlank(pakagePath) || StringUtils.isBlank(noSuffixName))
            return null;
        //编译
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        int compilationResult = javac.run(null, null, null, javaFile.getAbsolutePath());
        if (compilationResult != 0)
            throw new IllegalArgumentException("编译失败");
        Class<?> clazz = null;
        try {
            FactClassLoader loader = new FactClassLoader(Fact.DEFAULT_PACKAGE.concat(".").concat(noSuffixName));
            clazz = loader.findClass(pakagePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return null;
    }

    /**
     * 解压zip
     * @author chenyafeng
     * @date 2019/1/11
     */
    public static void unZip(File srcFile, String destDirPath) throws RuntimeException {
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new RuntimeException(srcFile.getPath() + "所指文件不存在");
        }
        // 开始解压
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(srcFile);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                System.out.println("解压" + entry.getName());
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    String dirPath = destDirPath + "/" + entry.getName();
                    File dir = new File(dirPath);
                    dir.mkdirs();
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    File targetFile = new File(destDirPath + "/" + entry.getName());
                    // 保证这个文件的父文件夹必须要存在
                    if(!targetFile.getParentFile().exists()){
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    // 将压缩文件内容写入到这个文件中
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int len;
                    byte[] buf = new byte[2048];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    // 关流顺序，先打开的后关闭
                    fos.close();
                    is.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("unzip error from ZipUtils", e);
        } finally {
            if(zipFile != null){
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 加载Fact_*.java编译后的class
     * @author chenyafeng
     * @date 2019/1/9
     */
    private static final class FactClassLoader extends ClassLoader {
        private String classPakage;
        @Override
        public Class<?> findClass(String name) {
            name = name.replaceAll("\\\\","/");
            String realPath = "file:///".concat(name.replace(".", "/")) + ".class";
            byte[] cLassBytes = null;
            Path path = null;
            try {
                path = Paths.get(new URI(realPath));
                cLassBytes = Files.readAllBytes(path);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
            Class clazz = defineClass(classPakage, cLassBytes, 0, cLassBytes.length);
            return clazz;
        }

        public FactClassLoader (String classPakage) {
            this.classPakage = classPakage;
        }
    }
}
