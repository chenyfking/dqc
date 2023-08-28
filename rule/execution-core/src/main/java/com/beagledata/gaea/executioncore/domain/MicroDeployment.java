package com.beagledata.gaea.executioncore.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 上线配置
 *
 * Created by liulu on 2020/7/13.
 */
public class MicroDeployment {
    /**
     * 上线id
     */
    private String id;
    /**
     * 最大进件量
     */
    private long incomingQuantity;
    /**
     * 到期时间
     */
    private Date expiredTime;
    /**
     * 上线方式
     */
    private DeployType type;
    /**
     * 模型列表
     */
    private List<Model> models = new ArrayList<>();

    /**
     * 优化类型
     */
    public enum DeployType {
        /**
         * 直接上线
         */
        COMMON,
        /**
         * AB测试
         */
        AB_TEST,
        /**
         * 冠军挑战者
         */
        CHAMPION_CHALLENGER
    }

    public static class Model {
        /**
         * 模型id
         */
        private String id;
        /**
         * 冠军挑战者流量分配的比例
         */
        private int percent;
        /**
         * 是否生效模型
         */
        private boolean primary;
        /**
         * 包uuid
         */
        private String pkgUuid;
        /**
         * 包基线
         */
        private Integer pkgBaseline;

        public String getPkgUuid() {
            if (pkgUuid == null) {
                split();
            }
            return pkgUuid;
        }

        public Integer getPkgBaseline() {
            if (pkgBaseline == null) {
                split();
            }
            return pkgBaseline;
        }

        private void split() {
            String[] arr = id.split("_");
            pkgUuid = arr[0];
            if (arr.length > 1) {
                pkgBaseline = Integer.parseInt(arr[1]);
            } else {
                pkgBaseline = 0;
            }
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getPercent() {
            return percent;
        }

        public void setPercent(int percent) {
            this.percent = percent;
        }

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Model)) return false;

            Model model = (Model) o;

            return getId() != null ? getId().equals(model.getId()) : model.getId() == null;
        }

        @Override
        public int hashCode() {
            return getId() != null ? getId().hashCode() : 0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Model{");
            sb.append("id='").append(id).append('\'');
            sb.append(", percent=").append(percent);
            sb.append(", primary=").append(primary);
            sb.append('}');
            return sb.toString();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getIncomingQuantity() {
        return incomingQuantity;
    }

    public void setIncomingQuantity(long incomingQuantity) {
        this.incomingQuantity = incomingQuantity;
    }

    public Date getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Date expiredTime) {
        this.expiredTime = expiredTime;
    }

    public DeployType getType() {
        return type;
    }

    public void setType(DeployType type) {
        this.type = type;
    }

    public List<Model> getModels() {
        return models;
    }

    public void setModels(List<Model> models) {
        this.models = models;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MicroDeployment{");
        sb.append("id='").append(id).append('\'');
        sb.append(", incomingQuantity=").append(incomingQuantity);
        sb.append(", expiredTime=").append(expiredTime);
        sb.append(", type=").append(type);
        sb.append(", models=").append(models);
        sb.append('}');
        return sb.toString();
    }
}
