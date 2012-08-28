package net.sf.l2j.gameserver.model.actor.instance;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.knownlist.CommanderKnownList;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.model.entity.FortSiege;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2CommanderInstance extends L2Attackable
{
  private int _homeX;
  private int _homeY;
  private int _homeZ;

  public L2CommanderInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
    getKnownList();
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return (attacker != null) && ((attacker instanceof L2PcInstance)) && (getFort() != null) && (getFort().getFortId() > 0) && (getFort().getSiege().getIsInProgress()) && (!getFort().getSiege().checkIsDefender(((L2PcInstance)attacker).getClan()));
  }

  public final CommanderKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof CommanderKnownList)))
    {
      setKnownList(new CommanderKnownList(this));
    }
    return (CommanderKnownList)super.getKnownList();
  }

  public void addDamageHate(L2Character attacker, int damage, int aggro)
  {
    if (attacker == null) {
      return;
    }
    if (!(attacker instanceof L2CommanderInstance))
    {
      super.addDamageHate(attacker, damage, aggro);
    }
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }
    if (getFort().getSiege().getIsInProgress())
    {
      getFort().getSiege().killedCommander(this);
    }

    return true;
  }

  public void getHomeLocation()
  {
    _homeX = getX();
    _homeY = getY();
    _homeZ = getZ();

    if (Config.DEBUG)
    {
      _log.finer(getObjectId() + ": Home location set to" + " X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
    }
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
      if (Config.DEBUG)
      {
        _log.fine(getObjectId() + ": moving home");
      }
      setisReturningToSpawnPoint(true);
      clearAggroList();

      if (hasAI())
      {
        getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_homeX, _homeY, _homeZ, 0));
      }
    }
  }
}