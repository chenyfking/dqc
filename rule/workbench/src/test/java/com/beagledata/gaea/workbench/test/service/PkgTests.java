package com.beagledata.gaea.workbench.test.service;
import org.codehaus.plexus.util.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public class PkgTests {

    /**
     * 程序会在zip同级目录生成一个新的zip, 新zip文件名字包含(unencode)
     * 将原来zip剪切备份到别的目录，新zip去掉(unencode)后使用即可
     */
    public static void main(String[] args) throws IOException {

    }

    /**
     * sdk调用基线知识包
     */
    public static void testSdkBaseLine() {
        String path = "";

    }

    /**
     * 解密知识包
     * @throws IOException
     */
    public static void decodePkg() throws IOException {
        String filePath = "C:\\Users\\Cyf\\Desktop\\pkg1113.zip";
        File file = new File(filePath);
        File decodeFile = getDecodeZip(file);
        if (decodeFile == null) {
            System.out.println("解密后的临时文件为空");
            return;
        }
        String fileName = file.getName();
        File dest = new File(file.getParent(), fileName.substring(0, fileName.lastIndexOf(".")).concat("(unencode).zip"));
        dest.createNewFile();
        System.out.println("解密后的文件为: " + dest.getAbsolutePath());
        FileUtils.copyFile(decodeFile, dest);   //复制文件内容
        decodeFile.deleteOnExit();
    }

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
}