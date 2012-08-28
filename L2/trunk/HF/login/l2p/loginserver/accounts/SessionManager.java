package l2m.loginserver.accounts;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2m.commons.threading.RunnableImpl;
import l2m.loginserver.SessionKey;
import l2m.loginserver.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManager
{
  private static final Logger _log = LoggerFactory.getLogger(SessionManager.class);

  private static final SessionManager _instance = new SessionManager();

  private final Map<SessionKey, Session> sessions = new HashMap();
  private final Lock lock = new ReentrantLock();

  public static final SessionManager getInstance()
  {
    return _instance;
  }

  private SessionManager()
  {
    ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
    {
      public void runImpl()
      {
        lock.lock();
        try
        {
          currentMillis = System.currentTimeMillis();

          for (itr = sessions.values().iterator(); itr.hasNext(); )
          {
            SessionManager.Session session = (SessionManager.Session)itr.next();
            if (session.getExpireTime() < currentMillis)
              itr.remove();
          }
        }
        finally
        {
          long currentMillis;
          Iterator itr;
          lock.unlock();
        }
      }
    }
    , 30000L, 30000L);
  }

  public Session openSession(Account account)
  {
    lock.lock();
    try
    {
      Session session = new Session(account, null);
      sessions.put(session.getSessionKey(), session);
      Session localSession1 = session;
      return localSession1; } finally { lock.unlock(); } throw localObject;
  }

  public Session closeSession(SessionKey skey)
  {
    lock.lock();
    try
    {
      Session localSession = (Session)sessions.remove(skey);
      return localSession; } finally { lock.unlock(); } throw localObject;
  }

  public Session getSessionByName(String name)
  {
    for (Session session : sessions.values())
    {
      if (session.account.getLogin().equalsIgnoreCase(name))
        return session;
    }
    return null;
  }

  public final class Session
  {
    private final Account account;
    private final SessionKey skey;
    private final long expireTime;

    private Session(Account account)
    {
      this.account = account;
      skey = SessionKey.create();
      expireTime = (System.currentTimeMillis() + 60000L);
    }

    public SessionKey getSessionKey()
    {
      return skey;
    }

    public Account getAccount()
    {
      return account;
    }

    public long getExpireTime()
    {
      return expireTime;
    }
  }
}