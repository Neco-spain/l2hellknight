package net.sf.l2j.gameserver.ai;

import java.util.EmptyStackException;
import java.util.Stack;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.FakePlayersTablePlus;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.AIAccessor;
import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList.KnownListAsynchronousUpdateTask;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;

public class L2PlayerFakeArcherAI extends L2PlayerFakeAI
{
  private boolean _thinking;
  private Stack<IntentionCommand> _interuptedIntentions = new Stack();

  private boolean _cpTask = false;
  private long _atkTask = 0L;

  public L2PlayerFakeArcherAI(L2Character.AIAccessor accessor)
  {
    super(accessor);
  }

  synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
  {
    if (intention != CtrlIntention.AI_INTENTION_CAST) {
      super.changeIntention(intention, arg0, arg1);
      return;
    }

    if ((intention == _intention) && (arg0 == _intentionArg0) && (arg1 == _intentionArg1)) {
      super.changeIntention(intention, arg0, arg1);
      return;
    }

    _interuptedIntentions.push(new IntentionCommand(_intention, _intentionArg0, _intentionArg1));
    super.changeIntention(intention, arg0, arg1);
  }

  protected void onEvtFinishCasting()
  {
    if ((_skill != null) && (_skill.isOffensive())) {
      _interuptedIntentions.clear();
    }

    if (getIntention() == CtrlIntention.AI_INTENTION_CAST)
    {
      if (!_interuptedIntentions.isEmpty()) {
        IntentionCommand cmd = null;
        try {
          cmd = (IntentionCommand)_interuptedIntentions.pop();
        }
        catch (EmptyStackException ese)
        {
        }

        if ((cmd != null) && (cmd._crtlIntention != CtrlIntention.AI_INTENTION_CAST))
        {
          setIntention(cmd._crtlIntention, cmd._arg0, cmd._arg1);
        }
        else setIntention(CtrlIntention.AI_INTENTION_IDLE);

      }
      else
      {
        setIntention(CtrlIntention.AI_INTENTION_IDLE);
      }
    }
  }

  protected void onIntentionRest()
  {
    if (getIntention() != CtrlIntention.AI_INTENTION_REST) {
      changeIntention(CtrlIntention.AI_INTENTION_REST, null, null);
      setTarget(null);
      if (getAttackTarget() != null) {
        setAttackTarget(null);
      }
      clientStopMoving(null);
    }
  }

  protected void onIntentionActive()
  {
    setIntention(CtrlIntention.AI_INTENTION_IDLE);

    ThreadPoolManager.getInstance().scheduleAi(new ActiveTask(), 3000L, true);
  }

  private void thinkCast()
  {
    L2Character target = getCastTarget();

    if ((_skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SIGNET_GROUND) && (_actor.isPlayer())) {
      if (maybeMoveToPosition(_actor.getPlayer().getCurrentSkillWorldPosition(), _actor.getMagicalAttackRange(_skill)))
        return;
    }
    else {
      if (checkTargetLost(target)) {
        if ((_skill.isOffensive()) && (getAttackTarget() != null))
        {
          setCastTarget(null);
        }
        ThreadPoolManager.getInstance().scheduleAi(new IdleTask(), 2000L, true);
        return;
      }

      if ((target != null) && (maybeMoveToPawn(target, _actor.getMagicalAttackRange(_skill)))) {
        return;
      }
    }

    if (_skill.getHitTime() > 50) {
      clientStopMoving(null);
    }

    L2Object oldTarget = _actor.getTarget();
    if (oldTarget != null)
    {
      if ((target != null) && (oldTarget != target)) {
        _actor.setTarget(getCastTarget());
      }

      _accessor.doCast(_skill);

      if ((target != null) && (oldTarget != target))
        _actor.setTarget(oldTarget);
    }
    else {
      _accessor.doCast(_skill);
    }

    ThreadPoolManager.getInstance().scheduleAi(new ActiveTask(), 700L, true);
  }

