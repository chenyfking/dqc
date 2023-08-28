package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

import java.util.List;

/**
 * Created by mahongfei on 2018/11/27.
 */
public class DirTree extends BaseEntity {
     //目录节点id
    private String treeId;

    //目录名
    private String treename;

    //目录父节点id
    private String parentId;

    //目录类型，是文件还是目录
    private DataTreeType dataTreeType;

    //子节点对象
    private List<DirTree> children;

    //子文件
    private List<Assets> assetsList;

    //是否被锁定
    private boolean locked;

    //返回类型
    public enum DataTreeType {
        DIR,
        FILE;
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

    public DataTreeType getDataTreeType() {
        return dataTreeType;
    }

    public void setDataTreeType(DataTreeType dataTreeType) {
        this.dataTreeType = dataTreeType;
    }

    public List<DirTree> getChildren() {
        return children;
    }

    public void setChildren(List<DirTree> children) {
        this.children = children;
    }

    public List<Assets> getAssetsList() {
        return assetsList;
    }

    public void setAssetsList(List<Assets> assetsList) {
        this.assetsList = assetsList;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public String toString() {
        return "DirTree{" +
                "treeId='" + treeId + '\'' +
                ", treename='" + treename + '\'' +
                ", parentId='" + parentId + '\'' +
                ", dataTreeType=" + dataTreeType +
                ", children=" + children +
                ", assetsList=" + assetsList +
                ", locked=" + locked +
                '}';
    }
}
