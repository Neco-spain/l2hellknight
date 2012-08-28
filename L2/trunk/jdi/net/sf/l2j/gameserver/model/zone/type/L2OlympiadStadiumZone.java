package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class L2OlympiadStadiumZone extends L2ZoneType
{
  private int _stadiumId;

  public L2OlympiadStadiumZone(int id)
  {
    super(id);
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("stadiumId"))
    {
      _stadiumId = Integer.parseInt(value);
    }
    else super.setParameter(name, value);
  }

  protected void onEnter(L2Character character)
  {
    character.setInsideZone(1, true);
    character.setInsideZone(8192, true);

    character.setInsideZone(64, false);

    if ((character instanceof L2PcInstance))
    {
      ((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
    }
  }

  protected void onExit(L2Character character)
  {
    character.setInsideZone(1, false);
    character.setInsideZone(8192, false);

    if ((character instanceof L2PcInstance))
    {
      ((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
    }
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }

  public int getStadiumId() {
    return _stadiumId;
  }
}