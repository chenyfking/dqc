package com.beagledata.gaea.batch.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述:
 * 退出码
 *
 * @author 周庚新
 * @date 2020-11-04
 */
public class ExitCode {
	/**
	 * 跑批失败
	 */
	public static final int FAILED = -1;
	/**
	 * 跑批成功
	 */
	public static final int SUCCESS = 0;

	public static final class Command {
		/**
		 * 参数缺少服务
		 */
		public static final int MISSING_MICRO = 1;
		/**
		 * 参数缺少输入表
		 */
		public static final int MISSING_INPUT_TABLE = 2;

		/**
		 * 参数缺少输出表
		 */
		public static final int MISSING_OUTPUT_TABLE = 3;

		/**
		 * 输入表不合法
		 */
		public static final int INVALID_INPUT_TABLE = 4;

		/**
		 * 输出表不合法
		 */
		public static final int INVALID_OUTPUT_TABLE = 5;

		/**
		 * 业务日期不合法
		 */
		public static final int INVALID_BIZ_DATE = 6;
	}

	/**
	 * 输入表不存在
	 */
	public static final int INPUT_TABLE_NOT_EXISTA = 7;
}