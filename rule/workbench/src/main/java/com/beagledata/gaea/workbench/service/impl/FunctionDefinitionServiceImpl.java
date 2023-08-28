package com.beagledata.gaea.workbench.service.impl;

import com.alibaba.fastjson.JSON;
import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.ruleengine.annotation.FunctionMethodProperty;
import com.beagledata.gaea.ruleengine.annotation.FunctionProperty;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.ruleengine.function.*;
import com.beagledata.gaea.ruleengine.runtime.ExecutionResult;
import com.beagledata.gaea.ruleengine.util.JavaSourceClassLoader;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.entity.FunctionDefinition;
import com.beagledata.gaea.workbench.mapper.FunctionDefinitionMapper;
import com.beagledata.gaea.workbench.mapper.ReferMapper;
import com.beagledata.gaea.workbench.service.FunctionDefinitionService;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Created by Cyf on 2019/8/26
 **/
@Service
public class FunctionDefinitionServiceImpl implements FunctionDefinitionService {
	private Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * 函数集默认包
	 */
	private static final String DEFAULT_FUNCTION_PACKAGE = "com.beagledata.gaea.ruleengine.function";

	private List<FunctionDefinition> buildinFunctions;

	@Autowired
    private FunctionDefinitionMapper functionDefinitionMapper;
	@Autowired
	private ResourceResolver resourceResolver;
	@Autowired
	private ReferMapper referMapper;

	@PostConstruct
	public void init() {
		buildinFunctions = new ArrayList<>();
		buildinFunctions.add(parseClass(MathFunction.class));
		buildinFunctions.add(parseClass(StringFunction.class));
		buildinFunctions.add(parseClass(DateFunction.class));
		buildinFunctions.add(parseClass(ListFunction.class));
		buildinFunctions.add(parseClass(JsonFunction.class));
	}

    @Override
    public List<FunctionDefinition> list() {
        List<FunctionDefinition> functions = new ArrayList<>(buildinFunctions);
        functionDefinitionMapper.selectAll().forEach(fd -> {
        	FunctionDefinition func = new FunctionDefinition();
			func.setUuid(fd.getUuid());
			func.setCreateTime(fd.getCreateTime());
			func.setUpdateTime(fd.getUpdateTime());
			func.setCreator(fd.getCreator());
			func.setName(fd.getName());
			func.getMethods().addAll(JSON.parseArray(fd.getMethodsJson(), FunctionDefinition.Method.class));
			func.setJarName(fd.getJarName());
			functions.add(func);
		});
        return functions;
    }

    @Override
    public String add(String src) {
		try {
			Class clazz = loadClass(src);
			FunctionDefinition func = parseClass(clazz);
			FunctionDefinition insertFunc = new FunctionDefinition(src);
			insertFunc.setName(func.getName());
			insertFunc.setClassName(func.getClassName());
			insertFunc.setUuid(IdUtils.UUID());
			insertFunc.setCreator(UserHolder.currentUser());
			insertFunc.setMethodsJson(JSON.toJSONString(func.getMethods()));
			functionDefinitionMapper.insert(insertFunc);
			return insertFunc.getUuid();
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuleException e) {
			throw e;
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("函数名称或类路径不能重复");
		} catch (Exception e) {
			logger.error("添加自定义函数失败: {}", src, e);
			throw new RuleException("函数编译失败，请检查代码语法是否正确");
		}
    }

	@Override
	public FunctionDefinition getByName(String name) {
		FunctionDefinition fd = buildinFunctions.stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
		if (fd == null) {
			fd = functionDefinitionMapper.selectByName(name);
			if (fd != null) {
				fd.getMethods().addAll(JSON.parseArray(fd.getMethodsJson(), FunctionDefinition.Method.class));
			}
		}
		return fd;
	}

	@Override
	public FunctionDefinition getByClassName(String className) {
		try {
			return functionDefinitionMapper.selectByClassName(className);
		} catch (Exception e) {
			logger.error("根据ClassName获取自定义函数失败: {}", className, e);
		}
		return null;
	}

