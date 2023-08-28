package com.beagledata.gaea.workbench.rule.define;

import java.util.Objects;

/**
 * Created by liulu on 2020/5/8.
 */
public interface Import {
    /**
     * @return 获取在drl中的import
     */
    String getImport();

    static Import with(String importName) {
        return new ImportImpl(importName);
    }

    class ImportImpl implements Import {
        private String importName;

        public ImportImpl(String importName) {
            this.importName = importName;
        }

        @Override
        public String getImport() {
            return importName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ImportImpl)) return false;

            ImportImpl anImport = (ImportImpl) o;

            return Objects.equals(importName, anImport.importName);
        }

        @Override
        public int hashCode() {
            return importName != null ? importName.hashCode() : 0;
        }
    }
}
