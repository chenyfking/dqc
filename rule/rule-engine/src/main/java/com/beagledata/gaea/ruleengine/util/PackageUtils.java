package com.beagledata.gaea.ruleengine.util;


import java.io.*;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Created by liulu on 2018/10/11.
 */
public class PackageUtils {
    /**
     * jar中的文件路径分隔符
     */
    private static final char SLASH_CHAR = '/';
    /**
     * 包名分隔符
     */
    private static final char DOT_CHAR = '.';
    /**
     * 加密校验码
     */
    private static final String CHECK_CODE = "gaea";

    /**
     * 在当前项目中寻找指定包下的所有类
     *
     * @param packageName 用'.'分隔的包名
     * @param recursion   是否递归搜索
     * @return 该包名下的所有类
     */
    public static List<Class<?>> getClass(String packageName, boolean recursive) throws IOException, URISyntaxException, ClassNotFoundException {
        List<Class<?>> classList = new ArrayList<>();
        //获取当前线程的类装载器中相应包名对应的资源
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> iterator = classLoader.getResources(packageName.replace(DOT_CHAR, '/'));
        while (iterator.hasMoreElements()) {
            URL url = iterator.nextElement();
            String protocol = url.getProtocol();
            List<Class<?>> childClassList = Collections.emptyList();
            switch (protocol) {
                case "file":
                    childClassList = getClassInFile(url, packageName, recursive);
                    break;
                case "jar":
                    childClassList = getClassInJar(url, packageName, recursive);
                    break;
                default:
                    //在某些WEB服务器中运行WAR包时，它不会像TOMCAT一样将WAR包解压为目录的，如JBOSS7，它是使用了一种叫VFS的协议
                    break;
            }
            classList.addAll(childClassList);
        }
        return classList;
    }

    /**
     * 在给定的文件或文件夹中寻找指定包下的所有类
     *
     * @param filePath    包的路径
     * @param packageName 用'.'分隔的包名
     * @param recursive   是否递归搜索
     * @return 该包名下的所有类
     */
    public static List<Class<?>> getClassInFile(String filePath, String packageName, boolean recursive) throws IOException, ClassNotFoundException {
        Path path = Paths.get(filePath);
        return getClassInFile(path, packageName, recursive);
    }

    /**
     * 在给定的文件或文件夹中寻找指定包下的所有类
     *
     * @param url         包的统一资源定位符
     * @param packageName 用'.'分隔的包名
     * @param recursive   是否递归搜索
     * @return 该包名下的所有类
     */
    public static List<Class<?>> getClassInFile(URL url, String packageName, boolean recursive) throws URISyntaxException, IOException, ClassNotFoundException {
        Path path = Paths.get(url.toURI());
        return getClassInFile(path, packageName, recursive);
    }

