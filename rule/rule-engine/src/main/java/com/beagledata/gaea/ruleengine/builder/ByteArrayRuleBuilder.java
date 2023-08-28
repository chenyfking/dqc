package com.beagledata.gaea.ruleengine.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.gaea.ruleengine.aimodel.JarInvoker;
import com.beagledata.gaea.ruleengine.aimodel.PmmlInvoker;
import com.beagledata.gaea.ruleengine.model.RuleFactModel;
import com.beagledata.gaea.ruleengine.thirdapi.ThirdApi;
import com.beagledata.gaea.ruleengine.util.ClassUtils;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.bigdata.bdtm.exceptions.ModelPredictException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by liulu on 2020/7/14.
 */
public class ByteArrayRuleBuilder extends AbstractRuleBuilder {
    protected ZipInputStream zis;

    public static ByteArrayRuleBuilder newBuilder(byte[] bytes) {
        return new ByteArrayRuleBuilder(bytes);
    }

    protected ByteArrayRuleBuilder(byte[] bytes) {
        this.zis = new ZipInputStream(new ByteArrayInputStream(bytes));
    }

    @Override
    public void build() throws Exception {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }

            String name = entry.getName();
            if (name.startsWith(DIR_FUNC)) {
                readFuncJar(name, readZipEntry());
            } else if (name.startsWith(DIR_MODEL)) {
                name = name.substring(name.lastIndexOf('/') + 1);
                if (name.endsWith(".jar")) {
                    readJarModel(name, readZipEntry());
                } else if (name.endsWith(".xml") || name.endsWith("pmml")) {
                    readPmmlModel(name, readZipEntry());
                }
            } else {
                String content = IOUtils.toString(readZipEntry());
                if ("default.conf".equals(name)) {
                    readConf(content);
                } else {
                    kfs.write(name, content);
                }
            }

            zis.closeEntry();
        }
        zis.close();
    }

    private InputStream readZipEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len;
        byte[] bytes = new byte[4096];
        while ((len = zis.read(bytes)) != -1) {
            baos.write(bytes, 0, len);
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private void readConf(String content) {
        JSONObject conf = JSON.parseObject(content);
        if (conf.containsKey("id")) {
            id = conf.getString("id");
        }
        if (conf.containsKey("facts")) {
            ruleFacts.addAll(conf.getJSONArray("facts").toJavaList(RuleFactModel.class));
        }
        if (conf.containsKey("globals")) {
            globals.addAll(conf.getJSONArray("globals").toJavaList(String.class));
        }
        if (conf.containsKey("flowProcessIds")) {
            flowProcessIds.addAll(conf.getJSONArray("flowProcessIds").toJavaList(String.class));
        }
        if (conf.containsKey("modelNames")) {
            modelNames.addAll(conf.getJSONArray("modelNames").toJavaList(String.class));
        }
        if (conf.containsKey("thirdApis")) {
            JSONObject jsonObject = conf.getJSONObject("thirdApis");
            jsonObject.keySet().forEach(id -> thirdApis.put(id, jsonObject.getJSONObject(id).toJavaObject(ThirdApi.class)));
        }
    }

    private void readJarModel(String fileName, InputStream stream) throws InvocationTargetException, IllegalAccessException, IOException, ClassNotFoundException, ModelPredictException, DocumentException, NoSuchFieldException {
        File jarFile = saveFile(fileName, stream);
        JarInvoker invoker = JarInvoker.newInstance(jarFile);
        modelInvokers.put(fileName, invoker);
    }

    private void readPmmlModel(String fileName, InputStream stream) throws IOException, IllegalAccessException, DocumentException, JAXBException, ClassNotFoundException, NoSuchMethodException, SAXException, InvocationTargetException, NoSuchFieldException {
        File pmmlFile = saveFile(fileName, stream);
        PmmlInvoker invoker = PmmlInvoker.newInstance(pmmlFile);
        modelInvokers.put(fileName, invoker);
    }

    private void readFuncJar(String fileName, InputStream stream) throws IOException, InvocationTargetException, IllegalAccessException {
        File funcJar = saveFile(fileName, stream);
        ClassUtils.loadJar(classLoader.getParent(), funcJar);
    }

    private File saveFile(String fileName, InputStream stream) throws IOException {
        String tmpDirPath = System.getProperty("gaea.tmpdir");
        if (null == tmpDirPath) {
            tmpDirPath = System.getProperty("java.io.tmpdir");
        }

        if (null == tmpDirPath) {   //适配sdk调用未设置gaea.tmpdir,  使用系统的临时目录
            File tempFile = File.createTempFile("tmp_pmml", ".modeltmp");
            tmpDirPath = tempFile.getParent();
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
        File tmpDir = SafelyFiles.newFile(tmpDirPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        String ext = fileName.substring(fileName.lastIndexOf('.'));
        File file = SafelyFiles.newFile(tmpDir, RandomStringUtils.randomAlphanumeric(32) + ext);
        FileUtils.writeByteArrayToFile(file, IOUtils.toByteArray(stream));
        return file;
    }
}
