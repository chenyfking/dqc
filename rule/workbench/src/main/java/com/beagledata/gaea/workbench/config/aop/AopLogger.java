package com.beagledata.gaea.workbench.config.aop;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.Logs;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.mapper.LogsMapper;
import com.beagledata.gaea.workbench.util.IpUtils;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.util.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Aspect
@Component
public class AopLogger {
	private final static Logger logger = LoggerFactory.getLogger(AopLogger.class);//参数为当前使用的类名

	private static ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Autowired
	private LogsMapper logsMapper;

	@Pointcut("execution(* com.beagledata.gaea.workbench.controller..*.*(..)) && !execution(* com.beagledata.gaea.workbench.controller.ErrController.*(..))")
	public void cut() {
	}

	@Pointcut("@annotation(com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation)")
	public void cutAnnoation() {
	}

	@Before("cut()")
	public void doBefore(JoinPoint joinPoint) {
	}

	@Around("cut() || cutAnnoation()")
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		RestLogAnnotation restLogAnnotation = getRestLogAnnotation(pjp);
		if (restLogAnnotation == null || StringUtils.isBlank(restLogAnnotation.describe())) {
			//没有注解直接通过
			return pjp.proceed();
		}

		Logs logs = new Logs();
		logs.setBeginTime(new Date());
		HttpServletRequest request = getRequest();
		try {
			Object result = pjp.proceed();
			setBackValue(logs, result);
			logs.setSuccess(true);
			return result;
		} catch (Throwable e) {
			logs.setBackValue(e.toString());
			logs.setSuccess(false);
			throw e;
		} finally {
			saveLog(logs, restLogAnnotation, request, pjp);
		}
	}

	@After("cut()")//无论Controller中调用方法以何种方式结束，都会执行
	public void doAfter() {
	}

	@AfterReturning(returning = "obj", pointcut = "cut()")//在调用上面 @Pointcut标注的方法后执行。用于获取返回值
	public void doAfterReturning(Object obj) {
	}

	private RestLogAnnotation getRestLogAnnotation(ProceedingJoinPoint pjp) throws NoSuchMethodException, SecurityException {
		Class<?> classTarget = pjp.getTarget().getClass();
		Class<?>[] par = ((MethodSignature) pjp.getSignature()).getParameterTypes();
		Method objMethod = classTarget.getMethod(pjp.getSignature().getName(), par);
		return objMethod.getAnnotation(RestLogAnnotation.class);
	}

	private HttpServletRequest getRequest() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return attributes.getRequest();
	}

	private void setBackValue(Logs logs, Object result) {
		if (result == null) {
			return;
		}
		logs.setBackValue(result.toString());
	}

	private void saveLog(Logs logs, RestLogAnnotation restLogAnnotation, HttpServletRequest request, ProceedingJoinPoint pjp) {
		logs.setEndTime(new Date());
		logs.setUseTime((int) (logs.getEndTime().getTime() - logs.getBeginTime().getTime()));
		logs.setOptName(restLogAnnotation.describe());
		User user = UserHolder.currentUser();
		if (user != null) {
			logs.setUser(user.getUuid());
		}
		logs.setClientIp(IpUtils.getRealIp(request));
		logs.setRequestUrl(request.getRequestURI());
		logs.setRequestType(request.getMethod());
		logs.setRequestMethod(pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName());
		logs.setRequestParams(Arrays.asList(pjp.getArgs()).toString());
		executorService.execute(() -> logsMapper.insert(logs));
	}
}
