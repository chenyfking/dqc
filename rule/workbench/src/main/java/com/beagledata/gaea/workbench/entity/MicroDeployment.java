package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;
import com.beagledata.gaea.workbench.common.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务模型优化
 *
 * Created by liulu on 2020/7/13.
 */
public class MicroDeployment extends BaseEntity {
    private static final long serialVersionUID = 2319472386185022305L;

    /**
     * 所属服务uuid
     */
    private String microUuid;
    /**
     * 最大进件量
     */
    private long incomingQuantity;
    /**
     * 到期时间
     */
    @JsonFormat(pattern = Constants.DEFAULT_DATE_FORMAT, timezone = "GMT+8")
    @DateTimeFormat(pattern = Constants.DEFAULT_DATE_FORMAT)
    private Date expiredTime;
    /**
     * 优化类型
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
         * AB测试
         */
        AB_TEST,
        /**
         * 冠军挑战者
         */
        CHAMPION_CHALLENGER,
        /**
         * 上线
         */
        COMMON
    }

    public static class Model extends BaseEntity {
        /**
         * 知识包基线
         */
        private KnowledgePackageBaseline pkgBaseline;
        /**
         * 冠军挑战者流量分配的比例
         */
        private Integer percent;
        /**
         * 被挑战者
         */
        private boolean primary;

        public KnowledgePackageBaseline getPkgBaseline() {
            return pkgBaseline;
        }

        public void setPkgBaseline(KnowledgePackageBaseline pkgBaseline) {
            this.pkgBaseline = pkgBaseline;
        }

        public Integer getPercent() {
            return percent;
        }

        public void setPercent(Integer percent) {
            this.percent = percent;
        }

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Model{");
            sb.append("pkgBaseline=").append(pkgBaseline);
            sb.append(", percent=").append(percent);
            sb.append(", primary=").append(primary);
            sb.append('}');
            return sb.toString();
        }
    }

    public String getMicroUuid() {
        return microUuid;
    }

    public void setMicroUuid(String microUuid) {
        this.microUuid = microUuid;
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
        sb.append("microUuid='").append(microUuid).append('\'');
        sb.append(", incomingQuantity=").append(incomingQuantity);
        sb.append(", expiredTime=").append(expiredTime);
        sb.append(", type=").append(type);
        sb.append(", models=").append(models);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
