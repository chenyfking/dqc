package com.beagledata.gaea.workbench.vo;

import com.beagledata.gaea.workbench.entity.AssetsVersion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Auther: yinrj
 * @Date: 0007 2020/7/7 19:19
 * @Description:
 */
@JsonIgnoreProperties(value = {"handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetsReferenceVO implements Serializable {
    private static final long serialVersionUID = -8513807949730927530L;
    /**
	 * 资源文件uuid
	 */
	private String uuid;
	/**
	 * 资源文件类型
	 */
	private String name;
	/**
	 * 资源文件类型
	 */
	private String type;

	/**
	 * 文件描述
	 */
	private String description;
    /**
     * 版本
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<AssetsVersion> assetsVersions;
    /**
     * 引用是规则还是规则模板
     */
    private boolean isTemplate;
    /**
     * 引用是否在回收站
     */
    private boolean recycle;
    /**
     * 引用文件创建时间
     */
    private Date createTime;
    /**
     * 引用版本
     */
    @JsonIgnore
    private Integer versionNo;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<AssetsVersion> getAssetsVersions() {
		return assetsVersions;
	}

	public void setAssetsVersions(List<AssetsVersion> assetsVersions) {
		this.assetsVersions = assetsVersions;
	}

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setTemplate(boolean template) {
        isTemplate = template;
    }

    public boolean isRecycle() {
        return recycle;
    }

    public void setRecycle(boolean recycle) {
        this.recycle = recycle;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AssetsReferenceVO{");
        sb.append("uuid='").append(uuid).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", assetsVersions=").append(assetsVersions);
        sb.append(", isTemplate=").append(isTemplate);
        sb.append(", recycle=").append(recycle);
        sb.append(", createTime=").append(createTime);
        sb.append(", versionNo=").append(versionNo);
        sb.append('}');
        return sb.toString();
    }
}
