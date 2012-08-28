package l2p.gameserver.model.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObjectTasks.DeleteTask;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Skill.SkillTargetType;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MyTargetSelected;
import l2p.gameserver.serverpackets.NpcInfo;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.taskmanager.EffectTaskManager;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;

public final class TrapInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;
  private final HardReference<? extends Creature> _ownerRef;
  private final Skill _skill;
  private ScheduledFuture<?> _targetTask;
  private ScheduledFuture<?> _destroyTask;
  private boolean _detected;

  public TrapInstance(int objectId, NpcTemplate template, Creature owner, Skill skill)
  {
    this(objectId, template, owner, skill, owner.getLoc());
  }

  public TrapInstance(int objectId, NpcTemplate template, Creature owner, Skill skill, Location loc)
  {
    super(objectId, template);
    _ownerRef = owner.getRef();
    _skill = skill;

    setReflection(owner.getReflection());
    setLevel(owner.getLevel());
    setTitle(owner.getName());
    setLoc(loc);
  }

  public boolean isTrap()
  {
    return true;
  }

  public Creature getOwner()
  {
    return (Creature)_ownerRef.get();
  }

  protected void onSpawn()
  {
    super.onSpawn();

    _destroyTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(this), 120000L);

    _targetTask = EffectTaskManager.getInstance().scheduleAtFixedRate(new CastTask(this), 250L, 250L);
  }

  public void broadcastCharInfo()
  {
    if (!isDetected())
      return;
    super.broadcastCharInfo();
  }

  protected void onDelete()
  {
    Creature owner = getOwner();
    if ((owner != null) && (owner.isPlayer()))
      ((Player)owner).removeTrap(this);
    if (_destroyTask != null)
      _destroyTask.cancel(false);
    _destroyTask = null;
    if (_targetTask != null)
      _targetTask.cancel(false);
    _targetTask = null;
    super.onDelete();
  }

  public boolean isDetected()
  {
    return _detected;
  }

  public void setDetected(boolean detected)
  {
    _detected = detected;
  }

  public int getPAtk(Creature target)
  {
    Creature owner = getOwner();
    return owner == null ? 0 : owner.getPAtk(target);
  }

  public int getMAtk(Creature target, Skill skill)
  {
    Creature owner = getOwner();
    return owner == null ? 0 : owner.getMAtk(target, skill);
  }

  public boolean hasRandomAnimation()
  {
    return false;
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    return false;
  }

  public boolean isAttackable(Creature attacker)
  {
    return false;
  }

  public boolean isInvul()
  {
    return true;
  }

  public boolean isFearImmune()
  {
    return true;
  }

  public boolean isParalyzeImmune()
  {
    return true;
  }

  public boolean isLethalImmune()
  {
    return true;
  }

  public void showChatWindow(Player player, int val, Object[] arg)
  {
  }

  public void showChatWindow(Player player, String filename, Object[] replace)
  {
  }

  public void onBypassFeedback(Player player, String command)
  {
  }

  public void onAction(Player player, boolean shift)
  {
    if (player.getTarget() != this)
    {
      player.setTarget(this);
      if (player.getTarget() == this)
        player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));
    }
    player.sendActionFailed();
  }

  public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
  {
    if ((!isDetected()) && (getOwner() != forPlayer)) {
      return Collections.emptyList();
    }
    return Collections.singletonList(new NpcInfo(this, forPlayer));
  }

  private static class CastTask extends RunnableImpl
  {
    private HardReference<NpcInstance> _trapRef;

    public CastTask(TrapInstance trap)
    {
      _trapRef = trap.getRef();
    }

    public void runImpl()
      throws Exception
    {
      TrapInstance trap = (TrapInstance)_trapRef.get();

      if (trap == null) {
        return;
      }
      Creature owner = trap.getOwner();
      if (owner == null) {
        return;
      }
      for (Creature target : trap.getAroundCharacters(200, 200))
        if ((target != owner) && 
          (trap._skill.checkTarget(owner, target, null, false, false) == null))
        {
          List targets = new ArrayList();
          if (trap._skill.getTargetType() != Skill.SkillTargetType.TARGET_AREA)
            targets.add(target);
          else {
            for (Creature t : trap.getAroundCharacters(trap._skill.getSkillRadius(), 128))
              if (trap._skill.checkTarget(owner, t, null, false, false) == null)
                targets.add(target);
          }
          trap._skill.useSkill(trap, targets);
          if (target.isPlayer())
            target.sendMessage(new CustomMessage("common.Trap", target.getPlayer(), new Object[0]));
          trap.deleteMe();
          break;
        }
    }
  }
}