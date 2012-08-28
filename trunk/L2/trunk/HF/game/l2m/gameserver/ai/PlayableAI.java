package l2m.gameserver.ai;

import java.util.concurrent.ScheduledFuture;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.geodata.GeoEngine;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Playable;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Skill.NextAction;
import l2m.gameserver.model.Skill.SkillType;
import l2m.gameserver.model.Summon;
import l2m.gameserver.network.serverpackets.MyTargetSelected;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.utils.Location;
import org.slf4j.Logger;

public class PlayableAI extends CharacterAI
{
  private volatile int thinking = 0;

  protected Object _intention_arg0 = null; protected Object _intention_arg1 = null;
  protected Skill _skill;
  private nextAction _nextAction;
  private Object _nextAction_arg0;
  private Object _nextAction_arg1;
  private boolean _nextAction_arg2;
  private boolean _nextAction_arg3;
  protected boolean _forceUse;
  private boolean _dontMove;
  private ScheduledFuture<?> _followTask;

  public PlayableAI(Playable actor)
  {
    super(actor);
  }

  public void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
  {
    super.changeIntention(intention, arg0, arg1);
    _intention_arg0 = arg0;
    _intention_arg1 = arg1;
  }

  public void setIntention(CtrlIntention intention, Object arg0, Object arg1)
  {
    _intention_arg0 = null;
    _intention_arg1 = null;
    super.setIntention(intention, arg0, arg1);
  }

  protected void onIntentionCast(Skill skill, Creature target)
  {
    _skill = skill;
    super.onIntentionCast(skill, target);
  }

  public void setNextAction(nextAction action, Object arg0, Object arg1, boolean arg2, boolean arg3)
  {
    _nextAction = action;
    _nextAction_arg0 = arg0;
    _nextAction_arg1 = arg1;
    _nextAction_arg2 = arg2;
    _nextAction_arg3 = arg3;
  }

  public boolean setNextIntention()
  {
    nextAction nextAction = _nextAction;
    Object nextAction_arg0 = _nextAction_arg0;
    Object nextAction_arg1 = _nextAction_arg1;
    boolean nextAction_arg2 = _nextAction_arg2;
    boolean nextAction_arg3 = _nextAction_arg3;

    Playable actor = getActor();
    if ((nextAction == null) || (actor.isActionsDisabled()))
      return false;
    Creature target;
    GameObject object;
    switch (2.$SwitchMap$l2p$gameserver$ai$PlayableAI$nextAction[nextAction.ordinal()])
    {
    case 1:
      if (nextAction_arg0 == null)
        return false;
      target = (Creature)nextAction_arg0;
      _forceUse = nextAction_arg2;
      _dontMove = nextAction_arg3;
      clearNextAction();
      setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
      break;
    case 2:
      if ((nextAction_arg0 == null) || (nextAction_arg1 == null))
        return false;
      Skill skill = (Skill)nextAction_arg0;
      target = (Creature)nextAction_arg1;
      _forceUse = nextAction_arg2;
      _dontMove = nextAction_arg3;
      clearNextAction();
      if (!skill.checkCondition(actor, target, _forceUse, _dontMove, true))
      {
        if ((skill.getNextAction() == Skill.NextAction.ATTACK) && (!actor.equals(target)))
        {
          setNextAction(nextAction.ATTACK, target, null, _forceUse, false);
          return setNextIntention();
        }
        return false;
      }
      setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
      break;
    case 3:
      if ((nextAction_arg0 == null) || (nextAction_arg1 == null))
        return false;
      Location loc = (Location)nextAction_arg0;
      Integer offset = (Integer)nextAction_arg1;
      clearNextAction();
      actor.moveToLocation(loc, offset.intValue(), nextAction_arg2);
      break;
    case 4:
      actor.sitDown(null);
      break;
    case 5:
      if (nextAction_arg0 == null)
        return false;
      object = (GameObject)nextAction_arg0;
      clearNextAction();
      onIntentionInteract(object);
      break;
    case 6:
      if (nextAction_arg0 == null)
        return false;
      object = (GameObject)nextAction_arg0;
      clearNextAction();
      onIntentionPickUp(object);
      break;
    case 7:
      if ((nextAction_arg0 == null) || (nextAction_arg1 == null))
        return false;
      target = (Creature)nextAction_arg0;
      Integer socialId = (Integer)nextAction_arg1;
      _forceUse = nextAction_arg2;
      _nextAction = null;
      clearNextAction();
      onIntentionCoupleAction((Player)target, socialId);
      break;
    default:
      return false;
    }
    return true;
  }

