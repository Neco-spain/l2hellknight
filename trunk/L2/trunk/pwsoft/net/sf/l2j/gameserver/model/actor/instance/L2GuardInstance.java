package net.sf.l2j.gameserver.model.actor.instance;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.Config.PvpColor;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.actor.knownlist.GuardKnownList;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public class L2GuardInstance extends L2Attackable
{
  private static final Logger _log = AbstractLogger.getLogger(L2GuardInstance.class.getName());
  private int _homeX;
  private int _homeY;
  private int _homeZ;
  private static final int RETURN_INTERVAL = 60000;
  private int _weaponEnch = 0;

  public L2GuardInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
    getKnownList();

    ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ReturnTask(), 60000L, 60000 + Rnd.nextInt(60000));

    if ((Config.ENCH_GUARD_CAHNCE > 0) && (Rnd.get(100) < Config.ENCH_GUARD_CAHNCE))
      _weaponEnch = Rnd.get(Config.ENCH_GUARD_MINMAX.nick, Config.ENCH_GUARD_MINMAX.title);
  }

  public final GuardKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof GuardKnownList))) {
      setKnownList(new GuardKnownList(this));
    }
    return (GuardKnownList)super.getKnownList();
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return false;
  }

  public void getHomeLocation()
  {
    _homeX = getX();
    _homeY = getY();
    _homeZ = getZ();
  }

  public int getHomeX()
  {
    return _homeX;
  }

  public void returnHome()
  {
    if (!isInsideRadius(_homeX, _homeY, 150, false))
    {
      clearAggroList();

      getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_homeX, _homeY, _homeZ, 0));
    }
  }

  public void onSpawn()
  {
    super.onSpawn();
    _homeX = getX();
    _homeY = getY();
    _homeZ = getZ();

    L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
    if ((region != null) && (!region.isActive().booleanValue()))
      ((L2AttackableAI)getAI()).stopAITask();
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";
    if (val == 0)
      pom = "" + npcId;
    else {
      pom = npcId + "-" + val;
    }
    return "data/html/guard/" + pom + ".htm";
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) {
      return;
    }

    if (getObjectId() != player.getTargetId())
    {
      player.setTarget(this);

      player.sendPacket(new MyTargetSelected(getObjectId(), 0));

      player.sendPacket(new ValidateLocation(this));
    }
    else if (containsTarget(player))
    {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
    }
    else if (!canInteract(player))
    {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    }
    else
    {
      broadcastPacket(new SocialAction(getObjectId(), Rnd.nextInt(8)));

      showChatWindow(player, 0);
    }

    player.sendActionFailed();
  }

  public int getWeaponEnchant()
  {
    return _weaponEnch;
  }

  public boolean isL2Guard()
  {
    return true;
  }

  public class ReturnTask
    implements Runnable
  {
    public ReturnTask()
    {
    }

    public void run()
    {
      if (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
        returnHome();
    }
  }
}