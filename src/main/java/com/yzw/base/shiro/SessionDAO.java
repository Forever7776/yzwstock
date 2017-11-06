package com.yzw.base.shiro;

import java.io.Serializable;
import java.util.Collection;

import com.jfinal.plugin.redis.Redis;
import com.yzw.base.config.Consts;
import com.yzw.base.util.L;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;


/***
 * 用redis 实现 分布式缓存
 * 
 * @author 12
 * 
 */
public class SessionDAO extends EnterpriseCacheSessionDAO
{
	private static final String SHIRO_SESSION = "shiro-session:";

	private static int i;
	public static  SessionDAO me;
	
	@Override
	public void setCacheManager(CacheManager cacheManager) {
		super.setCacheManager(cacheManager);
		me=this;
	}

	@Override
	protected Serializable doCreate(Session session)
	{
		if (Consts.OPEN_REDIS)
		{
			L.i("on create session=" + session);
			Serializable sessionId = super.doCreate(session);
			L.i("after on create session=" + session);
			saveSession(sessionId, session);
			return sessionId;
		}
		else return super.doCreate(session);

	}

	@Override
	protected Session doReadSession(Serializable sessionId)
	{
		L.i("doReadSession");
		if (Consts.OPEN_REDIS)
		{
			Session session = super.doReadSession(sessionId);
			if (session != null) return session;
			session = Redis.use().get(SHIRO_SESSION + sessionId.toString());
			if (session != null) super.update(session);
			return session;
		}
		else return super.doReadSession(sessionId);
	}

	@Override
	public void update(Session session) throws UnknownSessionException
	{

		if (Consts.OPEN_REDIS)
		{
			super.update(session);
			if (i++ % 10 == 0) saveSession(session.getId(), session);
		}
		else super.update(session);
	}

	@Override
	public void delete(Session session)
	{
		if (session != null)
		{
			Serializable id = session.getId();

			if (id != null)
			{
				if (Consts.OPEN_REDIS) Redis.use().del(SHIRO_SESSION + session.getId().toString());
				super.delete(session);
			}

		}
	}

	private Session saveSession(Serializable id, Session session)
	{
		L.i("saveSession ID=" + id + " session=" + session);
		if (id != null) Redis.use().setex(SHIRO_SESSION + session.getId().toString(), (int) (session.getTimeout() / 1000), session);

		return session;
	}

	@Override
	public Collection<Session> getActiveSessions()
	{
		if (Consts.OPEN_REDIS) return null;
		else return super.getActiveSessions();
	}

 
	
}
