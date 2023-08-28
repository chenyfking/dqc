package com.beagledata.gaea.ruleengine.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by liulu on 2020/7/14.
 */
public class ZipUtils {
    /**
     * 压缩ZIP
     *
     * @param outputStream
     * @param resources
     * @throws IOException
     */
    public static void compression(OutputStream outputStream, Map<String, byte[]> resources) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            for (Map.Entry<String, byte[]> me : resources.entrySet()) {
                ZipEntry entry = new ZipEntry(me.getKey());
                zos.putNextEntry(entry);
                zos.write(me.getValue());
                zos.closeEntry();
            }
        }
    }

    /**
     * 解压ZIP
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static Map<String, byte[]> decompression(InputStream inputStream) throws IOException {
        Map<String, byte[]> map = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                map.put(entry.getName(), IOUtils.toByteArray(zis));
                zis.closeEntry();
            }
        }
        return map;
    }
}
