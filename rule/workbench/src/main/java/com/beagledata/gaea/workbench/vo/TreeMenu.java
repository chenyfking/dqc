package com.beagledata.gaea.workbench.vo;

import java.util.List;

/**
 * 菜单树——菜单
 * Created by Cyf on 2020/6/12
 **/
public class TreeMenu {
    private String id;
    private String label;
    private String icon;
    private List<TreeData> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<TreeData> getChildren() {
        return children;
    }

    public void setChildren(List<TreeData> children) {
        this.children = children;
    }
}