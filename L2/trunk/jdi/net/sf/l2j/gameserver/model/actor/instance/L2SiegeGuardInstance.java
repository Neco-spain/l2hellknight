package net.sf.l2j.gameserver.model.actor.instance;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2SiegeGuardAI;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.actor.knownlist.SiegeGuardKnownList;
import net.sf.l2j.gameserver.model.actor.status.NpcStatus;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public final class L2SiegeGuardInstance extends L2Attackable
{
  private static Logger _log = Logger.getLogger(L2SiegeGuardInstance.class.getName());
  private int _homeX;
  private int _homeY;
  private int _homeZ;

  public L2SiegeGuardInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
    getKnownList();
  }

  public final SiegeGuardKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof SiegeGuardKnownList)))
      setKnownList(new SiegeGuardKnownList(this));
    return (SiegeGuardKnownList)super.getKnownList();
  }

  public L2CharacterAI getAI()
  {
    if (_ai == null)
    {
      synchronized (this)
      {
        if (_ai == null)
          _ai = new L2SiegeGuardAI(new L2Character.AIAccessor(this));
      }
    }
    return _ai;
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return (attacker != null) && ((attacker instanceof L2PcInstance)) && (getCastle() != null) && (getCastle().getCastleId() > 0) && (getCastle().getSiege().getIsInProgress()) && (!getCastle().getSiege().checkIsDefender(((L2PcInstance)attacker).getClan()));
  }

  public void getHomeLocation()
  {
    _homeX = getX();
    _homeY = getY();
    _homeZ = getZ();

    if (Config.DEBUG)
      _log.finer(getObjectId() + ": Home location set to" + " X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
  }

  public int getHomeX()
  {
    return _homeX;
  }

  public int getHomeY()
  {
    return _homeY;
  }

  public void returnHome()
  {
    if (!isInsideRadius(_homeX, _homeY, 40, false))
    {
      if (Config.DEBUG) _log.fine(getObjectId() + ": moving home");
      setisReturningToSpawnPoint(true);
      clearAggroList();

      if (hasAI())
        getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_homeX, _homeY, _homeZ, 0));
    }
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;

    if (this != player.getTarget())
    {
      if (Config.DEBUG) _log.fine("new target selected:" + getObjectId());

      player.setTarget(this);

      MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
      player.sendPacket(my);

      StatusUpdate su = new StatusUpdate(getObjectId());
      su.addAttribute(9, (int)getStatus().getCurrentHp());
      su.addAttribute(10, getMaxHp());
      player.sendPacket(su);

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
        else
        {
          player.sendPacket(new ActionFailed());
        }
      }
      if (!isAutoAttackable(player))
      {
        if (!canInteract(player))
        {
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
        }
        else
        {
          SocialAction sa = new SocialAction(getObjectId(), Rnd.nextInt(8));
          broadcastPacket(sa);
          sendPacket(sa);
          showChatWindow(player, 0);
        }
      }
    }
  }

  public void addDamageHate(L2Character attacker, int damage, int aggro)
  {
    if (attacker == null) {
      return;
    }
    if (!(attacker instanceof L2SiegeGuardInstance))
    {
      super.addDamageHate(attacker, damage, aggro);
    }
  }
}