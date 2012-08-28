package l2m.gameserver.model.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.ai.PlayerAI;
import l2m.gameserver.data.xml.holder.NpcHolder;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.AutoAttackStart;
import l2m.gameserver.network.serverpackets.CharInfo;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.MyTargetSelected;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.templates.PlayerTemplate;
import l2m.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecoyInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;
  private static final Logger _log = LoggerFactory.getLogger(DecoyInstance.class);
  private HardReference<Player> _playerRef;
  private int _lifeTime;
  private int _timeRemaining;
  private ScheduledFuture<?> _decoyLifeTask;
  private ScheduledFuture<?> _hateSpam;

  public DecoyInstance(int objectId, NpcTemplate template, Player owner, int lifeTime)
  {
    super(objectId, template);

    _playerRef = owner.getRef();
    _lifeTime = lifeTime;
    _timeRemaining = _lifeTime;
    int skilllevel = getNpcId() < 13257 ? getNpcId() - 13070 : getNpcId() - 13250;
    _decoyLifeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new DecoyLifetime(), 1000L, 1000L);
    _hateSpam = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HateSpam(SkillTable.getInstance().getInfo(5272, skilllevel)), 1000L, 3000L);
  }

  protected void onDeath(Creature killer)
  {
    super.onDeath(killer);
    if (_hateSpam != null)
    {
      _hateSpam.cancel(false);
      _hateSpam = null;
    }
    _lifeTime = 0;
  }

  public void unSummon()
  {
    if (_decoyLifeTask != null)
    {
      _decoyLifeTask.cancel(false);
      _decoyLifeTask = null;
    }
    if (_hateSpam != null)
    {
      _hateSpam.cancel(false);
      _hateSpam = null;
    }
    deleteMe();
  }

  public void decTimeRemaining(int value)
  {
    _timeRemaining -= value;
  }

  public int getTimeRemaining()
  {
    return _timeRemaining;
  }

  public int getLifeTime()
  {
    return _lifeTime;
  }

  public Player getPlayer()
  {
    return (Player)_playerRef.get();
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    Player owner = getPlayer();
    return (owner != null) && (owner.isAutoAttackable(attacker));
  }

  public boolean isAttackable(Creature attacker)
  {
    Player owner = getPlayer();
    return (owner != null) && (owner.isAttackable(attacker));
  }

  protected void onDelete()
  {
    Player owner = getPlayer();
    if (owner != null)
      owner.setDecoy(null);
    super.onDelete();
  }

  public void onAction(Player player, boolean shift)
  {
    if (player.getTarget() != this)
    {
      player.setTarget(this);
      player.sendPacket(new MyTargetSelected(getObjectId(), 0));
    }
    else if (isAutoAttackable(player)) {
      player.getAI().Attack(this, false, shift);
    }
  }

  public double getColRadius()
  {
    Player player = getPlayer();
    if (player == null)
      return 0.0D;
    if ((player.getTransformation() != 0) && (player.getTransformationTemplate() != 0))
      return NpcHolder.getInstance().getTemplate(player.getTransformationTemplate()).collisionRadius;
    return player.getBaseTemplate().collisionRadius;
  }

  public double getColHeight()
  {
    Player player = getPlayer();
    if (player == null)
      return 0.0D;
    if ((player.getTransformation() != 0) && (player.getTransformationTemplate() != 0))
      return NpcHolder.getInstance().getTemplate(player.getTransformationTemplate()).collisionHeight;
    return player.getBaseTemplate().collisionHeight;
  }

  public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
  {
    if (!isInCombat()) {
      return Collections.singletonList(new CharInfo(this));
    }

    List list = new ArrayList(2);
    list.add(new CharInfo(this));
    list.add(new AutoAttackStart(objectId));
    return list;
  }

  public boolean isInvul()
  {
    return _isInvul;
  }

  class HateSpam extends RunnableImpl
  {
    private Skill _skill;

    HateSpam(Skill skill)
    {
      _skill = skill;
    }

    public void runImpl()
      throws Exception
    {
      try
      {
        setTarget(DecoyInstance.this);
        doCast(_skill, DecoyInstance.this, true);
      }
      catch (Exception e)
      {
        _log.error("", e);
      }
    }
  }

  class DecoyLifetime extends RunnableImpl
  {
    DecoyLifetime()
    {
    }

    public void runImpl()
      throws Exception
    {
      try
      {
        decTimeRemaining(1000);
        double newTimeRemaining = getTimeRemaining();
        if (newTimeRemaining < 0.0D)
          unSummon();
      }
      catch (Exception e)
      {
        _log.error("", e);
      }
    }
  }
}