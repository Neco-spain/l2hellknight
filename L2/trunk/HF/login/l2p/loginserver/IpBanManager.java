package l2m.loginserver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpBanManager
{
  private static final Logger _log = LoggerFactory.getLogger(IpBanManager.class);

  private static final IpBanManager _instance = new IpBanManager();

  private final Map<String, IpSession> ips = new HashMap();
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock readLock = lock.readLock();
  private final Lock writeLock = lock.writeLock();

  public static final IpBanManager getInstance()
  {
    return _instance;
  }

  private IpBanManager()
  {
    ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
    {
      public void run()
      {
        long currentMillis = System.currentTimeMillis();

        writeLock.lock();
        try
        {
          for (itr = ips.values().iterator(); itr.hasNext(); )
          {
            IpBanManager.IpSession session = (IpBanManager.IpSession)itr.next();
            if ((session.banExpire < currentMillis) && (session.lastTry < currentMillis - Config.LOGIN_TRY_TIMEOUT))
              itr.remove();
          }
        }
        finally
        {
          Iterator itr;
          writeLock.unlock();
        }
      }
    }
    , 1000L, 1000L);
  }

  public boolean isIpBanned(String ip)
  {
    readLock.lock();
    try
    {
      IpSession ipsession;
      if ((ipsession = (IpSession)ips.get(ip)) == null) {
        i = 0;
        return i;
      }
      int i = ipsession.banExpire > System.currentTimeMillis() ? 1 : 0;
      return i; } finally { readLock.unlock(); } throw localObject;
  }

  public boolean tryLogin(String ip, boolean success)
  {
    writeLock.lock();
    try
    {
      IpSession ipsession;
      if ((ipsession = (IpSession)ips.get(ip)) == null) {
        ips.put(ip, ipsession = new IpSession(null));
      }
      long currentMillis = System.currentTimeMillis();

      if (currentMillis - ipsession.lastTry < Config.LOGIN_TRY_TIMEOUT) {
        success = false;
      }

      if (success)
      {
        if (ipsession.tryCount > 0) {
          ipsession.tryCount -= 1;
        }

      }
      else if (ipsession.tryCount < Config.LOGIN_TRY_BEFORE_BAN) {
        ipsession.tryCount += 1;
      }

      ipsession.lastTry = currentMillis;

      if (ipsession.tryCount == Config.LOGIN_TRY_BEFORE_BAN)
      {
        _log.warn("IpBanManager: " + ip + " banned for " + Config.IP_BAN_TIME / 1000L + " seconds.");
        ipsession.banExpire = (currentMillis + Config.IP_BAN_TIME);
        i = 0;
        return i;
      }
      int i = 1;
      return i; } finally { writeLock.unlock(); } throw localObject;
  }

  private class IpSession
  {
    public int tryCount;
    public long lastTry;
    public long banExpire;

    private IpSession()
    {
    }
  }
}