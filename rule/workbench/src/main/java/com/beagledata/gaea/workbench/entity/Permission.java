package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;
import com.beagledata.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 权限
 *
 * Created by liulu on 2018/1/9.
 */
public class Permission extends BaseEntity {
    private static final long serialVersionUID = -7696562722700151741L;

    /**
     * 权限名称
     */
    private String name;
    /**
     * 资源名称
     */
    @JsonIgnore
    private String resource;
    /**
     * 行为名称
     */
    @JsonIgnore
    private String action;
    /**
     * 资源唯一标识
     */
    @JsonIgnore
    private String identity;

    private String code;

    private String module;

    public Permission() {
    }

    public Permission(String name, String resource, String action, String module) {
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.module = module;
    }

    public Permission(String name, String resource, String action, String identity, String module) {
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.identity = identity;
        this.module = module;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;

        Permission that = (Permission) o;

        if (that.action.equals(this.action) && that.resource.equals(this.resource)
            && ((StringUtils.isBlank(that.identity) && StringUtils.isBlank(this.identity)) || that.identity.equals(this.identity))
        ) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Permission{");
        sb.append(", name='").append(name).append('\'');
        sb.append(", resource='").append(resource).append('\'');
        sb.append(", action='").append(action).append('\'');
        sb.append(", identity='").append(identity).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", module='").append(module).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
