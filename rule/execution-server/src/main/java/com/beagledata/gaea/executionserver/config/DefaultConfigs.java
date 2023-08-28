package com.beagledata.gaea.executionserver.config;

import com.beagledata.gaea.common.BaseConfigs;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by liulu on 2019/7/23.
 */

@Component
@ConfigurationProperties(prefix = "config")
public class DefaultConfigs extends BaseConfigs {
    /**
     * 日志相关配置
     */
    private Log log = new Log();

    public static class Log {
        /**
         * 保存频率，单位：ms，默认1000ms
         */
        private int savePerRate = 1000;
        /**
         * 缓存达到100条就保存入库
         */
        private int savePerNum = 100;
        /**
         * 保存线程数量
         */
        private int saveThreadNum = 1;

        public int getSavePerRate() {
            return savePerRate;
        }

        public void setSavePerRate(int savePerRate) {
            this.savePerRate = savePerRate;
        }

        public int getSavePerNum() {
            return savePerNum;
        }

        public void setSavePerNum(int savePerNum) {
            this.savePerNum = savePerNum;
        }

        public int getSaveThreadNum() {
            return saveThreadNum;
        }

        public void setSaveThreadNum(int saveThreadNum) {
            this.saveThreadNum = saveThreadNum;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Log{");
            sb.append("savePerRate=").append(savePerRate);
            sb.append(", savePerNum=").append(savePerNum);
            sb.append(", saveThreadNum=").append(saveThreadNum);
            sb.append('}');
            return sb.toString();
        }
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DefaultConfigs{");
        sb.append(", log=").append(log);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
