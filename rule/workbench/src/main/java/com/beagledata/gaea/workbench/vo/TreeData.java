package com.beagledata.gaea.workbench.vo;

import java.util.List;

/**
 * 菜单树——数据
 * Created by Cyf on 2020/6/12
 **/
public class TreeData {

    private String type;
    private List<TreeDir> list;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<TreeDir> getList() {
        return list;
    }

    public void setList(List<TreeDir> list) {
        this.list = list;
    }
}