	@Override
	@Transactional
	public void upload(MultipartFile file) {
		String filename = file.getOriginalFilename();
		if (!filename.endsWith(".jar")) {
			throw new IllegalArgumentException("不支持的文件格式");
		}

		String fileUuid = IdUtils.UUID();
		File diskFile = new File(resourceResolver.getFunctionPath(), fileUuid + ".jar");
		try {
			file.transferTo(diskFile);
			List<Class> classes = extractClass(diskFile);
			if (buildinFunctions.stream().anyMatch(fd -> classes.stream().anyMatch(cls -> cls.getName().equals(fd.getClassName())))) {
				throw new IllegalArgumentException("类路径不能重复");
			}

			List<FunctionDefinition> funcs = new ArrayList<>(
					classes.stream().map(cls -> parseClass(cls)).filter(func -> func != null).collect(Collectors.toSet())
			);

            if (funcs.isEmpty()) {
            	throw new IllegalArgumentException("函数包里未找到相关函数");
			}

            funcs.forEach(func -> {
            	if (buildinFunctions.stream().anyMatch(fd -> fd.getName().equals(func.getName()))) {
					throw new IllegalArgumentException("函数名称不能重复");
				}
			});

			funcs.forEach(f -> {
				FunctionDefinition insertFunc = new FunctionDefinition();
				insertFunc.setName(f.getName());
				insertFunc.setClassName(f.getClassName());
				insertFunc.setUuid(IdUtils.UUID());
				insertFunc.setCreator(UserHolder.currentUser());
				insertFunc.setJarName(filename);
				insertFunc.setFileUuid(fileUuid);
				insertFunc.setMethodsJson(JSON.toJSONString(f.getMethods()));
				functionDefinitionMapper.insert(insertFunc);
			});
		} catch (IllegalArgumentException e) {
			diskFile.delete();
			throw e;
		} catch (DuplicateKeyException e) {
			diskFile.delete();
			throw new IllegalArgumentException("函数名称或类路径不能重复");
		} catch (Exception e) {
			diskFile.delete();
			logger.error("上传自定义函数JAR包失败: {}", file, e);
			throw new IllegalArgumentException("上传失败");
		}
	}

	@Override
	public boolean isBuildIn(String className) {
		return buildinFunctions.stream().anyMatch(fd -> fd.getClassName().equals(className))
				|| ExecutionResult.class.getName().equals(className);
	}

	@Override
	public Result delete(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			logger.warn("删除失败, uuid为空: {}", uuid);
			throw new IllegalArgumentException("删除失败，uuid为空");
		}

