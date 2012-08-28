package scripts.zone.type;

import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Character;
import scripts.zone.L2ZoneType;

public class L2ZakenWelcomeZone extends L2ZoneType
{
  public L2ZakenWelcomeZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    if (character.isPlayer())
      GrandBossManager.getInstance().getZone(55242, 219131, -3251).allowPlayerEntry(character.getPlayer(), 9000000);
  }

  protected void onExit(L2Character character)
  {
  }

  public void onDieInside(L2Character character)
  {
  }

  public void onReviveInside(L2Character character)
  {
  }
}