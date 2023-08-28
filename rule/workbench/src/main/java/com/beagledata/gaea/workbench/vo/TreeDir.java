package com.beagledata.gaea.workbench.vo;

import com.beagledata.gaea.workbench.entity.Assets;

import java.util.List;

/**
 * 菜单树——目录
 * Created by Cyf on 2020/6/12
 **/
public class TreeDir {
    private String uuid;
    private String treeId;
    private String treename;
    private String parentId;
    private String dataTreeType;
    private boolean locked;
    private List<TreeDir> children;
    private List<Assets> assetsList;

    public void addTreeDir(TreeDir treeDir) {
        children.add(treeDir);
    }

    public void addAssets(Assets assets) {
        assetsList.add(assets);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTreeId() {
        return treeId;
    }

    public void setTreeId(String treeId) {
        this.treeId = treeId;
    }

    public String getTreename() {
        return treename;
    }

    public void setTreename(String treename) {
        this.treename = treename;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getDataTreeType() {
        return dataTreeType;
    }

    public void setDataTreeType(String dataTreeType) {
        this.dataTreeType = dataTreeType;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public List<TreeDir> getChildren() {
        return children;
    }

    public void setChildren(List<TreeDir> children) {
        this.children = children;
    }

    public List<Assets> getAssetsList() {
        return assetsList;
    }

    public void setAssetsList(List<Assets> assetsList) {
        this.assetsList = assetsList;
    }
}