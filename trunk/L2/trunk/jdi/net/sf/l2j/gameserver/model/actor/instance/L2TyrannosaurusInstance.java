package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2TyrannosaurusInstance extends L2MonsterInstance
{
  private static final int BOSS_MAINTENANCE_INTERVAL = 10000;

  public L2TyrannosaurusInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  protected int getMaintenanceInterval() {
    return 10000;
  }

  public void onSpawn()
  {
    super.onSpawn();
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
  {
    super.reduceCurrentHp(damage, attacker, awake);
  }

  public void doAttack(L2Character target)
  {
    super.doAttack(target);
  }

  public void doCast(L2Skill skill)
  {
    super.doCast(skill);
  }

  public boolean isRaid()
  {
    return true;
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer))
      return false;
    L2PcInstance player = null;

    if ((killer instanceof L2PcInstance))
      player = (L2PcInstance)killer;
    else if ((killer instanceof L2Summon))
      player = ((L2Summon)killer).getOwner();
    return true;
  }
}