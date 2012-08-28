package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class L2ArenaZone extends L2ZoneType
{
  private String _arenaName;
  private int[] _spawnLoc;

  public L2ArenaZone(int id)
  {
    super(id);

    _spawnLoc = new int[3];
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("name"))
    {
      _arenaName = value;
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
    }
    else super.setParameter(name, value);
  }

  protected void onEnter(L2Character character)
  {
    character.setInsideZone(1, true);

    if ((character instanceof L2PcInstance))
    {
      ((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
    }
  }

  protected void onExit(L2Character character)
  {
    character.setInsideZone(1, false);

    if ((character instanceof L2PcInstance))
    {
      ((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
    }
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character) {
  }

  public final int[] getSpawnLoc() {
    return _spawnLoc;
  }
}