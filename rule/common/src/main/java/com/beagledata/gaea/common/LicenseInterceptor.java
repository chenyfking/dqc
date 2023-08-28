package com.beagledata.gaea.common;

import com.beagledata.license.License;
import com.beagledata.license.LicenseException;
import com.beagledata.license.LicenseTools;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * License拦截器
 * Created by Cyf on 2019/12/23
 **/
public class LicenseInterceptor implements HandlerInterceptor {
	/**
	 * 检查频率，10分钟
	 */
	private static long VERIFY_FREQ = 10 * 60 * 1000;

	private String licensePath;
	/**
	 * 上一次检查时间
	 */
	private long lastVerifyTime;

	public LicenseInterceptor(String licensePath) {
		this.licensePath = licensePath;
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
//		validateLicense();
		return true;
	}

	public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
	}

	public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
	}

	/**
	 * 校验服务器license是否有效
	 *
	 * @throws LicenseException
	 */
	public void validateLicense() throws LicenseException {
		if (System.currentTimeMillis() - lastVerifyTime <= VERIFY_FREQ) { // 10分钟检查一次
			return;
		}

		try {
			LicenseTools licenseTools = new LicenseTools();
			License license = licenseTools.getLicense(licensePath);
			license.verify();
			lastVerifyTime = System.currentTimeMillis();
		} catch (LicenseException le) {
			throw le;
		} catch (Exception e) {
			throw new LicenseException("license校验出错");
		}
	}
}
