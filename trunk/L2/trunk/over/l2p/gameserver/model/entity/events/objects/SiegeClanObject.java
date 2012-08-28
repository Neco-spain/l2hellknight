package l2p.gameserver.model.entity.events.objects;

import java.io.Serializable;
import java.util.Comparator;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.components.IStaticPacket;

public class SiegeClanObject
  implements Serializable
{
  private String _type;
  private Clan _clan;
  private NpcInstance _flag;
  private final long _date;

  public SiegeClanObject(String type, Clan clan, long param)
  {
    this(type, clan, 0L, System.currentTimeMillis());
  }

  public SiegeClanObject(String type, Clan clan, long param, long date)
  {
    _type = type;
    _clan = clan;
    _date = date;
  }

  public int getObjectId()
  {
    return _clan.getClanId();
  }

  public Clan getClan()
  {
    return _clan;
  }

  public NpcInstance getFlag()
  {
    return _flag;
  }

  public void deleteFlag()
  {
    if (_flag != null)
    {
      _flag.deleteMe();
      _flag = null;
    }
  }

  public void setFlag(NpcInstance npc)
  {
    _flag = npc;
  }

  public void setType(String type)
  {
    _type = type;
  }

  public String getType()
  {
    return _type;
  }

  public void broadcast(IStaticPacket[] packet)
  {
    getClan().broadcastToOnlineMembers(packet);
  }

  public void broadcast(L2GameServerPacket[] packet)
  {
    getClan().broadcastToOnlineMembers(packet);
  }

  public void setEvent(boolean start, SiegeEvent event)
  {
    if (start)
    {
      for (Player player : _clan.getOnlineMembers(0))
      {
        player.addEvent(event);
        player.broadcastCharInfo();
      }
    }
    else
    {
      for (Player player : _clan.getOnlineMembers(0))
      {
        player.removeEvent(event);
        player.getEffectList().stopEffect(5660);
        player.broadcastCharInfo();
      }
    }
  }

  public boolean isParticle(Player player)
  {
    return true;
  }

  public long getParam()
  {
    return 0L;
  }

  public long getDate()
  {
    return _date;
  }

  public static class SiegeClanComparatorImpl
    implements Comparator<SiegeClanObject>
  {
    private static final SiegeClanComparatorImpl _instance = new SiegeClanComparatorImpl();

    public static SiegeClanComparatorImpl getInstance()
    {
      return _instance;
    }

    public int compare(SiegeClanObject o1, SiegeClanObject o2)
    {
      return o2.getParam() == o1.getParam() ? 0 : o2.getParam() < o1.getParam() ? -1 : 1;
    }
  }
}