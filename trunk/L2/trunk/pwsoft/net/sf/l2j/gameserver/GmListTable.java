package net.sf.l2j.gameserver;

import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.util.log.AbstractLogger;

public class GmListTable
{
  private static Logger _log = AbstractLogger.getLogger(GmListTable.class.getName());
  private static GmListTable _instance;
  private FastMap<L2PcInstance, Boolean> _gmList;

  public static GmListTable getInstance()
  {
    if (_instance == null)
    {
      _instance = new GmListTable();
    }
    return _instance;
  }

  public FastList<L2PcInstance> getAllGms(boolean includeHidden)
  {
    FastList tmpGmList = new FastList();

    FastMap.Entry n = _gmList.head(); for (FastMap.Entry end = _gmList.tail(); (n = n.getNext()) != end; ) {
      if ((includeHidden) || (!((Boolean)n.getValue()).booleanValue()))
        tmpGmList.add(n.getKey());
    }
    return tmpGmList;
  }

  public FastList<String> getAllGmNames(boolean includeHidden)
  {
    FastList tmpGmList = new FastList();

    FastMap.Entry n = _gmList.head(); for (FastMap.Entry end = _gmList.tail(); (n = n.getNext()) != end; ) {
      if (!((Boolean)n.getValue()).booleanValue()) {
        tmpGmList.add(((L2PcInstance)n.getKey()).getName()); continue;
      }if (includeHidden)
        tmpGmList.add(((L2PcInstance)n.getKey()).getName() + " (invis)");
    }
    return tmpGmList;
  }

  private GmListTable()
  {
    _gmList = new FastMap().shared("GmListTable._gmList");
  }

  public void addGm(L2PcInstance player, boolean hidden)
  {
    _gmList.put(player, Boolean.valueOf(hidden));
  }

  public void deleteGm(L2PcInstance player)
  {
    _gmList.remove(player);
  }

  public void showGm(L2PcInstance player)
  {
    FastMap.Entry gm = _gmList.getEntry(player);
    if (gm != null) gm.setValue(Boolean.valueOf(false));
  }

  public void hideGm(L2PcInstance player)
  {
    FastMap.Entry gm = _gmList.getEntry(player);
    if (gm != null) gm.setValue(Boolean.valueOf(true));
  }

  public boolean isGmOnline(boolean includeHidden)
  {
    FastMap.Entry n = _gmList.head(); for (FastMap.Entry end = _gmList.tail(); (n = n.getNext()) != end; )
    {
      if ((includeHidden) || (!((Boolean)n.getValue()).booleanValue())) {
        return true;
      }
    }
    return false;
  }

  public void sendListToPlayer(L2PcInstance player)
  {
    if (!isGmOnline(player.isGM())) {
      player.sendPacket(Static.NO_GM_PROVIDING_SERVICE_NOW);
    }
    else {
      player.sendPacket(SystemMessage.id(SystemMessageId.GM_LIST));

      for (String name : getAllGmNames(player.isGM()))
      {
        player.sendPacket(SystemMessage.id(SystemMessageId.GM_S1).addString(name));
      }
    }
  }

  public static void broadcastToGMs(L2GameServerPacket packet)
  {
    for (L2PcInstance gm : getInstance().getAllGms(true))
    {
      gm.sendPacket(packet);
    }
  }

  public static void broadcastMessageToGMs(String message)
  {
    for (L2PcInstance gm : getInstance().getAllGms(true))
    {
      gm.sendPacket(SystemMessage.sendString(message));
    }
  }
}