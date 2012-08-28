package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.instancemanager.RaidBossPointsManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public final class L2GrandBossInstance extends L2MonsterInstance
{
  private static final int BOSS_MAINTENANCE_INTERVAL = 10000;
  protected boolean _isInSocialAction = false;

  public L2GrandBossInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  protected int getMaintenanceInterval() {
    return 10000;
  }

  public void onSpawn()
  {
    setIsRaid(true);
    super.onSpawn();
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
  {
    if ((IsInSocialAction()) || (isInvul())) return;
    super.reduceCurrentHp(damage, attacker, awake);
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
    if (player != null)
    {
      player.broadcastPacket(new SystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
      if (player.getParty() != null)
        for (L2PcInstance member : player.getParty().getPartyMembers())
          RaidBossPointsManager.addPoints(member, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
      else {
        RaidBossPointsManager.addPoints(player, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
      }

    }

    return true;
  }

  public boolean IsInSocialAction()
  {
    return _isInSocialAction;
  }

  public void setIsInSocialAction(boolean value)
  {
    _isInSocialAction = value;
  }

  public void doAttack(L2Character target)
  {
    if (_isInSocialAction) return;
    super.doAttack(target);
  }

  public void doCast(L2Skill skill)
  {
    if (_isInSocialAction) return;
    super.doCast(skill);
  }
}