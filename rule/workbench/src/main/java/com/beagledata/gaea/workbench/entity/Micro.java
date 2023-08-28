package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务实体类
 * Created by Chenyafeng on 2018/12/3.
 */
public class Micro extends BaseEntity {
    private static final long serialVersionUID = 718753098462366851L;

    //知识包uuid
    private String packageUuid;
    //服务名称
    private String name;
    //服务描述
    private String description;
    //分类
    private MicroType type;
    //审批标志
    private String approvalLabel;
    //是否可用
    private Boolean enable;
    /**
     * 知识包名称
     */
    private String packageName;
    /**
     * 项目uuid
     */
    private String projectUuid;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目创建者名称
     */
    private String projectCreatorName;
    /**
     * 发布到集群节点列表
     */
    private List<Client> clients = new ArrayList<>();

    public String getPackageUuid() {
        return packageUuid;
    }

    public void setPackageUuid(String packageUuid) {
        this.packageUuid = packageUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MicroType getType() {
        return type;
    }

    public void setType(MicroType type) {
        this.type = type;
    }

    public String getApprovalLabel() {
        return approvalLabel;
    }

    public void setApprovalLabel(String approvalLabel) {
        this.approvalLabel = approvalLabel;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getProjectUuid() {
        return projectUuid;
    }

    public void setProjectUuid(String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectCreatorName() {
        return projectCreatorName;
    }

    public void setProjectCreatorName(String projectCreatorName) {
        this.projectCreatorName = projectCreatorName;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Micro{");
        sb.append("packageUuid='").append(packageUuid).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", type=").append(type);
        sb.append(", approvalLabel='").append(approvalLabel).append('\'');
        sb.append(", enable=").append(enable);
        sb.append(", packageName='").append(packageName).append('\'');
        sb.append(", projectUuid='").append(projectUuid).append('\'');
        sb.append(", projectName='").append(projectName).append('\'');
        sb.append(", projectCreatorName='").append(projectCreatorName).append('\'');
        sb.append(", clients=").append(clients);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