  private void thinkPickUp()
  {
    if ((_actor.isAllSkillsDisabled()) || (_actor.isMovementDisabled())) {
      return;
    }
    L2Object target = getTarget();
    if (checkTargetLost(target)) {
      return;
    }
    if (maybeMoveToPawn(target, 36)) {
      return;
    }
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
    ((L2PcInstance.AIAccessor)_accessor).doPickupItem(target);
  }

  private void thinkInteract()
  {
    if (_actor.isAllSkillsDisabled()) {
      return;
    }
    L2Object target = getTarget();
    if (checkTargetLost(target)) {
      return;
    }
    if (maybeMoveToPawn(target, 36)) {
      return;
    }
    if (!(target instanceof L2StaticObjectInstance)) {
      ((L2PcInstance.AIAccessor)_accessor).doInteract((L2Character)target);
    }
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
  }

  protected void onEvtThink()
  {
    if ((_thinking) || (_actor.isAllSkillsDisabled())) {
      return;
    }

    _thinking = true;
    try {
      if (getIntention() == CtrlIntention.AI_INTENTION_IDLE)
        radarOn();
      else if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
        thinkAttack();
      else if (getIntention() == CtrlIntention.AI_INTENTION_CAST)
        thinkCast();
      else if (getIntention() == CtrlIntention.AI_INTENTION_PICK_UP)
        thinkPickUp();
      else if (getIntention() == CtrlIntention.AI_INTENTION_INTERACT)
        thinkInteract();
    }
    finally {
      _thinking = false;
    }
  }

  protected void onEvtArrivedRevalidate()
  {
    ThreadPoolManager.getInstance().executeAi(new ObjectKnownList.KnownListAsynchronousUpdateTask(_actor), true);
    super.onEvtArrivedRevalidate();
  }

