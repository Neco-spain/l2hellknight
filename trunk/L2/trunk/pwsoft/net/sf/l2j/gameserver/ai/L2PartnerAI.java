package net.sf.l2j.gameserver.ai;

import java.util.concurrent.ScheduledFuture;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Summon.AIAccessor;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.Rnd;

public class L2PartnerAI extends L2CharacterAI
{
  private boolean _thinking;
  private boolean _startFollow = _actor.getFollowStatus();
  private RunOnAttacked _runOnAttacked;
  private ScheduledFuture<?> _runOnAttackedTask;

  public L2PartnerAI(L2Character.AIAccessor accessor)
  {
    super(accessor);
  }

  protected void onIntentionIdle()
  {
    stopFollow();
    _startFollow = false;
    onIntentionActive();
  }

  protected void onIntentionActive()
  {
    if (_startFollow)
      setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _actor.getOwner());
    else
      super.onIntentionActive();
  }

  private void thinkAttack()
  {
    L2Character target = getAttackTarget();
    if (target == null) {
      return;
    }

    if (checkTargetLostOrDead(target)) {
      setAttackTarget(null);
      return;
    }

    if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange())) {
      return;
    }
    clientStopMoving(null);

    if (_actor.getOwner() != null) {
      _actor.getOwner().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, target);
    }

    switch (_actor.getPartnerClass()) {
    case 1:
      archerAtatck(target);
      _accessor.doAttack(target);
      break;
    default:
      _accessor.doAttack(target);
    }
  }

  private void archerAtatck(L2Character target)
  {
    if (Rnd.get(100) < 45) {
      switch (Rnd.get(10)) {
      case 1:
        _accessor.doCast(SkillTable.getInstance().getInfo(101, 40));
        break;
      case 2:
        _accessor.doCast(SkillTable.getInstance().getInfo(19, 37));
        break;
      case 3:
        _accessor.doCast(SkillTable.getInstance().getInfo(354, 1));
      }

      _actor.setTarget(target);
      ThreadPoolManager.getInstance().scheduleAi(new AttackTask(), 900L, true);
      return;
    }
  }

  private void thinkCast()
  {
    if (checkTargetLost(getCastTarget())) {
      setCastTarget(null);
      return;
    }
    boolean val = _startFollow;
    if (maybeMoveToPawn(getCastTarget(), _actor.getMagicalAttackRange(_skill))) {
      return;
    }
    clientStopMoving(null);
    _actor.setFollowStatus(false);
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
    _startFollow = val;
    _accessor.doCast(_skill);
  }

  private void thinkPickUp() {
    if (checkTargetLost(getTarget())) {
      return;
    }
    if (maybeMoveToPawn(getTarget(), 36)) {
      return;
    }
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
    ((L2Summon.AIAccessor)_accessor).doPickupItem(getTarget());
  }

  private void thinkInteract()
  {
    if (_actor.isAllSkillsDisabled()) {
      return;
    }
    if (checkTargetLost(getTarget())) {
      return;
    }
    if (maybeMoveToPawn(getTarget(), 36)) {
      return;
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
      switch (1.$SwitchMap$net$sf$l2j$gameserver$ai$CtrlIntention[getIntention().ordinal()]) {
      case 1:
        thinkAttack();
        break;
      case 2:
        thinkCast();
        break;
      case 3:
        thinkPickUp();
        break;
      case 4:
        thinkInteract();
      }
    }
    finally {
      _thinking = false;
    }
  }

  protected void onEvtFinishCasting()
  {
    if (_actor.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
      _actor.setFollowStatus(_startFollow);
  }

  public void notifyFollowStatusChange()
  {
    _startFollow = (!_startFollow);
    switch (1.$SwitchMap$net$sf$l2j$gameserver$ai$CtrlIntention[getIntention().ordinal()]) {
    case 5:
    case 6:
    case 7:
      _actor.setFollowStatus(_startFollow);
    }
  }

  public void setStartFollowController(boolean val) {
    _startFollow = val;
  }

  public void onOwnerGotAttacked(L2Character attacker)
  {
    L2PcInstance owner = _actor.getOwner();

    if ((owner == null) || (owner.isOnline() == 0)) {
      _actor.logout();
      return;
    }

    if (!owner.isInsideRadius(_actor, 1200, true, true)) {
      startFollow(owner);
      return;
    }

    if (owner.isDead()) {
      return;
    }

    if (_actor.isCastingNow()) {
      return;
    }

    if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK) {
      _actor.setTarget(attacker);
      setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
    }
  }

  protected void onEvtAttacked(L2Character attacker)
  {
    if (_actor == null) {
      return;
    }

    if (_runOnAttacked != null) {
      _runOnAttacked.setAttacker(attacker);
    }

    if (_actor.getOwner() != null) {
      _actor.getOwner().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getAttackTarget());
    }

    if ((_runOnAttacked == null) && ((_intention == CtrlIntention.AI_INTENTION_FOLLOW) || (_intention == CtrlIntention.AI_INTENTION_IDLE) || (_intention == CtrlIntention.AI_INTENTION_ACTIVE)) && (!_clientMoving)) {
      if (_runOnAttacked == null) {
        _runOnAttacked = new RunOnAttacked(null);
        _runOnAttacked.setAttacker(attacker);
      }

      if (_runOnAttackedTask == null) {
        _runOnAttackedTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(_runOnAttacked, 0L, 500L);
      }
    }
    super.onEvtAttacked(attacker);
  }

  public int getPAtk()
  {
    switch (_actor.getPartnerClass()) {
    case 1:
      return Config.FAKE_MAX_PATK_BOW;
    case 3:
      return Config.FAKE_MAX_PATK_MAG;
    case 4:
      return Config.FAKE_MAX_PATK_HEAL;
    case 2:
    }return 2000;
  }

  public int getMDef()
  {
    switch (_actor.getPartnerClass()) {
    case 1:
      return Config.FAKE_MAX_MDEF_BOW;
    case 3:
      return Config.FAKE_MAX_MDEF_MAG;
    case 4:
      return Config.FAKE_MAX_MDEF_HEAL;
    case 2:
    }return 2300;
  }

  public int getPAtkSpd()
  {
    switch (_actor.getPartnerClass()) {
    case 1:
      return Config.FAKE_MAX_PSPD_BOW;
    case 3:
      return Config.FAKE_MAX_PSPD_MAG;
    case 4:
      return Config.FAKE_MAX_PSPD_HEAL;
    case 2:
    }return 400;
  }

  public int getPDef()
  {
    switch (_actor.getPartnerClass()) {
    case 1:
      return Config.FAKE_MAX_PDEF_BOW;
    case 3:
      return Config.FAKE_MAX_PDEF_MAG;
    case 4:
      return Config.FAKE_MAX_PDEF_HEAL;
    case 2:
    }return 2600;
  }

  public int getMAtk()
  {
    switch (_actor.getPartnerClass()) {
    case 1:
      return Config.FAKE_MAX_MATK_BOW;
    case 3:
      return Config.FAKE_MAX_MATK_MAG;
    case 4:
      return Config.FAKE_MAX_MATK_HEAL;
    case 2:
    }return 5600;
  }

  public int getMAtkSpd()
  {
    switch (_actor.getPartnerClass()) {
    case 1:
      return Config.FAKE_MAX_MSPD_BOW;
    case 3:
      return Config.FAKE_MAX_MSPD_MAG;
    case 4:
      return Config.FAKE_MAX_MSPD_HEAL;
    case 2:
    }return 400;
  }

  public int getMaxHp()
  {
    switch (_actor.getPartnerClass()) {
    case 1:
      return Config.FAKE_MAX_HP_BOW;
    case 3:
      return Config.FAKE_MAX_HP_MAG;
    case 4:
      return Config.FAKE_MAX_HP_HEAL;
    case 2:
    }return 4000;
  }

  private class RunOnAttacked
    implements Runnable
  {
    private L2Character _attacker;
    private long _lastAttack;

    private RunOnAttacked()
    {
    }

    public void run()
    {
      if (_actor == null) {
        return;
      }

      if ((_attacker != null) && (_actor.getOwner() != null) && (_lastAttack + 20000L > System.currentTimeMillis()) && ((_intention == CtrlIntention.AI_INTENTION_FOLLOW) || (_intention == CtrlIntention.AI_INTENTION_IDLE) || (_intention == CtrlIntention.AI_INTENTION_ACTIVE))) {
        if (!_clientMoving) {
          int posX = _actor.getOwner().getX();
          int posY = _actor.getOwner().getY();
          int posZ = _actor.getOwner().getZ();

          int side = Rnd.get(1, 6);
          switch (side) {
          case 1:
            posX += 30;
            posY += 140;
            break;
          case 2:
            posX += 150;
            posY += 50;
            break;
          case 3:
            posX += 70;
            posY -= 100;
            break;
          case 4:
            posX += 5;
            posY -= 100;
            break;
          case 5:
            posX -= 150;
            posY -= 20;
            break;
          case 6:
            posX -= 100;
            posY += 50;
          }

          _actor.setRunning();
          _actor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, _actor.calcHeading(posX, posY)));
        }
      } else {
        _attacker = null;
        if (_runOnAttackedTask != null) {
          _runOnAttackedTask.cancel(true);
        }

        L2PartnerAI.access$202(L2PartnerAI.this, null);
        L2PartnerAI.access$302(L2PartnerAI.this, null);
      }
    }

    public void setAttacker(L2Character attacker) {
      _attacker = attacker;
      _lastAttack = System.currentTimeMillis();
    }
  }

  private class AttackTask
    implements Runnable
  {
    public AttackTask()
    {
    }

    public void run()
    {
      L2PartnerAI.this.thinkAttack();
    }
  }
}