    /**
     * 在给定的文件或文件夹中寻找指定包下的所有类
     *
     * @param path        包的路径
     * @param packageName 用'.'分隔的包名
     * @param recursive   是否递归搜索
     * @return 该包名下的所有类
     */
    public static List<Class<?>> getClassInFile(Path path, String packageName, boolean recursive) throws IOException, ClassNotFoundException {
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }
        List<Class<?>> classList = new ArrayList<>();
        if (Files.isDirectory(path)) {
            if (!recursive) {
                return Collections.emptyList();
            }
                //获取目录下的所有文件
            Stream<Path> stream = Files.list(path);
            Iterator<Path> iterator = stream.iterator();
            while (iterator.hasNext()) {
                classList.addAll(getClassInFile(iterator.next(), packageName, recursive));
            }
        } else {
            //由于传入的文件可能是相对路径, 这里要拿到文件的实际路径, 如果不存在则报IOException
            path = path.toRealPath();
            String pathStr = path.toString();
            //这里拿到的一般的"aa:\bb\...\cc.class"格式的文件名, 要去除末尾的类型后缀(.class)
            int lastDotIndex = pathStr.lastIndexOf(DOT_CHAR);
            //Class.forName只允许使用用'.'分隔的类名的形式
            String className = pathStr.replace(File.separatorChar, DOT_CHAR);
            //获取包名的起始位置
            int beginIndex = className.indexOf(packageName);
            if (beginIndex == -1) {
                return Collections.emptyList();
            }
            className = lastDotIndex == -1 ? className.substring(beginIndex) : className.substring(beginIndex, lastDotIndex);
            classList.add(Class.forName(className));
        }
        return classList;
    }

    /**
     * 在给定的jar包中寻找指定包下的所有类
     *
     * @param filePath    包的路径
     * @param packageName 用'.'分隔的包名
     * @param recursive   是否递归搜索
     * @return 该包名下的所有类
     */
    public static List<Class<?>> getClassInJar(String filePath, String packageName, boolean recursive) throws IOException, ClassNotFoundException {
        JarFile jar = new JarFile(filePath);
        return getClassInJar(jar, packageName, recursive);
    }

    /**
     * 在给定的jar包中寻找指定包下的所有类
     *
     * @param url         jar包的统一资源定位符
     * @param packageName 用'.'分隔的包名
     * @param recursive   是否递归搜索
     * @return 该包名下的所有类
     */
    public static List<Class<?>> getClassInJar(URL url, String packageName, boolean recursive) throws IOException, ClassNotFoundException {
        JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
        return getClassInJar(jar, packageName, recursive);
    }

    /**
     * 在给定的jar包中寻找指定包下的所有类
     *
     * @param jar         jar对象
     * @param packageName 用'.'分隔的包名
     * @param recursive   是否递归搜索
     * @return 该包名下的所有类
     */
    public static List<Class<?>> getClassInJar(JarFile jar, String packageName, boolean recursive) throws ClassNotFoundException {
        List<Class<?>> classList = new ArrayList<Class<?>>();
        //该迭代器会递归得到该jar底下所有的目录和文件
        Enumeration<JarEntry> iterator = jar.entries();
        while (iterator.hasMoreElements()) {
            //这里拿到的一般的"aa/bb/.../cc.class"格式的Entry或 "包路径"
            JarEntry jarEntry = iterator.nextElement();
            if (!jarEntry.isDirectory()) {
                String name = jarEntry.getName();
                //对于拿到的文件,要去除末尾的.class
                int lastDotClassIndex = name.lastIndexOf(".class");
                if (lastDotClassIndex != -1) {
                    int lastSlashIndex = name.lastIndexOf(SLASH_CHAR);
                    name = name.replace(SLASH_CHAR, DOT_CHAR);
                    if (name.startsWith(packageName)) {
                        if (recursive || packageName.length() == lastSlashIndex) {
                            String className = name.substring(0, lastDotClassIndex);
                            classList.add(Class.forName(className));
                        }
                    }
                }
            }
        }
        return classList;
    }

    /**
     * 解密知识包
     */
    public static File getDecodeZip(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] buf = new byte[1024];
        int length = 0;
        StringBuilder builder = new StringBuilder();
        while((length = fis.read(buf)) != -1) {
            builder.append(new String(buf, 0, length));
        }
        byte[] byteBase64 = Base64.getDecoder().decode(builder.toString());
        file = File.createTempFile(file.getName(), ".TEMPZIP");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(byteBase64);
        fos.close();
        fis.close();
        return file;
    }

    /**
     * 加密字节流
     *
     * @param bytes
     * @return
     * @throws IOException
     */
    public static byte[] encrypt(byte[] bytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            // 写入校验码
            dos.writeUTF(CHECK_CODE);
            // 写入数据流长度
            dos.writeInt(bytes.length);
            // 写入数据流
            dos.write(bytes);
            // base64编码
            bytes = Base64.getEncoder().encode(baos.toByteArray());
            return bytes;
        }
    }

    /**
     * 解密字节流
     *
     * @param bytes
     * @return
     * @throws IOException
     */
    public static byte[] decrypt(byte[] bytes) throws IOException {
        // base64解码
        bytes = Base64.getDecoder().decode(bytes);
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            // 检查校验码
            String checkCode = dis.readUTF();
            if (!CHECK_CODE.equals(checkCode)) {
                throw new IllegalArgumentException("解密出错，校验码不正确");
            }

            // 读取数据流长度
            int len = dis.readInt();

            // 读取数据流并对比长度
            byte[] data = new byte[len];
            if (dis.read(data) != len) {
                throw new IllegalArgumentException("解密出错，数据读取失败");
            }

            return data;
        }
    }
}
