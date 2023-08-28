package com.beagledata.gaea.workbench.vo;

/**
 * Created by liulu on 2020/11/11.
 */
public class BaseSearchVO {
    /**
     * 页码
     */
    private int page = 1;
    /**
     * 每页数量
     */
    private int pageNum = 20;
    /**
     * 排序字段
     */
    private String sortField;
    /**
     * 排序方向：asc, desc
     */
    private String sortDirection;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BaseSearchVO{");
        sb.append("page=").append(page);
        sb.append(", pageNum=").append(pageNum);
        sb.append(", sortField='").append(sortField).append('\'');
        sb.append(", sortDirection='").append(sortDirection).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
