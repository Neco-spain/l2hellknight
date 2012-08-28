package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.zone.L2ZoneForm;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;

public class L2FishingZone extends L2ZoneType
{
  public L2FishingZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
  }

  protected void onExit(L2Character character)
  {
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }

  public int getWaterZ()
  {
    return getZone().getHighZ();
  }
}