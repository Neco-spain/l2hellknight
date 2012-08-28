package net.sf.l2j.gameserver.model.actor.instance;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2FortSiegeGuardAI;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.knownlist.FortSiegeGuardKnownList;
import net.sf.l2j.gameserver.model.actor.status.NpcStatus;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.model.entity.FortSiege;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2FortSiegeGuardInstance extends L2Attackable
{
  public L2FortSiegeGuardInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
    getKnownList();
  }

  public FortSiegeGuardKnownList getKnownList()
  {
    if (!(super.getKnownList() instanceof FortSiegeGuardKnownList))
    {
      setKnownList(new FortSiegeGuardKnownList(this));
    }
    return (FortSiegeGuardKnownList)super.getKnownList();
  }

  public L2CharacterAI getAI()
  {
    if (_ai == null)
    {
      synchronized (this)
      {
        if (_ai == null)
        {
          _ai = new L2FortSiegeGuardAI(new L2Character.AIAccessor(this));
        }
      }
    }
    return _ai;
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    if (!(attacker instanceof L2PlayableInstance)) {
      return false;
    }
    boolean isFort = (getFort() != null) && (getFort().getFortId() > 0) && (getFort().getSiege().getIsInProgress()) && (!getFort().getSiege().checkIsDefender(((L2PcInstance)attacker).getClan()));

    return isFort;
  }

  public boolean hasRandomAnimation()
  {
    return false;
  }

  public void returnHome()
  {
    if (getWalkSpeed() <= 0)
      return;
    if (!isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), 40, false))
    {
      if (Config.DEBUG)
      {
        _log.info(getObjectId() + ": moving home");
      }
      setisReturningToSpawnPoint(true);
      clearAggroList();

      if (hasAI())
      {
        getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
      }
    }
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player))
    {
      player.sendPacket(new ActionFailed());
      return;
    }

    if (this != player.getTarget())
    {
      if (Config.DEBUG)
      {
        _log.info("new target selected:" + getObjectId());
      }

      player.setTarget(this);

      MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
      player.sendPacket(my);
      my = null;

      StatusUpdate su = new StatusUpdate(getObjectId());
      su.addAttribute(9, (int)getStatus().getCurrentHp());
      su.addAttribute(10, getMaxHp());
      player.sendPacket(su);
      su = null;

      player.sendPacket(new ValidateLocation(this));
    }
    else
    {
      if ((isAutoAttackable(player)) && (!isAlikeDead()))
      {
        if (Math.abs(player.getZ() - getZ()) < 600)
        {
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
        }
      }
      if (!isAutoAttackable(player))
      {
        if (!canInteract(player))
        {
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
        }
      }
    }

    player.sendPacket(new ActionFailed());
  }

  public void addDamageHate(L2Character attacker, int damage, int aggro)
  {
    if (attacker == null) {
      return;
    }
    if (!(attacker instanceof L2FortSiegeGuardInstance))
    {
      if ((attacker instanceof L2PlayableInstance))
      {
        L2PcInstance player = null;
        if ((attacker instanceof L2PcInstance))
        {
          player = (L2PcInstance)attacker;
        }
        else if ((attacker instanceof L2Summon))
        {
          player = ((L2Summon)attacker).getOwner();
        }
        if ((player != null) && (player.getClan() != null) && (player.getClan().getHasFort() == getFort().getFortId()))
          return;
      }
      super.addDamageHate(attacker, damage, aggro);
    }
  }
}