  public void clearNextAction()
  {
    _nextAction = null;
    _nextAction_arg0 = null;
    _nextAction_arg1 = null;
    _nextAction_arg2 = false;
    _nextAction_arg3 = false;
  }

  protected void onEvtFinishCasting()
  {
    if (!setNextIntention())
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
  }

  protected void onEvtReadyToAct()
  {
    if (!setNextIntention())
      onEvtThink();
  }

  protected void onEvtArrived()
  {
    if (!setNextIntention())
      if ((getIntention() == CtrlIntention.AI_INTENTION_INTERACT) || (getIntention() == CtrlIntention.AI_INTENTION_PICK_UP))
        onEvtThink();
      else
        changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
  }

  protected void onEvtArrivedTarget()
  {
    switch (2.$SwitchMap$l2p$gameserver$ai$CtrlIntention[getIntention().ordinal()])
    {
    case 1:
      thinkAttack(false);
      break;
    case 2:
      thinkCast(false);
      break;
    case 3:
      thinkFollow();
      break;
    default:
      onEvtThink();
    }
  }

  protected final void onEvtThink()
  {
    Playable actor = getActor();
    if (actor.isActionsDisabled()) {
      return;
    }
    try
    {
      if (thinking++ > 1)
        return;
      switch (2.$SwitchMap$l2p$gameserver$ai$CtrlIntention[getIntention().ordinal()])
      {
      case 4:
        thinkActive();
        break;
      case 1:
        thinkAttack(true);
        break;
      case 2:
        thinkCast(true);
        break;
      case 5:
        thinkPickUp();
        break;
      case 6:
        thinkInteract();
        break;
      case 3:
        thinkFollow();
        break;
      case 7:
        thinkCoupleAction((Player)_intention_arg0, (Integer)_intention_arg1, false);
      }

    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      thinking -= 1;
    }
  }

  protected void thinkActive()
  {
  }

  protected void thinkFollow()
  {
    Playable actor = getActor();

    Creature target = (Creature)_intention_arg0;
    Integer offset = (Integer)_intention_arg1;

    if ((target == null) || (target.isAlikeDead()) || (actor.getDistance(target) > 4000.0D) || (offset == null))
    {
      clientActionFailed();
      return;
    }

    if ((actor.isFollow) && (actor.getFollowTarget() == target))
    {
      clientActionFailed();
      return;
    }

    if ((actor.isInRange(target, offset.intValue() + 20)) || (actor.isMovementDisabled())) {
      clientActionFailed();
    }
    if (_followTask != null)
    {
      _followTask.cancel(false);
      _followTask = null;
    }

    _followTask = ThreadPoolManager.getInstance().schedule(new ThinkFollow(), 250L);
  }

  protected void onIntentionInteract(GameObject object)
  {
    Playable actor = getActor();

    if (actor.isActionsDisabled())
    {
      setNextAction(nextAction.INTERACT, object, null, false, false);
      clientActionFailed();
      return;
    }

    clearNextAction();
    changeIntention(CtrlIntention.AI_INTENTION_INTERACT, object, null);
    onEvtThink();
  }

  protected void onIntentionCoupleAction(Player player, Integer socialId)
  {
    clearNextAction();
    changeIntention(CtrlIntention.AI_INTENTION_COUPLE_ACTION, player, socialId);
    onEvtThink();
  }

  protected void thinkInteract()
  {
    Playable actor = getActor();

    GameObject target = (GameObject)_intention_arg0;

    if (target == null)
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      return;
    }

    int range = (int)(Math.max(30.0D, actor.getMinDistance(target)) + 20.0D);

