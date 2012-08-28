package net.sf.l2j.gameserver.ai;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStop;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.util.PeaceZone;
import net.sf.l2j.util.Point3D;

public class L2CharacterAI extends AbstractAI
{
  private static final int ZONE_PVP = 1;

  protected void onEvtAttacked(L2Character attacker)
  {
    clientStartAutoAttack();
  }

  public L2CharacterAI(L2Character.AIAccessor accessor)
  {
    super(accessor);
  }

  protected void onIntentionIdle()
  {
    changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);

    setCastTarget(null);
    setAttackTarget(null);

    clientStopMoving(null);

    clientStopAutoAttack();
  }

  protected void onIntentionActive()
  {
    if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
    {
      changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);

      setCastTarget(null);
      setAttackTarget(null);

      clientStopMoving(null);

      clientStopAutoAttack();

      onEvtThink();
    }
  }

  protected void onIntentionRest()
  {
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
  }

  protected void onIntentionAttack(L2Character target)
  {
    if (target == null) {
      clientActionFailed();
      return;
    }

    if (getIntention() == CtrlIntention.AI_INTENTION_REST)
    {
      clientActionFailed();
      return;
    }

    if ((_actor.isAllSkillsDisabled()) || (_actor.isAttackingDisabled()) || (_actor.isAfraid()))
    {
      clientActionFailed();
      return;
    }

    if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
    {
      if (getAttackTarget() != target)
      {
        setAttackTarget(target);

        stopFollow();

        notifyEvent(CtrlEvent.EVT_THINK, null);
      }
      else {
        clientActionFailed();
      }
    }
    else {
      changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);

      setAttackTarget(target);

      stopFollow();

      notifyEvent(CtrlEvent.EVT_THINK, null);
    }
  }

  protected void onIntentionCast(L2Skill skill, L2Object target)
  {
    if (target == null) {
      clientActionFailed();
      return;
    }

    if ((getIntention() == CtrlIntention.AI_INTENTION_REST) && (skill.isMagic())) {
      clientActionFailed();
      return;
    }

    if (_actor.isAllSkillsDisabled())
    {
      clientActionFailed();
      return;
    }

    if ((_actor.isMuted()) && (skill.isMagic()))
    {
      clientActionFailed();
      return;
    }

    if ((_actor.isPlayer()) && (_actor.getKarma() > 0) && (target.isPlayer()) && 
      (target.getProtectionBlessing()) && (_actor.getLevel() - target.getLevel() >= 10) && (!target.isInsidePvpZone()))
    {
      clientActionFailed();
      return;
    }

    setCastTarget((L2Character)target);

    if (skill.getHitTime() > 50)
    {
      _actor.abortAttack();
    }

    _skill = skill;

    changeIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);

    notifyEvent(CtrlEvent.EVT_THINK, null);
  }

  protected void onIntentionMoveTo(L2CharPosition pos)
  {
    if (getIntention() == CtrlIntention.AI_INTENTION_REST)
    {
      clientActionFailed();
      return;
    }

    if (_actor.isAllSkillsDisabled())
    {
      clientActionFailed();
      return;
    }
    if (_actor.isAttackingNow())
    {
      _actor.setNextLoc(pos.x, pos.y, pos.z);
      clientActionFailed();
      return;
    }

    changeIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos, null);

    clientStopAutoAttack();

    _actor.abortAttack();

    moveTo(pos.x, pos.y, pos.z);
  }

  protected void onIntentionMoveToInABoat(L2CharPosition destination, L2CharPosition origin)
  {
    if (getIntention() == CtrlIntention.AI_INTENTION_REST)
    {
      clientActionFailed();
      return;
    }

    if (_actor.isAllSkillsDisabled())
    {
      clientActionFailed();
      return;
    }

    clientStopAutoAttack();

    _actor.abortAttack();

    moveToInABoat(destination, origin);
  }

  protected void onIntentionFollow(L2Character target)
  {
    if (getIntention() == CtrlIntention.AI_INTENTION_REST)
    {
      clientActionFailed();
      return;
    }

    if (_actor.isAllSkillsDisabled())
    {
      clientActionFailed();
      return;
    }

    if ((_actor.isImobilised()) || (_actor.isRooted()))
    {
      clientActionFailed();
      return;
    }

    if (_actor.isDead()) {
      clientActionFailed();
      return;
    }

    if (_actor == target) {
      clientActionFailed();
      return;
    }

    clientStopAutoAttack();

    changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, null);

    startFollow(target);
  }

  protected void onIntentionPickUp(L2Object object)
  {
    if (getIntention() == CtrlIntention.AI_INTENTION_REST)
    {
      clientActionFailed();
      return;
    }

    if (_actor.isAllSkillsDisabled())
    {
      clientActionFailed();
      return;
    }

    clientStopAutoAttack();

    changeIntention(CtrlIntention.AI_INTENTION_PICK_UP, object, null);

    setTarget(object);
    if ((object.getX() == 0) && (object.getY() == 0))
    {
      _log.warning("Object in coords 0,0 - using a temporary fix");
      object.setXYZ(getActor().getX(), getActor().getY(), getActor().getZ() + 5);
    }

    moveToPawn(object, 20);
  }

  protected void onIntentionInteract(L2Object object)
  {
    if (getIntention() == CtrlIntention.AI_INTENTION_REST)
    {
      clientActionFailed();
      return;
    }

    if (_actor.isAllSkillsDisabled())
    {
      clientActionFailed();
      return;
    }

    clientStopAutoAttack();

    if (getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
    {
      changeIntention(CtrlIntention.AI_INTENTION_INTERACT, object, null);

      setTarget(object);

      moveToPawn(object, 60);
    }
  }

  protected void onEvtThink()
  {
  }

  protected void onEvtAggression(L2Character target, int aggro)
  {
  }

  protected void onEvtStunned(L2Character attacker)
  {
    _actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
    if (AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor)) {
      AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
    }

    setAutoAttacking(false);

    clientStopMoving(null);

    onEvtAttacked(attacker);
  }

  protected void onEvtSleeping(L2Character attacker)
  {
    _actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
    if (AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor)) {
      AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
    }

    setAutoAttacking(false);

    clientStopMoving(null);
  }

  protected void onEvtRooted(L2Character attacker)
  {
    clientStopMoving(null);

    onEvtAttacked(attacker);
  }

  protected void onEvtConfused(L2Character attacker)
  {
    clientStopMoving(null);

    onEvtAttacked(attacker);
  }

  protected void onEvtMuted(L2Character attacker)
  {
    onEvtAttacked(attacker);
  }

  protected void onEvtReadyToAct()
  {
    onEvtThink();
  }

  protected void onEvtUserCmd(Object arg0, Object arg1)
  {
  }

  protected void onEvtArrived()
  {
    if (_accessor.getActor().isPlayer())
      _accessor.getActor().getPlayer().revalidateZone(true);
    else {
      _accessor.getActor().revalidateZone();
    }

    if (_accessor.getActor().moveToNextRoutePoint()) {
      return;
    }

    clientStoppedMoving();

    if (getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO) {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }

    onEvtThink();

    if ((_actor instanceof L2BoatInstance))
      ((L2BoatInstance)_actor).evtArrived();
  }

  protected void onEvtArrivedRevalidate()
  {
    onEvtThink();
  }

  protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos)
  {
    clientStopMoving(blocked_at_pos);

    if (getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO) {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }

    onEvtThink();
  }

  protected void onEvtForgetObject(L2Object object)
  {
    if (getTarget() == object) {
      setTarget(null);

      if (getIntention() == CtrlIntention.AI_INTENTION_INTERACT)
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      else if (getIntention() == CtrlIntention.AI_INTENTION_PICK_UP) {
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      }

    }

    if (getAttackTarget() == object)
    {
      setAttackTarget(null);

      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }

    if (getCastTarget() == object)
    {
      setCastTarget(null);

      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }

    if (getFollowTarget() == object)
    {
      clientStopMoving(null);

      stopFollow();

      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }

    if (_actor == object)
    {
      setTarget(null);
      setAttackTarget(null);
      setCastTarget(null);

      stopFollow();

      clientStopMoving(null);

      changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
    }
  }

  protected void onEvtCancel()
  {
    stopFollow();

    if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor)) {
      _actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
    }

    onEvtThink();
  }

  protected void onEvtDead()
  {
    stopFollow();

    clientNotifyDead();

    if (!_actor.isPlayer())
      _actor.setWalking();
  }

  protected void onEvtFakeDeath()
  {
    stopFollow();

    clientStopMoving(null);

    _intention = CtrlIntention.AI_INTENTION_IDLE;
    setTarget(null);
    setCastTarget(null);
    setAttackTarget(null);
  }

  protected void onEvtFinishCasting()
  {
  }

  protected boolean maybeMoveToPosition(Point3D worldPosition, int offset)
  {
    if (worldPosition == null) {
      _log.warning("maybeMoveToPosition: worldPosition == NULL!");
      return false;
    }

    if (offset < 0) {
      return false;
    }
    if (!_actor.isInsideRadius(worldPosition.getX(), worldPosition.getY(), offset + _actor.getTemplate().collisionRadius, false)) {
      if (_actor.isMovementDisabled()) {
        return true;
      }

      if ((!_actor.isRunning()) && (!(this instanceof L2PlayerAI))) {
        _actor.setRunning();
      }

      stopFollow();

      int x = _actor.getX();
      int y = _actor.getY();

      double dx = worldPosition.getX() - x;
      double dy = worldPosition.getY() - y;

      double dist = Math.sqrt(dx * dx + dy * dy);

      double sin = dy / dist;
      double cos = dx / dist;

      dist -= offset - 5;

      x += (int)(dist * cos);
      y += (int)(dist * sin);

      moveTo(x, y, worldPosition.getZ());
      return true;
    }

    if (getFollowTarget() != null) {
      stopFollow();
    }

    return false;
  }

  protected boolean maybeMoveToPawn(L2Object target, int offset)
  {
    if (target == null) {
      _log.warning("maybeMoveToPawn: target == NULL!");
      return false;
    }
    if (offset < 0) {
      return false;
    }
    offset += _actor.getTemplate().collisionRadius;
    if (target.isL2Character()) {
      offset += ((L2Character)target).getTemplate().collisionRadius;
    }

    if (!_actor.isInsideRadius(target, offset, false, false))
    {
      if (getFollowTarget() != null)
      {
        if ((getAttackTarget() != null) && (_actor.isL2Playable()) && (target.isL2Playable()) && 
          (getAttackTarget() == getFollowTarget()))
        {
          if (PeaceZone.getInstance().inPeace(_actor, target)) {
            stopFollow();
            setIntention(CtrlIntention.AI_INTENTION_IDLE);
            return true;
          }

        }

        if (!_actor.isInsideRadius(target, 2000, false, false)) {
          stopFollow();
          setIntention(CtrlIntention.AI_INTENTION_IDLE);
          return true;
        }

        if (!_actor.isInsideRadius(target, offset + 100, false, false)) {
          return true;
        }
        stopFollow();
        return false;
      }

      if (_actor.isMovementDisabled()) {
        return true;
      }

      if ((!_actor.isRunning()) && (!(this instanceof L2PlayerAI))) {
        _actor.setRunning();
      }

      stopFollow();
      if ((target.isL2Character()) && (!target.isL2Door())) {
        if (((L2Character)target).isMoving()) {
          offset -= 100;
        }
        if (offset < 5) {
          offset = 5;
        }

        startFollow((L2Character)target, offset);
      }
      else {
        moveToPawn(target, offset);
      }
      return true;
    }

    if (getFollowTarget() != null) {
      stopFollow();
    }

    return false;
  }

  protected boolean checkTargetLostOrDead(L2Character target)
  {
    if ((target == null) || (target.isAlikeDead()))
    {
      if ((target != null) && (target.isFakeDeath())) {
        target.stopFakeDeath(null);
        return false;
      }

      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      return true;
    }
    return false;
  }

  protected boolean checkTargetLost(L2Object target)
  {
    if (target == null)
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      return true;
    }

    if ((target.isPlayer()) && (target.getPlayer().isFakeDeath())) {
      target.getPlayer().stopFakeDeath(null);
      return false;
    }

    return false;
  }

  public int getPAtk() {
    return 1000;
  }

  public int getMDef() {
    return 1000;
  }

  public int getPAtkSpd() {
    return 600;
  }

  public int getPDef() {
    return 1000;
  }

  public int getMAtk() {
    return 1000;
  }

  public int getMAtkSpd() {
    return 400;
  }

  public int getMaxHp() {
    return 400;
  }

  public void onOwnerGotAttacked(L2Character attacker)
  {
  }
}