		try {
			FunctionDefinition fd = functionDefinitionMapper.selectByUuid(uuid);
			int referCount = referMapper.countBySubjectUuid(fd.getName());
			if (referCount > 0) {
				return Result.newError().withMsg("函数集中有函数已被引用，无法删除");
			}
			functionDefinitionMapper.delete(uuid);
			return Result.SUCCESS;
		} catch (Exception e) {
			logger.error("删除自定义函数集失败: {}", uuid, e);
			throw new IllegalStateException("删除失败");
		}
	}

	@Override
	public FunctionDefinition getByUuid(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			logger.warn("查询失败[uuid为空]: {}", uuid);
			throw new IllegalArgumentException("函数集不存在");
		}

		try {
			FunctionDefinition definition = functionDefinitionMapper.selectByUuid(uuid);
			if (definition == null) {
				logger.warn("查询失败[函数集不存在], uuid: {}", uuid);
				throw new IllegalArgumentException("函数集不存在");
			}
			return definition;
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error("根据uuid获取自定义函数集失败: {}", uuid, e);
			throw new IllegalStateException("查询失败");
		}
	}

	@Override
	public void downloadByUuid(String uuid, HttpServletResponse response) {
		if (StringUtils.isBlank(uuid)) {
			logger.warn("下载失败[uuid为空]: {}", uuid);
			throw new IllegalArgumentException("函数集不存在");
		}

		try {
			FunctionDefinition definition = functionDefinitionMapper.selectByUuid(uuid);
			if (definition == null) {
				logger.warn("下载失败[函数集不存在], uuid: {}", uuid);
				throw new IllegalArgumentException("函数集不存在");
			}

			File diskFile = SafelyFiles.newFile(resourceResolver.getFunctionPath(), definition.getFileUuid() + ".jar");
			if (!diskFile.exists()) {
				logger.warn("下载失败[函数jar包不存在], uuid: {}, diskFile: {}", uuid, diskFile);
				throw new IllegalArgumentException("函数集不存在");
			}

			response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + definition.getJarName());
            OutputStream os = response.getOutputStream();
            os.write(FileUtils.readFileToByteArray(diskFile));
            os.flush();
            os.close();
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error("根据uuid下载自定义函数JAR包失败: {}", uuid, e);
			throw new IllegalStateException("下载失败");
		}
	}

	private List<Class> extractClass(File file) throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
		List<Class> classes = new ArrayList<>();
		URLClassLoader tmpClassLoader = new URLClassLoader(new URL[] {});
		Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		addURL.setAccessible(true);
		addURL.invoke(tmpClassLoader, file.toURI().toURL());
		JarFile jarFile = new JarFile(file);
		Enumeration e = jarFile.entries();
		while (e.hasMoreElements()) {
			JarEntry jarEntry = (JarEntry) e.nextElement();
			String name = jarEntry.getName();
			if (name.endsWith(".class")) {
				String className = name.substring(0, name.lastIndexOf('.')).replace("/", ".");
				Class clazz = tmpClassLoader.loadClass(className);
				classes.add(clazz);
			}
		}
		return classes;
	}

	private Class loadClass(String src) throws ClassNotFoundException {
		return new JavaSourceClassLoader(src).loadClass();
	}

    private FunctionDefinition doParseClass(Class<?> clazz) {
		if (!clazz.isAnnotationPresent(FunctionProperty.class)) {
			return null;
		}

		FunctionProperty funcAnnotation = clazz.getAnnotation(FunctionProperty.class);
		if (StringUtils.isBlank(funcAnnotation.name())) {
			return null;
		}

		FunctionDefinition func = new FunctionDefinition(funcAnnotation.name(), clazz.getName());
		Map<String, Integer> methodMap = new HashMap<>();
		for (Method method : clazz.getMethods()) {
			if (!method.isAnnotationPresent(FunctionMethodProperty.class)) {
				continue;
			}

			FunctionMethodProperty methodAnnotation = method.getAnnotation(FunctionMethodProperty.class);
			String annotationName = methodAnnotation.name();
			if (StringUtils.isBlank(annotationName)) {
				throw new RuleException("注解FunctionMethodProperty的name没有默认值");
			}
			if (methodMap.get(annotationName) != null) {
				throw new RuleException("注解FunctionMethodProperty的name值不能重复");
			}
			methodMap.put(annotationName, 1);

			FunctionDefinition.Method funcMethod = new FunctionDefinition.Method(methodAnnotation.name());
			funcMethod.setDeclare(method.getName());
			funcMethod.setReturnType(method.getReturnType().getSimpleName());

			Parameter[] parameters = method.getParameters();
			for (int i = 0; i < parameters.length; i++) {
				Parameter parameter = parameters[i];
				String paramType = parameter.getType().getSimpleName();
				funcMethod.addParam(new FunctionDefinition.Param(methodAnnotation.params()[i], paramType));
			}
			func.addMethod(funcMethod);
		}
		return func;
	}

	@Override
	public Result edit(String uuid, String src) {
		try {
			FunctionDefinition oldFunc = functionDefinitionMapper.selectByUuid(uuid);
			Class clazz = loadClass(src);
			FunctionDefinition func = parseClass(clazz);
			String newName = func.getName();
			String oldName = oldFunc.getName();

			// 判断函数集名称是否能修改
			if (!oldName.equals(newName)) {
				int referCount = referMapper.countBySubjectUuid(oldName);
				if (referCount > 0) {
					return Result.newError().withMsg("函数集中有函数被引用，函数集名称[" + oldName + "]无法修改");
				}
			}

			Map<String, FunctionDefinition.Method> oldMethodMap = getMethodMap(oldFunc);
			Map<String, FunctionDefinition.Method> newMethodMap = getMethodMap(func);
			// 遍历每一个函数，判断是否能够被修改、删除
			for (Map.Entry<String, FunctionDefinition.Method> entry : oldMethodMap.entrySet()) {
				String methodName = entry.getKey();
				FunctionDefinition.Method oldMethod = entry.getValue();
				FunctionDefinition.Method newMethod = newMethodMap.get(methodName);

				// 判断函数能否被删除
				if (newMethod == null) {
					int referCount = referMapper.countBySubjectUuidAndChild(oldName, methodName);
					if (referCount > 0) {
						return Result.newError().withMsg("函数[" + methodName + "]被引用，不能删除该函数");
					}
					continue;
				}

				// 判断函数返回类型能否被修改
				if (!oldMethod.getReturnType().equals(newMethod.getReturnType())) {
					int referCount = referMapper.countBySubjectUuidAndChild(oldName, methodName);
					if (referCount > 0) {
						return Result.newError().withMsg("函数[" + methodName + "]被引用，不能修改返回类型");
					}
					continue;
				}

				// 判断函数的参数个数是否一致
				List<FunctionDefinition.Param> oldParams = oldMethod.getParams();
				List<FunctionDefinition.Param> newParams = newMethod.getParams();
				if (oldParams.size() != newParams.size()) {
					int referCount = referMapper.countBySubjectUuidAndChild(oldName, methodName);
					if (referCount > 0) {
						return Result.newError().withMsg("函数[" + methodName + "]被引用，不能修改参数列表");
					}
					continue;
				}

				// 判断每个参数的类型能否改变
				for (int i = 0; i < oldParams.size(); i++) {
					FunctionDefinition.Param oldParam = oldParams.get(i);
					FunctionDefinition.Param newParam = newParams.get(i);
					if (!oldParam.getType().equals(newParam.getType())) {
						int referCount = referMapper.countBySubjectUuidAndChild(oldName, methodName);
						if (referCount > 0) {
							return Result.newError().withMsg("函数[" + methodName + "]被引用，不能修改参数列表");
						}
					}
				}
			}

			FunctionDefinition updateFunc = new FunctionDefinition(src);
			updateFunc.setUuid(uuid);
			updateFunc.setMethodsJson(JSON.toJSONString(func.getMethods()));
			updateFunc.setName(func.getName());
			updateFunc.setClassName(func.getClassName());
			functionDefinitionMapper.update(updateFunc);
			return Result.SUCCESS;
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (RuleException e) {
			throw e;
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("函数名称或类路径不能重复");
		} catch (Exception e) {
			logger.error("编辑自定义函数失败. uuid: {}, src: {}", uuid, src, e);
			throw new IllegalStateException("函数编译失败，请检查代码语法是否正确");
		}
	}

	/**
	 * 获取函数集中函数名称与函数的映射
	 *
	 * @param func
	 * @return
	 */
	private Map<String, FunctionDefinition.Method> getMethodMap(FunctionDefinition func) {
		String methodJson = func.getMethodsJson();
		List<FunctionDefinition.Method> methods = func.getMethods();
		Map<String, FunctionDefinition.Method> methodMap = new HashMap<>();

		if (methods.isEmpty()) {
			if (StringUtils.isBlank(methodJson)) {
				return methodMap;
			}
			methods = JSON.parseArray(methodJson, FunctionDefinition.Method.class);
		}
		if (!methods.isEmpty()) {
			methods.forEach(m -> methodMap.put(m.getName(), m));
		}

		return methodMap;
	}

	private FunctionDefinition parseClass(Class clazz) {
		if (buildinFunctions.stream().anyMatch(fd -> clazz.getName().equals(fd.getClassName()))) {
			throw new IllegalArgumentException("类路径不能重复");
		}

		FunctionDefinition func = doParseClass(clazz);
		if (func == null) {
			throw new RuleException("缺少FunctionProperty注解");
		}
		if (func.getName().length() > 20) {
			throw new IllegalArgumentException("函数名称长度不能超过20个字符");
		}
		if (func.getName().length() > 200) {
			throw new IllegalArgumentException("函数类路径长度不能超过200个字符");
		}
		if (buildinFunctions.stream().anyMatch(fd -> fd.getName().equals(func.getName()))) {
			throw new IllegalArgumentException("函数名称不能重复");
		}

		return func;
	}
}