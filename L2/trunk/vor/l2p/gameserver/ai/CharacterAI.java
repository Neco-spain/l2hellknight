package l2p.gameserver.ai;

import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Summon;
import l2p.gameserver.serverpackets.Die;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.utils.Location;

public class CharacterAI extends AbstractAI
{
  public CharacterAI(Creature actor)
  {
    super(actor);
  }

  protected void onIntentionIdle()
  {
    clientStopMoving();
    changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
  }

  protected void onIntentionActive()
  {
    clientStopMoving();
    changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
    onEvtThink();
  }

  protected void onIntentionAttack(Creature target)
  {
    setAttackTarget(target);
    clientStopMoving();
    changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
    onEvtThink();
  }

  protected void onIntentionCast(Skill skill, Creature target)
  {
    setAttackTarget(target);
    changeIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
    onEvtThink();
  }

  protected void onIntentionFollow(Creature target, Integer offset)
  {
    changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, offset);
    onEvtThink();
  }

  protected void onIntentionInteract(GameObject object)
  {
  }

  protected void onIntentionPickUp(GameObject item)
  {
  }

  protected void onIntentionRest()
  {
  }

  protected void onIntentionCoupleAction(Player player, Integer socialId)
  {
  }

  protected void onEvtArrivedBlocked(Location blocked_at_pos)
  {
    Creature actor = getActor();
    if (actor.isPlayer())
    {
      Location loc = ((Player)actor).getLastServerPosition();
      if (loc != null)
        actor.setLoc(loc, true);
      actor.stopMove();
    }
    onEvtThink();
  }

  protected void onEvtForgetObject(GameObject object)
  {
    if (object == null) {
      return;
    }
    Creature actor = getActor();

    if ((actor.isAttackingNow()) && (getAttackTarget() == object)) {
      actor.abortAttack(true, true);
    }
    if ((actor.isCastingNow()) && (getAttackTarget() == object)) {
      actor.abortCast(true, true);
    }
    if (getAttackTarget() == object) {
      setAttackTarget(null);
    }
    if (actor.getTargetId() == object.getObjectId()) {
      actor.setTarget(null);
    }
    if (actor.getFollowTarget() == object) {
      actor.setFollowTarget(null);
    }
    if (actor.getPet() != null)
      actor.getPet().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
  }

  protected void onEvtDead(Creature killer)
  {
    Creature actor = getActor();

    actor.abortAttack(true, true);
    actor.abortCast(true, true);
    actor.stopMove();
    actor.broadcastPacket(new L2GameServerPacket[] { new Die(actor) });

    setIntention(CtrlIntention.AI_INTENTION_IDLE);
  }

  protected void onEvtFakeDeath()
  {
    clientStopMoving();
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
  }

  protected void onEvtAttacked(Creature attacker, int damage)
  {
  }

  protected void onEvtClanAttacked(Creature attacked_member, Creature attacker, int damage)
  {
  }

  public void Attack(GameObject target, boolean forceUse, boolean dontMove)
  {
    setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
  }

  public void Cast(Skill skill, Creature target)
  {
    Cast(skill, target, false, false);
  }

  public void Cast(Skill skill, Creature target, boolean forceUse, boolean dontMove)
  {
    setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
  }

  protected void onEvtThink()
  {
  }

  protected void onEvtAggression(Creature target, int aggro)
  {
  }

  protected void onEvtFinishCasting()
  {
  }

  protected void onEvtReadyToAct()
  {
  }

  protected void onEvtArrived()
  {
  }

  protected void onEvtArrivedTarget()
  {
  }

  protected void onEvtSeeSpell(Skill skill, Creature caster)
  {
  }

  protected void onEvtSpawn()
  {
  }

  public void onEvtDeSpawn()
  {
  }

  public void stopAITask()
  {
  }

  public void startAITask() {
  }

  public void setNextAction(PlayableAI.nextAction action, Object arg0, Object arg1, boolean arg2, boolean arg3) {
  }

  public void clearNextAction() {
  }

  public boolean isActive() {
    return true;
  }

  protected void onEvtTimer(int timerId, Object arg1, Object arg2)
  {
  }

  public void addTimer(int timerId, long delay)
  {
    addTimer(timerId, null, null, delay);
  }

  public void addTimer(int timerId, Object arg1, long delay)
  {
    addTimer(timerId, arg1, null, delay);
  }

  public void addTimer(int timerId, Object arg1, Object arg2, long delay)
  {
    ThreadPoolManager.getInstance().schedule(new Timer(timerId, arg1, arg2), delay);
  }
  protected class Timer extends RunnableImpl {
    private int _timerId;
    private Object _arg1;
    private Object _arg2;

    public Timer(int timerId, Object arg1, Object arg2) {
      _timerId = timerId;
      _arg1 = arg1;
      _arg2 = arg2;
    }

    public void runImpl()
    {
      notifyEvent(CtrlEvent.EVT_TIMER, Integer.valueOf(_timerId), _arg1, _arg2);
    }
  }
}