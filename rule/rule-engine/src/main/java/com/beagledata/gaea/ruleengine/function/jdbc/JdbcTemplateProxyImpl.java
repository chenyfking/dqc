package com.beagledata.gaea.ruleengine.function.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * 描述:
 * jdbc 代理
 *
 * @author zgx
 * @date 2020-09-15
 */
public class JdbcTemplateProxyImpl implements JdbcTemplateProxy{

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplateProxyImpl(DataSource dataSource){
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * @param sql
	 * @param: [sql]
	 * @author: zgx
	 * @date: 2020/9/15
	 * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
	 */
	@Override
	public List<Map<String, Object>> queryForList(String sql) {
		return jdbcTemplate.queryForList(sql);
	}

	/**
	 * 描述:
	 *
	 * @param sql
	 * @param org
	 * @param: [sql, org]
	 * @author: 周庚新
	 * @date: 2020/9/15
	 * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
	 */
	@Override
	public List<Map<String, Object>> queryForList(String sql, Object... args) {
		return jdbcTemplate.queryForList(sql, args);
	}

	@Override
	public int queryCount(String sql) {
		return jdbcTemplate.queryForObject(sql, Integer.class);
	}

	@Override
	public int queryCount(String sql, Object... args) {
		return jdbcTemplate.queryForObject(sql, Integer.class, args);
	}
}