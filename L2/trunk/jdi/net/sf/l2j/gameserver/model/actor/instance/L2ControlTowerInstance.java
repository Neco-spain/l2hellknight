package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.status.NpcStatus;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2ControlTowerInstance extends L2NpcInstance
{
  private List<L2Spawn> _guards;

  public L2ControlTowerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public boolean isAttackable()
  {
    return (getCastle() != null) && (getCastle().getCastleId() > 0) && (getCastle().getSiege().getIsInProgress());
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return (attacker != null) && ((attacker instanceof L2PcInstance)) && (getCastle() != null) && (getCastle().getCastleId() > 0) && (getCastle().getSiege().getIsInProgress()) && (getCastle().getSiege().checkIsAttacker(((L2PcInstance)attacker).getClan()));
  }

  public void onForcedAttack(L2PcInstance player)
  {
    onAction(player);
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;

    if (this != player.getTarget())
    {
      player.setTarget(this);

      MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
      player.sendPacket(my);

      StatusUpdate su = new StatusUpdate(getObjectId());
      su.addAttribute(9, (int)getStatus().getCurrentHp());
      su.addAttribute(10, getMaxHp());
      player.sendPacket(su);

      player.sendPacket(new ValidateLocation(this));
    }
    else if ((isAutoAttackable(player)) && (Math.abs(player.getZ() - getZ()) < 100) && (GeoData.getInstance().canSeeTarget(player, this)))
    {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);

      player.sendPacket(new ActionFailed());
    }
  }

  public void onDeath()
  {
    if (getCastle().getSiege().getIsInProgress())
    {
      getCastle().getSiege().killedCT(this);

      if ((getGuards() != null) && (getGuards().size() > 0))
      {
        for (L2Spawn spawn : getGuards())
        {
          if (spawn != null)
            spawn.stopRespawn();
        }
      }
    }
  }

  public void registerGuard(L2Spawn guard)
  {
    getGuards().add(guard);
  }

  public final List<L2Spawn> getGuards()
  {
    if (_guards == null) _guards = new FastList();
    return _guards;
  }
}