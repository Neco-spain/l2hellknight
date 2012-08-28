package scripts.zone.type;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.network.serverpackets.ClanHallDecoration;
import net.sf.l2j.util.Location;
import scripts.zone.L2ZoneType;

public class L2ClanHallZone extends L2ZoneType
{
  private int _clanHallId;
  private int[] _spawnLoc;

  public L2ClanHallZone(int id)
  {
    super(id);

    _spawnLoc = new int[3];
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("clanHallId"))
    {
      _clanHallId = Integer.parseInt(value);

      ClanHallManager.getInstance().getClanHallById(_clanHallId).setZone(this);
    }
    else if (name.equals("spawnX"))
    {
      _spawnLoc[0] = Integer.parseInt(value);
    }
    else if (name.equals("spawnY"))
    {
      _spawnLoc[1] = Integer.parseInt(value);
    }
    else if (name.equals("spawnZ"))
    {
      _spawnLoc[2] = Integer.parseInt(value);
    } else {
      super.setParameter(name, value);
    }
  }

  protected void onEnter(L2Character character)
  {
    if (character.isPlayer())
    {
      character.setInsideZone(16, true);

      ClanHall clanHall = ClanHallManager.getInstance().getClanHallById(_clanHallId);
      if (clanHall == null) return;

      ClanHallDecoration deco = new ClanHallDecoration(clanHall);
      ((L2PcInstance)character).sendPacket(deco);

      ((L2PcInstance)character).sendMessage("\u0412\u044B \u0432\u043E\u0448\u043B\u0438 \u0432 \u043A\u043B\u0430\u043D\u0445\u043E\u043B\u043B");
    }
  }

  protected void onExit(L2Character character)
  {
    if (character.isPlayer())
    {
      character.setInsideZone(16, false);

      ((L2PcInstance)character).sendMessage("\u0412\u044B \u043F\u043E\u043A\u0438\u043D\u0443\u043B\u0438 \u043A\u043B\u0430\u043D\u0445\u043E\u043B\u043B");
    }
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }

  public void banishForeigners(int owningClanId)
  {
    for (L2Character temp : _characterList.values())
    {
      if ((!temp.isPlayer()) || 
        (((L2PcInstance)temp).getClanId() == owningClanId))
        continue;
      ((L2PcInstance)temp).teleToLocation(MapRegionTable.TeleportWhereType.Town);
    }
  }

  public Location getSpawn()
  {
    return new Location(_spawnLoc[0], _spawnLoc[1], _spawnLoc[2]);
  }
}