  private void thinkAttack()
  {
    L2Character target = getAttackTarget();
    if (target == null) {
      return;
    }

    if (!_actor.isDead())
    {
      if ((!_cpTask) && (_actor.getCurrentCp() < _actor.getMaxCp() * 0.9D)) {
        _cpTask = true;
        ThreadPoolManager.getInstance().scheduleAi(new CpTask(), Config.CP_REUSE_TIME, true);
      }
    }

    if (checkTargetLostOrDead(target)) {
      if (target != null)
      {
        setAttackTarget(null);
      }

      ThreadPoolManager.getInstance().scheduleAi(new IdleTask(), 2000L, true);
      return;
    }
    if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange()))
    {
      return;
    }

    if ((_atkTask < System.currentTimeMillis()) && (Rnd.get(100) < 45)) {
      _atkTask = (System.currentTimeMillis() + 5000L);
      switch (Rnd.get(10)) {
      case 1:
        _accessor.doCast(SkillTable.getInstance().getInfo(101, 40));
        break;
      case 2:
        _accessor.doCast(SkillTable.getInstance().getInfo(19, 37));
        break;
      case 3:
        _accessor.doCast(SkillTable.getInstance().getInfo(354, 1));
        break;
      case 4:
        rndWalk();
      }

      _actor.setTarget(target);
      ThreadPoolManager.getInstance().scheduleAi(new AttackTask(target), 900L, true);
      return;
    }

    _accessor.doAttack(target);
  }

  private void radarOn()
  {
    if ((getAttackTarget() == null) && (Util.calculateDistance(_actor.getX(), _actor.getY(), _actor.getZ(), _actor.getFakeLoc().x, _actor.getFakeLoc().y, _actor.getFakeLoc().z, true) > 2100.0D)) {
      moveXYZ(_actor.getFakeLoc().x, _actor.getFakeLoc().y, _actor.getFakeLoc().z);
    }
    else if (Rnd.get(100) < 5) {
      _accessor.doCast(SkillTable.getInstance().getInfo(99, 2));
      _actor.setTarget(_actor);
    } else if (Rnd.get(100) < 10) {
      if (Util.calculateDistance(_actor.getX(), _actor.getY(), _actor.getZ(), _actor.getFakeLoc().x, _actor.getFakeLoc().y, _actor.getFakeLoc().z, true) > 310.0D)
        moveXYZ(_actor.getFakeLoc().x, _actor.getFakeLoc().y, _actor.getFakeLoc().z);
      else {
        rndWalk();
      }
    }

    findTarget();
    ThreadPoolManager.getInstance().scheduleAi(new ActiveTask(), 6000L, true);
  }

  private void findTarget() {
    for (L2PcInstance target : _actor.getKnownList().getKnownPlayersInRadius(1450)) {
      if ((target == null) || (target.isDead()))
      {
        continue;
      }
      if ((target.getKarma() > 0) || (target.getPvpFlag() > 0)) {
        if (!_actor.isRunning()) {
          _actor.setRunning();
        }

        if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK) {
          _actor.setTarget(target);
          setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
        }

        super.onEvtAttacked(target);
        break;
      }
    }
  }

  protected void onEvtAttacked(L2Character attacker)
  {
    if (!_actor.isRunning()) {
      _actor.setRunning();
    }

    if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK) {
      _actor.setTarget(attacker);
      setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
    }

    super.onEvtAttacked(attacker);
  }

  protected void clientNotifyDead()
  {
    _clientMovingToPawnOffset = 0;
    _clientMoving = false;

    ThreadPoolManager.getInstance().scheduleAi(new ResurrectTask(), getRespawnDelay(0), true);
    super.clientNotifyDead();
  }

  private int getRespawnDelay(int delay) {
    delay = Rnd.get(3500, 6500);
    if ((delay > 4000) && (Rnd.get(100) < 25)) {
      _actor.sayString(getLastWord(Rnd.get(13)), 0);
    }

    return delay;
  }

  private String getLastWord(int word) {
    return FakePlayersTablePlus.getInstance().getRandomLastPhrase();
  }

  private String getRangeWord(int word)
  {
    switch (word) {
    case 0:
      return "\u0441\u0442\u043E\u044F\u0442\u044C";
    case 1:
      return "\u0441\u0442\u043E\u0439 \u0441\u0443\u043A\u0430";
    case 2:
      return "\u043A\u0430\u043A \u0431\u0430\u0431\u0430";
    }

    return "\u043F\u0438\u0437\u0434\u0430 \u0442\u0435\u0431\u0435";
  }

  public int getPAtk()
  {
    return 6000;
  }

  public int getMDef()
  {
    return 2300;
  }

  public int getPAtkSpd()
  {
    return 923;
  }

  public int getPDef()
  {
    return 2600;
  }

  private class CpTask
    implements Runnable
  {
    public CpTask()
    {
    }

    public void run()
    {
      if (_actor.isDead()) {
        return;
      }

      if (!_actor.isAllSkillsDisabled()) {
        _actor.broadcastPacket(new MagicSkillUser(_actor, _actor, 2166, 1, 1, 0));
        if (_actor.getCurrentCp() != _actor.getMaxCp()) {
          _actor.setCurrentCp(_actor.getCurrentCp() + 500.0D);
        }
      }

      if (_actor.getCurrentCp() < _actor.getMaxCp() * 0.9D) {
        L2PlayerFakeArcherAI.access$202(L2PlayerFakeArcherAI.this, true);
        ThreadPoolManager.getInstance().scheduleAi(new CpTask(L2PlayerFakeArcherAI.this), Config.CP_REUSE_TIME, true);
      } else {
        L2PlayerFakeArcherAI.access$202(L2PlayerFakeArcherAI.this, false);
      }
    }
  }

  private class AttackTask
    implements Runnable
  {
    private L2Character target;

    public AttackTask(L2Character target)
    {
      this.target = target;
    }

    public void run()
    {
      L2PlayerFakeArcherAI.this.thinkAttack();
    }
  }

  private class ActiveTask
    implements Runnable
  {
    public ActiveTask()
    {
    }

    public void run()
    {
      onEvtThink();
    }
  }

  private class IdleTask
    implements Runnable
  {
    public IdleTask()
    {
    }

    public void run()
    {
      L2PlayerFakeArcherAI.this.radarOn();
    }
  }

  private class ResurrectTask
    implements Runnable
  {
    public ResurrectTask()
    {
    }

    public void run()
    {
      _actor.teleToClosestTown();
      _actor.doRevive();
    }
  }

  static class IntentionCommand
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