package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义函数
 *
 * Created by liulu on 2020/6/10.
 */
public class FunctionDefinition extends BaseEntity {
    private static final long serialVersionUID = 736695232500676599L;

    /**
     * 函数集名称
     */
    private String name;
    /**
     * 具体执行函数的Class对象
     */
    @JsonIgnore
    private String className;
    /**
     * 函数列表
     */
    private List<Method> methods = new ArrayList<>();
    /**
     * 函数列表JSON串
     */
    @JsonIgnore
    private String methodsJson;
    /**
     * 函数代码
     */
    @JsonIgnore
    private String content;
    /**
     * 函数包文件名称
     */
    private String jarName;
    /**
     * 保存jar到磁盘唯一标识: uuid.jar
     */
    @JsonIgnore
    private String fileUuid;
    /**
     * 创建人
     */
    private User creator;

    public FunctionDefinition() {
    }

    public FunctionDefinition(String name, String className) {
        this.name = name;
        this.className = className;
    }

    public FunctionDefinition(String content) {
        this.content = content;
    }


    public void addMethod(Method method) {
        this.methods.add(method);
    }

    public Method getMethodByName(String name) {
        return methods.stream().filter(method -> method.getName().equals(name)).findFirst().orElse(null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public String getMethodsJson() {
        return methodsJson;
    }

    public void setMethodsJson(String methodsJson) {
        this.methodsJson = methodsJson;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getFileUuid() {
        return fileUuid;
    }

    public void setFileUuid(String fileUuid) {
        this.fileUuid = fileUuid;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionDefinition)) return false;

        FunctionDefinition that = (FunctionDefinition) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        return getClassName() != null ? getClassName().equals(that.getClassName()) : that.getClassName() == null;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getClassName() != null ? getClassName().hashCode() : 0);
        return result;
    }

    /**
     * 函数
     */
    public static class Method {
        /**
         * 函数中文名称
         */
        private String name;
        /**
         * 函数名
         */
        private String declare;
        /**
         * 返回类型
         */
        private String returnType;
        /**
         * 参数列表
         */
        private List<Param> params = new ArrayList<>();

        public Method() {
        }

        public Method(String name) {
            this.name = name;
        }

        public void addParam(Param param) {
            this.params.add(param);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDeclare() {
            return declare;
        }

        public void setDeclare(String declare) {
            this.declare = declare;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        public List<Param> getParams() {
            return params;
        }

        public void setParams(List<Param> params) {
            this.params = params;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Method{");
            sb.append("name='").append(name).append('\'');
            sb.append(", declare='").append(declare).append('\'');
            sb.append(", returnType='").append(returnType).append('\'');
            sb.append(", params=").append(params);
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * 参数
     */
    public static class Param {
        /**
         * 参数中文名称
         */
        private String name;
        /**
         * 参数类型
         */
        private String type;

        public Param() {
        }

        public Param(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Param{");
            sb.append("name='").append(name).append('\'');
            sb.append(", type='").append(type).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FunctionDefinition{");
        sb.append("name='").append(name).append('\'');
        sb.append(", className='").append(className).append('\'');
        sb.append(", methods=").append(methods);
        sb.append(", content='").append(content).append('\'');
        sb.append(", creator=").append(creator);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
