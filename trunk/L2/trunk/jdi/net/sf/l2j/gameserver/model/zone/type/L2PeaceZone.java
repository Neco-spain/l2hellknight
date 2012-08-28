package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;

public class L2PeaceZone extends L2ZoneType
{
  public L2PeaceZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    character.setInsideZone(2, true);
  }

  protected void onExit(L2Character character)
  {
    character.setInsideZone(2, false);
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }
}