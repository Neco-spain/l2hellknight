package net.sf.l2j.gameserver.ai;

import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Universe;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStop;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate.AIType;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Point3D;
import net.sf.l2j.util.Rnd;

public class L2CharacterAI extends AbstractAI
{
  protected boolean _sitDownAfterAction = false;

  public L2CharacterAI(L2Character.AIAccessor accessor)
  {
    super(accessor);
  }

  public IntentionCommand getNextIntention()
  {
    return null;
  }

  protected void onEvtAttacked(L2Character attacker)
  {
    clientStartAutoAttack();
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

      if ((_actor instanceof L2Attackable)) {
        ((L2NpcInstance)_actor).startRandomAnimationTimer();
      }

      onEvtThink();
    }
  }

  protected void onIntentionRest()
  {
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
  }

  protected void onIntentionAttack(L2Character target)
  {
    if (target == null)
    {
      clientActionFailed();
      return;
    }

    if (getIntention() == CtrlIntention.AI_INTENTION_REST)
    {
      clientActionFailed();
      return;
    }

    if ((_actor.isAllSkillsDisabled()) || (_actor.isAfraid()))
    {
      clientActionFailed();
      return;
    }
    if (((target instanceof L2PcInstance)) && ((_actor instanceof L2PcInstance)))
    {
      if ((((L2PcInstance)_actor).getKarma() > 0) && (_actor.getLevel() - target.getLevel() >= 10) && (((L2PlayableInstance)target).getProtectionBlessing()) && (!target.isInsideZone(1)))
      {
        clientActionFailed();
        return;
      }
    }

    if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
    {
      if (getAttackTarget() != target)
      {
        setAttackTarget(target);

        stopFollow();

        notifyEvent(CtrlEvent.EVT_THINK, null);
      }
      else
      {
        clientActionFailed();
      }
    }
    else
    {
      changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);

      setAttackTarget(target);

      stopFollow();

      notifyEvent(CtrlEvent.EVT_THINK, null);
    }
  }

  protected void onIntentionCast(L2Skill skill, L2Object target)
  {
    if ((getIntention() == CtrlIntention.AI_INTENTION_REST) && (skill.isMagic()))
    {
      clientActionFailed();
      return;
    }

    if (((target instanceof L2PcInstance)) && ((_actor instanceof L2PcInstance)))
    {
      if ((((L2PcInstance)_actor).getKarma() > 0) && (_actor.getLevel() - ((L2PcInstance)target).getLevel() >= 10) && (((L2PlayableInstance)target).getProtectionBlessing()) && (!((L2Character)target).isInsideZone(1)))
      {
        clientActionFailed();
        return;
      }
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

    setCastTarget((L2Character)target);

    if (skill.getHitTime() > 50)
    {
      _actor.abortAttack();
    }

    setSitDownAfterAction(false);

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

    setSitDownAfterAction(false);

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

    if (_actor.isDead())
    {
      clientActionFailed();
      return;
    }

    if (_actor == target)
    {
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

    if ((object.getX() == 0) && (object.getY() == 0))
    {
      L2Character player_char = getActor();

      if ((player_char instanceof L2PcInstance)) {
        L2PcInstance player = (L2PcInstance)player_char;

        _log.severe("ATTENTION: Player " + player.getName() + " is trying to use pickup dupe");
        player.sendMessage("\u0412\u044B \u043F\u043E\u043F\u044B\u0442\u0430\u043B\u0438\u0441\u044C \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u044D\u043A\u0441\u043F\u043B\u043E\u0439\u0442 \u043D\u0430 \u043F\u043E\u0434\u0431\u043E\u0440 \u0432\u0435\u0449\u0438, \u043A\u0438\u043A.");
        clientActionFailed();
        Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " has used pickup dupe! Kicked ", Config.DEFAULT_PUNISH);
        player.closeNetConnection(false);
        return;
      }

      object.setXYZ(getActor().getX(), getActor().getY(), getActor().getZ() + 5);
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
    if ((Config.ACTIVATE_POSITION_RECORDER) && ((_accessor.getActor() instanceof L2PcInstance)))
    {
      ((L2PcInstance)_accessor.getActor()).explore();
    }
    _accessor.getActor().revalidateZone(true);

    if (_accessor.getActor().moveToNextRoutePoint()) {
      return;
    }
    if ((_accessor.getActor() instanceof L2Attackable))
    {
      ((L2Attackable)_accessor.getActor()).setisReturningToSpawnPoint(false);
    }
    clientStoppedMoving();

    if (getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
    {
      if ((_sitDownAfterAction) && ((_actor instanceof L2PcInstance)))
      {
        ((L2PcInstance)_actor).sitDown();
        _sitDownAfterAction = false;
      }

      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }

    onEvtThink();

    if ((_actor instanceof L2BoatInstance))
    {
      ((L2BoatInstance)_actor).evtArrived();
    }
  }

  protected void onEvtArrivedRevalidate()
  {
    onEvtThink();
  }

  protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos)
  {
    clientStopMoving(blocked_at_pos);

    if ((Config.ACTIVATE_POSITION_RECORDER) && (Universe.getInstance().shouldLog(Integer.valueOf(_accessor.getActor().getObjectId()))))
    {
      if (!_accessor.getActor().isFlying()) {
        Universe.getInstance().registerObstacle(blocked_at_pos.x, blocked_at_pos.y, blocked_at_pos.z);
      }
      if ((_accessor.getActor() instanceof L2PcInstance)) {
        ((L2PcInstance)_accessor.getActor()).explore();
      }
    }

    if (getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO) setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

    onEvtThink();
  }

  protected void onEvtForgetObject(L2Object object)
  {
    if (getTarget() == object)
    {
      setTarget(null);

      if (getIntention() == CtrlIntention.AI_INTENTION_INTERACT) setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      else if (getIntention() == CtrlIntention.AI_INTENTION_PICK_UP) setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

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

    if (!(_actor instanceof L2PcInstance))
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
    if (worldPosition == null)
    {
      _log.warning("maybeMoveToPosition: worldPosition == NULL!");
      return false;
    }

    if (offset < 0) {
      return false;
    }
    if (!_actor.isInsideRadius(worldPosition.getX(), worldPosition.getY(), offset + _actor.getTemplate().collisionRadius, false))
    {
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
    if (target == null)
    {
      _log.warning("maybeMoveToPawn: target == NULL!");
      return false;
    }
    if (offset < 0) return false;

    offset += _actor.getTemplate().collisionRadius;
    if ((target instanceof L2Character)) {
      offset += ((L2Character)target).getTemplate().collisionRadius;
    }
    if (!_actor.isInsideRadius(target, offset, false, false))
    {
      if (getFollowTarget() != null)
      {
        if ((getAttackTarget() != null) && ((_actor instanceof L2PlayableInstance)) && ((target instanceof L2PlayableInstance)))
        {
          if (getAttackTarget() == getFollowTarget())
          {
            boolean isGM = (_actor instanceof L2PcInstance) ? ((L2PcInstance)_actor).isGM() : false;
            if ((((L2PlayableInstance)_actor).isInsidePeaceZone(_actor, target)) && (!isGM))
            {
              stopFollow();
              setIntention(CtrlIntention.AI_INTENTION_IDLE);
              return true;
            }
          }
        }

        if (!_actor.isInsideRadius(target, 2000, false, false))
        {
          stopFollow();
          setIntention(CtrlIntention.AI_INTENTION_IDLE);
          return true;
        }

        if (!_actor.isInsideRadius(target, offset + 100, false, false)) return true;
        stopFollow();
        return false;
      }

      if (_actor.isMovementDisabled()) return true;

      if ((!_actor.isRunning()) && (!(this instanceof L2PlayerAI))) _actor.setRunning();

      stopFollow();
      if (((target instanceof L2Character)) && (!(target instanceof L2DoorInstance)))
      {
        if (((L2Character)target).isMoving()) offset -= 100;
        if (offset < 5) offset = 5;

        startFollow((L2Character)target, offset);
      }
      else
      {
        moveToPawn(target, offset);
      }
      return true;
    }

    if (getFollowTarget() != null) stopFollow();

    return false;
  }

  protected boolean checkTargetLostOrDead(L2Character target)
  {
    if ((target == null) || (target.isAlikeDead()))
    {
      if ((target != null) && (target.isFakeDeath()))
      {
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
    if ((target instanceof L2PcInstance))
    {
      L2PcInstance target2 = (L2PcInstance)target;

      if (target2.isFakeDeath())
      {
        target2.stopFakeDeath(null);
        return false;
      }
    }
    if (target == null)
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      return true;
    }
    return false;
  }

  public void setSitDownAfterAction(boolean val)
  {
    _sitDownAfterAction = val;
  }

  protected class SelfAnalysis
  {
    public boolean isMage = false;
    public boolean isBalanced;
    public boolean isArcher = false;
    public boolean isFighter = false;
    public boolean cannotMoveOnLand = false;
    public List<L2Skill> generalSkills = new FastList();
    public List<L2Skill> buffSkills = new FastList();
    public int lastBuffTick = 0;
    public List<L2Skill> debuffSkills = new FastList();
    public int lastDebuffTick = 0;
    public List<L2Skill> cancelSkills = new FastList();
    public List<L2Skill> healSkills = new FastList();

    public List<L2Skill> generalDisablers = new FastList();
    public List<L2Skill> sleepSkills = new FastList();
    public List<L2Skill> rootSkills = new FastList();
    public List<L2Skill> muteSkills = new FastList();
    public List<L2Skill> resurrectSkills = new FastList();
    public boolean hasHealOrResurrect = false;
    public boolean hasLongRangeSkills = false;
    public boolean hasLongRangeDamageSkills = false;
    public int maxCastRange = 0;

    public SelfAnalysis()
    {
    }

    public void init()
    {
      switch (L2CharacterAI.1.$SwitchMap$net$sf$l2j$gameserver$templates$L2NpcTemplate$AIType[((net.sf.l2j.gameserver.templates.L2NpcTemplate)_actor.getTemplate()).AI.ordinal()])
      {
      case 1:
        isFighter = true;
        break;
      case 2:
        isMage = true;
        break;
      case 3:
        isBalanced = true;
        break;
      case 4:
        isArcher = true;
        break;
      default:
        isFighter = true;
      }

      if ((_actor instanceof L2NpcInstance))
      {
        int npcId = ((L2NpcInstance)_actor).getNpcId();

        switch (npcId)
        {
        case 20314:
        case 20849:
          cannotMoveOnLand = true;
          break;
        default:
          cannotMoveOnLand = false;
        }

      }

      for (L2Skill sk : _actor.getAllSkills())
      {
        if (!sk.isPassive()) {
          int castRange = sk.getCastRange();
          boolean hasLongRangeDamageSkill = false;
          switch (L2CharacterAI.1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[sk.getSkillType().ordinal()])
          {
          case 1:
          case 2:
          case 3:
          case 4:
          case 5:
            healSkills.add(sk);
            hasHealOrResurrect = true;
            break;
          case 6:
            buffSkills.add(sk);
            break;
          case 7:
          case 8:
            switch (sk.getId()) { case 367:
            case 4111:
            case 4383:
            case 4578:
            case 4616:
              sleepSkills.add(sk);
              break;
            default:
              generalDisablers.add(sk); }
            break;
          case 9:
            muteSkills.add(sk);
            break;
          case 10:
            sleepSkills.add(sk);
            break;
          case 11:
            rootSkills.add(sk);
            break;
          case 12:
          case 13:
          case 14:
            debuffSkills.add(sk);
            break;
          case 15:
          case 16:
          case 17:
          case 18:
            cancelSkills.add(sk);
            break;
          case 19:
            resurrectSkills.add(sk);
            hasHealOrResurrect = true;
            break;
          case 20:
            break;
          default:
            generalSkills.add(sk);
            hasLongRangeDamageSkill = true;
          }

          if (castRange > 70) {
            hasLongRangeSkills = true;
            if (hasLongRangeDamageSkill)
              hasLongRangeDamageSkills = true;
          }
          if (castRange <= maxCastRange) continue; maxCastRange = castRange;
        }
      }

      if ((!hasLongRangeDamageSkills) && (isMage))
      {
        isBalanced = true;
        isMage = false;
        isFighter = false;
      }
      if ((!hasLongRangeSkills) && ((isMage) || (isBalanced)))
      {
        isBalanced = false;
        isMage = false;
        isFighter = true;
      }
      if ((generalSkills.isEmpty()) && (isMage))
      {
        isBalanced = true;
        isMage = false;
      }
    }
  }

  protected class TargetAnalysis
  {
    public L2Character character;
    public boolean isMage;
    public boolean isBalanced;
    public boolean isArcher;
    public boolean isFighter;
    public boolean isCanceled;
    public boolean isSlower;
    public boolean isMagicResistant;

    public TargetAnalysis()
    {
    }

    public void update(L2Character target)
    {
      if ((target == character) && (Rnd.nextInt(100) > 25))
        return;
      character = target;
      if (target == null)
        return;
      isMage = false;
      isBalanced = false;
      isArcher = false;
      isFighter = false;
      isCanceled = false;

      if (target.getMAtk(null, null) > 1.5D * target.getPAtk(null)) {
        isMage = true;
      } else if ((target.getPAtk(null) * 0.8D < target.getMAtk(null, null)) || (target.getMAtk(null, null) * 0.8D > target.getPAtk(null)))
      {
        isBalanced = true;
      }
      else
      {
        L2Weapon weapon = target.getActiveWeaponItem();
        if ((weapon != null) && (weapon.getItemType() == L2WeaponType.BOW))
          isArcher = true;
        else
          isFighter = true;
      }
      if (target.getRunSpeed() < _actor.getRunSpeed() - 3)
        isSlower = true;
      else
        isSlower = false;
      if (target.getMDef(null, null) * 1.2D > _actor.getMAtk(null, null))
        isMagicResistant = true;
      else
        isMagicResistant = false;
      if (target.getBuffCount() < 4)
        isCanceled = true;
    }
  }

  class IntentionCommand
  {
    protected CtrlIntention _crtlIntention;
    protected Object _arg0;
    protected Object _arg1;

    protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1)
    {
      _crtlIntention = pIntention;
      _arg0 = pArg0;
      _arg1 = pArg1;
    }
  }
}