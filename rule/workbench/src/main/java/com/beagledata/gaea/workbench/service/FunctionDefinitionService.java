package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.FunctionDefinition;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by Cyf on 2019/8/26
 **/
public interface FunctionDefinitionService {
    /**
     * 查询函数集
     *
     * @return
     */
    List<FunctionDefinition> list();

    /**
     * 添加函数集
     *
     * @param src 函数集JAVA源码
     */
    String add(String src);

    /**
     * 编辑函数
     *
     * @param uuid
     * @param src
     * @return
     */
    Result edit(String uuid, String src);

    /**
     * 根据名称获取函数
     *
     * @param name
     * @return
     */
    FunctionDefinition getByName(String name);

    /**
     * 根据类路径获取函数
     *
     * @param className
     * @return
     */
    FunctionDefinition getByClassName(String className);

    /**
     * 上传函数包
     *
     * @param file
     */
    void upload(MultipartFile file);

    /**
     * 判断是否内置函数
     *
     * @param className
     * @return
     */
    boolean isBuildIn(String className);

    /**
     * 删除函数集
     *
     * @param uuid
     */
    Result delete(String uuid);

    /**
     * 根据uuid获取函数集
     *
     * @param uuid
     * @return
     */
    FunctionDefinition getByUuid(String uuid);

    /**
     * 根据uuid下载函数jar包
     *
     * @param uuid
     */
    void downloadByUuid(String uuid, HttpServletResponse response);
}