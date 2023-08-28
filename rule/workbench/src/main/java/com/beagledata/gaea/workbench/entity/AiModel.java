package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * AI模型
 *
 * Created by liulu on 2018/1/17.
 */
public class AiModel extends BaseEntity {
    private static final long serialVersionUID = -8349636509652327584L;

    /**
     * 模型名称
     */
    private String modelName;
    /**
     * jar包名称
     */
    private String jarName;
    /**
     * 是否可用
     */
    private boolean enable;
    /**
     * 模型参数列表
     */
    private String params;
    /**
     * 前端上传的模型文件
     */
    @JsonIgnore
    private MultipartFile formFile;
    /**
     * 存储在磁盘上的模型文件
     */
    private File diskFile;

    public AiModel() {
    }

    public AiModel(String modelName, String jarName) {
        this.modelName = modelName;
        this.jarName = jarName;
    }

    public AiModel(String modelName, MultipartFile formFile) {
        this.modelName = modelName;
        this.formFile = formFile;
    }

    public AiModel(String uuid, String modelName, MultipartFile formFile) {
        super.setUuid(uuid);
        this.modelName = modelName;
        this.formFile = formFile;
    }

    public String getClassName() {
        if (this.jarName.endsWith(".jar")) {
            return this.jarName.substring(0, this.jarName.lastIndexOf('.'));
        }
        return this.jarName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public MultipartFile getFormFile() {
        return formFile;
    }

    public void setFormFile(MultipartFile formFile) {
        this.formFile = formFile;
    }

    public File getDiskFile() {
        return diskFile;
    }

    public void setDiskFile(File diskFile) {
        this.diskFile = diskFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AiModel)) return false;

        AiModel model = (AiModel) o;

        return getModelName() != null ? getModelName().equals(model.getModelName()) : model.getModelName() == null;
    }

    @Override
    public int hashCode() {
        return getModelName() != null ? getModelName().hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AiModel{");
        sb.append("modelName='").append(modelName).append('\'');
        sb.append(", jarName='").append(jarName).append('\'');
        sb.append(", enable=").append(enable);
        sb.append(", params='").append(params).append('\'');
        sb.append(", formFile=").append(formFile);
        sb.append(", diskFile=").append(diskFile);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
