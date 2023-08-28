package com.beagledata.gaea.workbench.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.AiModel;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by liulu on 2018/1/16.
 */
public interface AiModelService {
    /**
     * 初始化模型方法Spring Bean
     */
    void initModelAction();

    /**
     * @author liulu
     * 2018/1/17 14:39
     */
    void add(AiModel model);

    /**
     * @author liulu
     * 2018/1/17 13:54
     */
    void edit(AiModel model);
    
    /**
     * @author liulu
     * 2018/1/17 14:28
     */
    Result delete(String uuid);

    /**
     * @author liulu
     * 2018/1/17 14:31
     */
    void enable(String uuid);

    /**
     * @author liulu
     * 2018/1/17 14:33
     */
    void disable(String uuid);

    /**
     * @author liulu
     * 2018/1/17 14:37
     */
    Result listAll(int page, int pageNum, AiModel aiModel, boolean isAll, String sortField, String sortDirection);

    /**
     * 根据modelname调用模型
     * @author liulu
     * 2018/5/23 下午 05:15
     */
    JSONArray predictByModelName(String json);

    /**
     * 根据jarName 调用模型
     * Created by chenyafeng on 2019/06/27
     */
    JSONArray predictByJarName(JSONObject jsonObject);

    /**
     * 下载模型
     * @author chenyafeng
     * @date 2018/6/25
     */
    Result downModel(String uuid, HttpServletResponse response);

    /**
     * 模型在线发布到决策引擎
     * @author chenyafeng
     * @date 2018/9/29
     */
    Result apiPublish(MultipartFile file, String params);

    /**
     * 查询ai模型关联的服务
     * @author chenyafeng
     * @date 2018/12/18
     */
    Result microForModel(String modelUuid);

    /**
     * @Author: mahongfei
     * @description: 获取pmml模型文件地址
     */
    String path ();

    Result downModelParams(String uuid, HttpServletResponse response);

}
