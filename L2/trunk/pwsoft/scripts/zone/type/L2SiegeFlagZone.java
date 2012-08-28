package scripts.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import scripts.zone.L2ZoneType;

public class L2SiegeFlagZone extends L2ZoneType
{
  public L2SiegeFlagZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    character.setInSiegeFlagArea(true);
  }

  protected void onExit(L2Character character)
  {
    character.setInSiegeFlagArea(false);
  }

  public void onDieInside(L2Character character)
  {
  }

  public void onReviveInside(L2Character character)
  {
  }
}