package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.common.Result;
import com.beagledata.gaea.common.RedisConstants;
import com.beagledata.gaea.workbench.entity.Token;
import com.beagledata.gaea.workbench.mapper.TokenMapper;
import com.beagledata.gaea.workbench.service.TokenService;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.gaea.workbench.vo.MicroTokenVO;
import com.beagledata.util.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述: Token service实现
 * @author: 周庚新
 * @date: 2020/6/11
 */
@Service
public class TokenServiceImpl extends BaseServiceImpl implements TokenService {
	@Autowired
	private TokenMapper tokenMapper;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	/**
	 * by yangyongqiang
	 * 2020/06/11
	 * 删除令牌
	 */
	@Override
	@Transactional
	public void delete(String uuid) {
		if (StringUtil.isBlank(uuid)) {
			throw new IllegalArgumentException("令牌uuid不能为空");
		}

		try {
			Token token = tokenMapper.selectByUuid(uuid);
			if (token == null) {
				throw new IllegalArgumentException("令牌不存在");
			}
			tokenMapper.delete(uuid);
			tokenMapper.deleteMicroToken(token.getToken());
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error("删除令牌失败: {} ", uuid, e);
			throw new IllegalStateException("删除令牌失败");
		}
	}

	/**
	 * 描述: 新增令牌
	 * @param: [token]
	 * @author: 周庚新
	 * @date: 2020/6/11
	 * @return: int
	 */
	@Override
	@Transactional
	public void add(String name, String microUuids, boolean all) {
		if (StringUtil.isBlank(name) || name.length() > 40) {
			throw new IllegalArgumentException("令牌名称不能为空并且长度不能超过40个字符");
		}
		Token dbToken = tokenMapper.selectByName(name);
		if (dbToken != null) {
			throw new IllegalArgumentException("令牌名称不能重复");
		}

		Token token = new Token();
		token.setName(name);
		token.setCreatorUuid(UserHolder.currentUserUuid());
		token.setToken(IdUtils.UUID());
		token.setUuid(IdUtils.UUID());
		token.setAll(all);

		try {
			tokenMapper.insert(token);
			if (!all && StringUtils.isNotBlank(microUuids)) {
				tokenMapper.insertBatchMicroToken(
						Arrays.stream(microUuids.split(","))
								.map(microUuid -> new MicroTokenVO(token.getToken(), microUuid))
								.collect(Collectors.toList())
				);
			}
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("令牌名称不能重复");
		} catch (Exception e) {
			logger.error("添加令牌失败. name: {}, microUuids: {}, all: {}", name, microUuids, all, e);
			throw new IllegalStateException("添加失败");
		}
	}

	/**
	 * by yangyongqiang
	 * 2020/06/11
	 * 查询令牌
	 */
	@Override
	public Result listPage(int page, int pageNum, Token token) {
		if (StringUtils.isNotBlank(token.getName())) {
			token.setName("%" + token.getName() + "%");
		}
		if (StringUtils.isNotBlank(token.getCreatorName())) {
			token.setCreatorName("%" + token.getCreatorName() + "%");
		}

		if (page <= 0) {
			page = 1;
		}
		try {
			int count = tokenMapper.countTotal(token);
			if (count > 0) {
				List<Token> list = tokenMapper.selectList(pageNum * (page - 1), pageNum, token);
				return Result.newSuccess().withData(list).withTotal(count);
			}
		} catch (Exception e) {
			logger.error("获取令牌列表失败. page: {}, pageNum: {}, token: {}", page, pageNum, token, e);
		}
		return Result.emptyList();
	}

	/**
	* 描述: 修改令牌以及令牌关联服务
	* @param: [uuid, name, tokenValue, microUuids]
	* @author: 周庚新
	* @date: 2020/6/12
	* @return: void
	*/
	@Override
	@Transactional
	public void edit(String uuid, String name, String microUuids, boolean all) {
		if (StringUtil.isBlank(uuid)) {
			throw new IllegalArgumentException("令牌uuid不能为空");
		}
		if (StringUtil.isBlank(name) || name.length() > 40) {
			throw new IllegalArgumentException("令牌名称不能为空并且长度不能超过40个字符");
		}

		try {
			Token dbToken = tokenMapper.selectByUuid(uuid);
			if (dbToken == null) {
				throw new IllegalArgumentException("令牌不存在");
			}

			Token token = new Token();
			token.setUuid(uuid);
			token.setName(name);
			token.setAll(all);
			// 修改令牌
			tokenMapper.update(token);
			// 删除令牌关联的服务并重新添加
			tokenMapper.deleteMicroToken(dbToken.getToken());
			if (!all && StringUtils.isNotBlank(microUuids)) {
				tokenMapper.insertBatchMicroToken(
						Arrays.stream(microUuids.split(","))
								.map(microUuid -> new MicroTokenVO(dbToken.getToken(), microUuid))
								.collect(Collectors.toList())
				);
			}
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("令牌名称不能重复");
		} catch (Exception e) {
			logger.error("添加令牌失败. uuid: {}, name: {}, microUuids: {}, all: {}", uuid, name, microUuids, all, e);
			throw new IllegalStateException("修改失败");
		}
	}

	/**
	* 描述: 刷新令牌
	* @param: []
	* @author: 周庚新
	* @date: 2020/7/20
	* @return: void
	*
	*/
	@Override
	public void refreshToken() {
		// 发布刷新令牌消息
		stringRedisTemplate.convertAndSend(RedisConstants.Gateway.CHANNEL_REFRESH_KEY, RedisConstants.Gateway.CHANNEL_REFRESH_TOKEN);
	}
}
