package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;

public class L2SwampZone extends L2ZoneType
{
  private int _move_bonus;

  public L2SwampZone(int id)
  {
    super(id);

    _move_bonus = -50;
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("move_bonus"))
    {
      _move_bonus = Integer.parseInt(value);
    }
    else
      super.setParameter(name, value);
  }

  protected void onEnter(L2Character character)
  {
    character.setInsideZone(2048, true);
    if ((character instanceof L2PcInstance))
    {
      ((L2PcInstance)character).enterDangerArea();
      ((L2PcInstance)character).broadcastUserInfo();
    }
  }

  protected void onExit(L2Character character)
  {
    character.setInsideZone(2048, false);
    if ((character instanceof L2PcInstance))
    {
      ((L2PcInstance)character).exitDangerArea();
      ((L2PcInstance)character).broadcastUserInfo();
    }
  }

  public int getMoveBonus()
  {
    return _move_bonus;
  }

  public void onDieInside(L2Character character)
  {
  }

  public void onReviveInside(L2Character character)
  {
  }
}