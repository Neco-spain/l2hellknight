package net.sf.l2j.gameserver.ai;

import java.util.EmptyStackException;
import java.util.Stack;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.AIAccessor;
import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList.KnownListAsynchronousUpdateTask;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;

public class L2PlayerFakeAI extends L2CharacterAI
{
  private boolean _thinking;
  private Stack<IntentionCommand> _interuptedIntentions = new Stack();

  private boolean _cpTask = false;

  public L2PlayerFakeAI(L2Character.AIAccessor accessor)
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
    _actor.clearRndWalk();
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
      if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
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

    if (!_actor.isDead()) {
      if (_actor.getCurrentHp() < _actor.getMaxHp() * 0.75D) {
        _actor.setCurrentHp(_actor.getMaxHp() * 0.9D);
        _actor.broadcastPacket(new MagicSkillUser(_actor, _actor, 2032, 1, 1, 0));
      }

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
      return;
    }
    if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange())) {
      return;
    }

    _accessor.doAttack(target);
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

  private int getRespawnDelay(int delay)
  {
    delay = Rnd.get(3500, 6500);
    if ((delay > 4000) && (Rnd.get(100) < 25))
    {
      _actor.sayString(getLastWord(Rnd.get(10)), 1);
    }

    return delay;
  }

  private String getLastWord(int word)
  {
    switch (word)
    {
    case 0:
      return "\u0425\u0443\u0438\u043B\u043E";
    case 1:
      return "\u0441\u0443\u043A\u0430 \u043F\u0435\u0434";
    case 2:
      return "\u0433\u0430\u043D\u0434\u043E\u043D";
    case 3:
      return "\u0436\u0434\u0438 \u0437\u0434\u0435\u0441\u044C \u0441\u0443\u043A\u0430";
    case 4:
      return "\u0445\u0443\u0435\u0432 \u0434\u043E\u043D";
    case 5:
      return "\u0431\u043E\u0442 \u0431\u043B\u044F";
    case 6:
      return "\u0441\u043E\u0441\u0443\u043B\u044F";
    case 7:
      return "\u0435\u0431\u0430\u0442\u044C";
    case 8:
      return ":D";
    case 9:
      return "\u043A\u0443\u0440\u0432\u0430!!";
    case 10:
      return "\u0431\u0431 \u043E\u043A";
    }

    return "\u043D\u043E\u0440\u043C";
  }

  public int getPAtk()
  {
    return 1000;
  }

  public int getMDef()
  {
    return 1000;
  }

  public int getPAtkSpd()
  {
    return 600;
  }

  public int getPDef()
  {
    return 1000;
  }

  public void rndWalk() {
    int posX = _actor.getFakeLoc().x;
    int posY = _actor.getFakeLoc().y;
    int posZ = _actor.getFakeLoc().z;
    switch (Rnd.get(1, 6)) {
    case 1:
      posX += 40;
      posY += 180;
      break;
    case 2:
      posX += 150;
      posY += 50;
      break;
    case 3:
      posX += 69;
      posY -= 100;
      break;
    case 4:
      posX += 10;
      posY -= 100;
      break;
    case 5:
      posX -= 150;
      posY -= 20;
      break;
    case 6:
      posX -= 100;
      posY += 60;
    }

    _actor.setRunning();
    _actor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, _actor.calcHeading(posX, posY)));
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

      _actor.broadcastPacket(new MagicSkillUser(_actor, _actor, 2166, 1, 1, 0));
      if (_actor.getCurrentCp() != _actor.getMaxCp()) {
        _actor.setCurrentCp(_actor.getCurrentCp() + 500.0D);
      }

      if (_actor.getCurrentCp() < _actor.getMaxCp() * 0.9D) {
        L2PlayerFakeAI.access$002(L2PlayerFakeAI.this, true);
        ThreadPoolManager.getInstance().scheduleAi(new CpTask(L2PlayerFakeAI.this), Config.CP_REUSE_TIME, true);
      } else {
        L2PlayerFakeAI.access$002(L2PlayerFakeAI.this, false);
      }
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