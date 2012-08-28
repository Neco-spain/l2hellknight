package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.actor.status.NpcStatus;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2SiegeFlagInstance extends L2MonsterInstance
{
  private L2PcInstance _player;
  private Siege _siege;

  public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template)
  {
    super(objectId, template);

    _player = player;
    _siege = SiegeManager.getInstance().getSiege(_player.getX(), _player.getY(), _player.getZ());
    if ((_player.getClan() == null) || (_siege == null))
    {
      deleteMe();
    }
    else
    {
      L2SiegeClan sc = _siege.getAttackerClan(_player.getClan());
      if (sc == null)
        deleteMe();
      else
        sc.addFlag(this);
    }
  }

  public void onSpawn()
  {
    setIsImobilised(true);
    setIsParalyzed(true);
  }

  public boolean isAttackable()
  {
    return (getCastle() != null) && (getCastle().getCastleId() > 0) && (getCastle().getSiege().getIsInProgress());
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return (attacker != null) && (attacker.isPlayer()) && (getCastle() != null) && (getCastle().getCastleId() > 0) && (getCastle().getSiege().getIsInProgress());
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer))
      return false;
    L2SiegeClan sc = _siege.getAttackerClan(_player.getClan());
    if (sc != null)
      sc.removeFlag();
    return true;
  }

  public void onForcedAttack(L2PcInstance player)
  {
    onAction(player);
  }

  public void onAction(L2PcInstance player)
  {
    if ((player == null) || (!canTarget(player))) {
      return;
    }

    if (this != player.getTarget())
    {
      player.setTarget(this);

      player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

      StatusUpdate su = new StatusUpdate(getObjectId());
      su.addAttribute(9, (int)getStatus().getCurrentHp());
      su.addAttribute(10, getMaxHp());
      player.sendPacket(su);

      player.sendPacket(new ValidateLocation(this));
    }
    else if ((isAutoAttackable(player)) && (Math.abs(player.getZ() - getZ()) < 100)) {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
    }
    else
    {
      player.sendActionFailed();
    }
  }

  public boolean isDebuffProtected()
  {
    return true;
  }
}