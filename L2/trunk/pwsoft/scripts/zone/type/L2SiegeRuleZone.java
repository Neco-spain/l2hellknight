package scripts.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import scripts.zone.L2ZoneType;

public class L2SiegeRuleZone extends L2ZoneType
{
  public L2SiegeRuleZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    character.setInSiegeRuleArea(true);
  }

  protected void onExit(L2Character character)
  {
    character.setInSiegeRuleArea(false);
  }

  public void onDieInside(L2Character character)
  {
  }

  public void onReviveInside(L2Character character)
  {
  }
}