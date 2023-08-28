package com.beagledata.gaea.ruleengine.builder;

import com.beagledata.gaea.ruleengine.util.PackageUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by liulu on 2019/1/4.
 */
public class ZipRuleBuilder extends ByteArrayRuleBuilder {
    public static ZipRuleBuilder newBuilder(File file) throws IOException {
        return new ZipRuleBuilder(file);
    }

    private ZipRuleBuilder(File file) throws IOException {
        super(PackageUtils.decrypt(FileUtils.readFileToByteArray(file)));
    }
}