    if (actor.isInRangeZ(target, range))
    {
      if (actor.isPlayer())
        ((Player)actor).doInteract(target);
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }
    else
    {
      actor.moveToLocation(target.getLoc(), 40, true);
      setNextAction(nextAction.INTERACT, target, null, false, false);
    }
  }

  protected void onIntentionPickUp(GameObject object)
  {
    Playable actor = getActor();

    if (actor.isActionsDisabled())
    {
      setNextAction(nextAction.PICKUP, object, null, false, false);
      clientActionFailed();
      return;
    }

    clearNextAction();
    changeIntention(CtrlIntention.AI_INTENTION_PICK_UP, object, null);
    onEvtThink();
  }

  protected void thinkPickUp()
  {
    Playable actor = getActor();

    GameObject target = (GameObject)_intention_arg0;

    if (target == null)
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      return;
    }

    if ((actor.isInRange(target, 30L)) && (Math.abs(actor.getZ() - target.getZ()) < 50))
    {
      if ((actor.isPlayer()) || (actor.isPet()))
        actor.doPickupItem(target);
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }
    else {
      ThreadPoolManager.getInstance().execute(new RunnableImpl(actor, target)
      {
        public void runImpl()
        {
          val$actor.moveToLocation(val$target.getLoc(), 10, true);
          setNextAction(PlayableAI.nextAction.PICKUP, val$target, null, false, false);
        } } );
    }
  }

  protected void thinkAttack(boolean checkRange) {
    Playable actor = getActor();

    Player player = actor.getPlayer();
    if (player == null)
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      return;
    }

    if ((actor.isActionsDisabled()) || (actor.isAttackingDisabled()))
    {
      actor.sendActionFailed();
      return;
    }

    boolean isPosessed = ((actor instanceof Summon)) && (((Summon)actor).isDepressed());

    Creature attack_target = getAttackTarget();
    if ((attack_target == null) || (attack_target.isDead()) || ((!isPosessed) && (_forceUse ? !attack_target.isAttackable(actor) : !attack_target.isAutoAttackable(actor))))
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      actor.sendActionFailed();
      return;
    }

    if (!checkRange)
    {
      clientStopMoving();
      actor.doAttack(attack_target);
      return;
    }

    int range = actor.getPhysicalAttackRange();
    if (range < 10) {
      range = 10;
    }
    boolean canSee = GeoEngine.canSeeTarget(actor, attack_target, false);

    if ((!canSee) && ((range > 200) || (Math.abs(actor.getZ() - attack_target.getZ()) > 200)))
    {
      actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      actor.sendActionFailed();
      return;
    }

    range = (int)(range + actor.getMinDistance(attack_target));

    if (actor.isFakeDeath()) {
      actor.breakFakeDeath();
    }
    if (actor.isInRangeZ(attack_target, range))
    {
      if (!canSee)
      {
        actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        actor.sendActionFailed();
        return;
      }

      clientStopMoving(false);
      actor.doAttack(attack_target);
    }
    else if (!_dontMove) {
      ThreadPoolManager.getInstance().execute(new ExecuteFollow(attack_target, range - 20));
    } else {
      actor.sendActionFailed();
    }
  }

  protected void thinkCast(boolean checkRange) {
    Playable actor = getActor();

    Creature target = getAttackTarget();

    if ((_skill.getSkillType() == Skill.SkillType.CRAFT) || (_skill.isToggle()))
    {
      if (_skill.checkCondition(actor, target, _forceUse, _dontMove, true))
        actor.doCast(_skill, target, _forceUse);
      return;
    }

    if ((target == null) || ((target.isDead() != _skill.getCorpse()) && (!_skill.isNotTargetAoE())))
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      actor.sendActionFailed();
      return;
    }

    if (!checkRange)
    {
      if ((_skill.getNextAction() == Skill.NextAction.ATTACK) && (!actor.equals(target)))
        setNextAction(nextAction.ATTACK, target, null, _forceUse, false);
      else {
        clearNextAction();
      }
      clientStopMoving();

      if (_skill.checkCondition(actor, target, _forceUse, _dontMove, true)) {
        actor.doCast(_skill, target, _forceUse);
      }
      else {
        setNextIntention();
        if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK) {
          thinkAttack(true);
        }
      }
      return;
    }

    int range = actor.getMagicalAttackRange(_skill);
    if (range < 10) {
      range = 10;
    }
    boolean canSee = (_skill.getSkillType() == Skill.SkillType.TAKECASTLE) || (_skill.getSkillType() == Skill.SkillType.TAKEFORTRESS) || (GeoEngine.canSeeTarget(actor, target, actor.isFlying()));
    boolean noRangeSkill = _skill.getCastRange() == 32767;

    if ((!noRangeSkill) && (!canSee) && ((range > 200) || (Math.abs(actor.getZ() - target.getZ()) > 200)))
    {
      actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      actor.sendActionFailed();
      return;
    }

    range = (int)(range + actor.getMinDistance(target));

    if (actor.isFakeDeath()) {
      actor.breakFakeDeath();
    }
    if ((actor.isInRangeZ(target, range)) || (noRangeSkill))
    {
      if ((!noRangeSkill) && (!canSee))
      {
        actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        actor.sendActionFailed();
        return;
      }

      if ((_skill.getNextAction() == Skill.NextAction.ATTACK) && (!actor.equals(target)))
        setNextAction(nextAction.ATTACK, target, null, _forceUse, false);
      else {
        clearNextAction();
      }
      if (_skill.checkCondition(actor, target, _forceUse, _dontMove, true))
      {
        clientStopMoving(false);
        actor.doCast(_skill, target, _forceUse);
      }
      else
      {
        setNextIntention();
        if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
          thinkAttack(true);
      }
    }
    else if (!_dontMove) {
      ThreadPoolManager.getInstance().execute(new ExecuteFollow(target, range - 20));
    }
    else {
      actor.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      actor.sendActionFailed();
    }
  }

  protected void thinkCoupleAction(Player target, Integer socialId, boolean cancel)
  {
  }

  protected void onEvtDead(Creature killer)
  {
    clearNextAction();
    super.onEvtDead(killer);
  }

  protected void onEvtFakeDeath()
  {
    clearNextAction();
    super.onEvtFakeDeath();
  }

  public void lockTarget(Creature target)
  {
    Playable actor = getActor();

    if ((target == null) || (target.isDead())) {
      actor.setAggressionTarget(null);
    } else if (actor.getAggressionTarget() == null)
    {
      GameObject actorStoredTarget = actor.getTarget();
      actor.setAggressionTarget(target);
      actor.setTarget(target);

      clearNextAction();

      if (actorStoredTarget != target)
        actor.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
    }
  }

  public void Attack(GameObject target, boolean forceUse, boolean dontMove)
  {
    Playable actor = getActor();

    if ((target.isCreature()) && ((actor.isActionsDisabled()) || (actor.isAttackingDisabled())))
    {
      setNextAction(nextAction.ATTACK, target, null, forceUse, false);
      actor.sendActionFailed();
      return;
    }

    _dontMove = dontMove;
    _forceUse = forceUse;
    clearNextAction();
    setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
  }

  public void Cast(Skill skill, Creature target, boolean forceUse, boolean dontMove)
  {
    Playable actor = getActor();

    if ((skill.altUse()) || (skill.isToggle()))
    {
      if (((skill.isToggle()) || (skill.isHandler())) && ((actor.isOutOfControl()) || (actor.isStunned()) || (actor.isSleeping()) || (actor.isParalyzed()) || (actor.isAlikeDead())))
        clientActionFailed();
      else
        actor.altUseSkill(skill, target);
      return;
    }

    if (actor.isActionsDisabled())
    {
      setNextAction(nextAction.CAST, skill, target, forceUse, dontMove);
      clientActionFailed();
      return;
    }

    _forceUse = forceUse;
    _dontMove = dontMove;
    clearNextAction();
    setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
  }

  public Playable getActor()
  {
    return (Playable)super.getActor();
  }

  protected class ExecuteFollow extends RunnableImpl
  {
    private Creature _target;
    private int _range;

    public ExecuteFollow(Creature target, int range)
    {
      _target = target;
      _range = range;
    }

    public void runImpl()
    {
      if (_target.isDoor())
        _actor.moveToLocation(_target.getLoc(), 40, true);
      else
        _actor.followToCharacter(_target, _range, true);
    }
  }

  protected class ThinkFollow extends RunnableImpl
  {
    protected ThinkFollow()
    {
    }

    public void runImpl()
      throws Exception
    {
      Playable actor = getActor();

      if (getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
      {
        if (((actor.isPet()) || (actor.isSummon())) && (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE))
          ((Summon)actor).setFollowMode(false);
        return;
      }

      Creature target = (Creature)_intention_arg0;
      int offset = (_intention_arg1 instanceof Integer) ? ((Integer)_intention_arg1).intValue() : 0;

      if ((target == null) || (target.isAlikeDead()) || (actor.getDistance(target) > 4000.0D))
      {
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        return;
      }

      Player player = actor.getPlayer();
      if ((player == null) || (player.isLogoutStarted()) || (((actor.isPet()) || (actor.isSummon())) && (player.getPet() != actor)))
      {
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        return;
      }

      if ((!actor.isInRange(target, offset + 20)) && ((!actor.isFollow) || (actor.getFollowTarget() != target)))
        actor.followToCharacter(target, offset, false);
      PlayableAI.access$002(PlayableAI.this, ThreadPoolManager.getInstance().schedule(this, 250L));
    }
  }

  public static enum nextAction
  {
    ATTACK, 
    CAST, 
    MOVE, 
    REST, 
    PICKUP, 
    INTERACT, 
    COUPLE_ACTION;
  }
}