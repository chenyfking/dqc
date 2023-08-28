package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * 角色
 *
 * Created by liulu on 2018/1/9.
 */
public class Role extends BaseEntity {
    private static final long serialVersionUID = 7859736055244244755L;

    /**
     * 角色编码
     */
    private String code;
    /**
     * 角色名称
     */
    private String name;
    private Set<Permission> permissions = new HashSet<>();

    public Role() {
    }

    public Role(String uuid) {
        super.setUuid(uuid);
    }

    public Role(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Role(String code, String name, Set<Permission> permissions) {
        this.code = code;
        this.name = name;
        this.permissions = permissions;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Role{");
        sb.append("code='").append(code).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", permissions=").append(permissions);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
