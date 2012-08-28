package scripts.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import scripts.zone.L2ZoneType;

public class L2NoLandingZone extends L2ZoneType
{
  public L2NoLandingZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    if (character.isPlayer())
    {
      character.setInsideZone(64, true);
    }
  }

  protected void onExit(L2Character character)
  {
    if (character.isPlayer())
    {
      character.setInsideZone(64, false);
    }
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }
}