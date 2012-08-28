package net.sf.protection;

import java.lang.reflect.Method;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.GameGuardQuery;

public class nProtect
{
  private static final Logger _log = Logger.getLogger("Loader");

  protected Method _checkGameGuardQuery = null;
  protected Method _startTask = null;
  protected Method _checkRestriction = null;
  protected Method _sendRequest = null;
  protected Method _closeSession = null;
  protected Method _sendGGQuery = null;
  private static nProtect _instance = null;

  public static nProtect getInstance() {
    if (_instance == null)
      _instance = new nProtect();
    return _instance;
  }

  private nProtect()
  {
    Class clazz = null;
    try
    {
      try {
        clazz = Class.forName("com.l2jcore.protection.main");
      } catch (ClassNotFoundException e) {
      }
      if (clazz != null)
      {
        Method m = clazz.getMethod("init", new Class[] { nProtectAccessor.class });
        if (m != null)
          m.invoke(null, new Object[] { new nProtectAccessor() });
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      _log.warning("Error" + e.getMessage());
    }
  }

  public void sendGameGuardQuery(GameGuardQuery pkt)
  {
    try {
      if (_sendGGQuery != null)
        _sendGGQuery.invoke(pkt, new Object[0]);
    }
    catch (Exception e)
    {
    }
  }

  public boolean checkGameGuardReply(L2GameClient cl, int[] reply)
  {
    try
    {
      if (_checkGameGuardQuery != null)
        return ((Boolean)_checkGameGuardQuery.invoke(null, new Object[] { cl, reply })).booleanValue();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      _log.warning("Error" + e.getMessage());
    }
    return true;
  }

  public ScheduledFuture<?> startTask(L2GameClient client)
  {
    try
    {
      if (_startTask != null)
        return (ScheduledFuture)_startTask.invoke(null, new Object[] { client });
    }
    catch (Exception e)
    {
      e.printStackTrace();
      _log.warning("Error" + e.getMessage());
    }
    return null;
  }

  public void sendRequest(L2GameClient cl)
  {
    if (_sendRequest != null)
      try
      {
        _sendRequest.invoke(null, new Object[] { cl });
      }
      catch (Exception e)
      {
        e.printStackTrace();
        _log.warning("Error" + e.getMessage());
      }
  }

  public void closeSession(L2GameClient cl)
  {
    if (_closeSession != null)
      try
      {
        _closeSession.invoke(null, new Object[] { cl });
      }
      catch (Exception e)
      {
        _log.warning("Error" + e.getMessage());
      }
  }

  public boolean checkRestriction(L2PcInstance player, RestrictionType type, Object[] params)
  {
    try
    {
      if (_checkRestriction != null)
        return ((Boolean)_checkRestriction.invoke(null, new Object[] { player, type, params })).booleanValue();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      _log.warning("Error" + e.getMessage());
    }
    return true;
  }

  public class nProtectAccessor
  {
    public nProtectAccessor()
    {
    }

    public void setCheckGameGuardQuery(Method m)
    {
      _checkGameGuardQuery = m;
    }

    public void setStartTask(Method m)
    {
      _startTask = m;
    }

    public void setCheckRestriction(Method m)
    {
      _checkRestriction = m;
    }

    public void setSendRequest(Method m)
    {
      _sendRequest = m;
    }

    public void setCloseSession(Method m)
    {
      _closeSession = m;
    }
    public void setSendGGQuery(Method m) {
      _sendGGQuery = m;
    }
  }

  public static enum RestrictionType
  {
    RESTRICT_ENTER, RESTRICT_EVENT, RESTRICT_OLYMPIAD, RESTRICT_SIEGE;
  }
}