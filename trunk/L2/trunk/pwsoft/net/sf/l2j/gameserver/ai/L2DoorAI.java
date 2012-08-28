package net.sf.l2j.gameserver.ai;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance.AIAccessor;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.DoorKnownList;

public class L2DoorAI extends L2CharacterAI
{
  public L2DoorAI(L2DoorInstance.AIAccessor accessor)
  {
    super(accessor);
  }
  protected void onIntentionIdle() {
  }
  protected void onIntentionActive() {
  }
  protected void onIntentionRest() {
  }
  protected void onIntentionAttack(L2Character target) {
  }
  protected void onIntentionCast(L2Skill skill, L2Object target) {
  }
  protected void onIntentionMoveTo(L2CharPosition destination) {
  }

  protected void onIntentionFollow(L2Character target) {
  }

  protected void onIntentionPickUp(L2Object item) {
  }

  protected void onIntentionInteract(L2Object object) {
  }

  protected void onEvtThink() {
  }

  protected void onEvtAttacked(L2Character attacker) {
    L2DoorInstance me = (L2DoorInstance)_actor;
    ThreadPoolManager.getInstance().executeAi(new onEventAttackedDoorTask(me, attacker), false);
  }
  protected void onEvtAggression(L2Character target, int aggro) {
  }
  protected void onEvtStunned(L2Character attacker) {
  }
  protected void onEvtSleeping(L2Character attacker) {
  }
  protected void onEvtRooted(L2Character attacker) {
  }
  protected void onEvtReadyToAct() {
  }
  protected void onEvtUserCmd(Object arg0, Object arg1) {
  }
  protected void onEvtArrived() {
  }
  protected void onEvtArrivedRevalidate() {
  }
  protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos) {
  }
  protected void onEvtForgetObject(L2Object object) {
  }
  protected void onEvtCancel() {
  }
  protected void onEvtDead() {
  }

  private class onEventAttackedDoorTask implements Runnable {
    private L2DoorInstance _door;
    private L2Character _attacker;

    public onEventAttackedDoorTask(L2DoorInstance door, L2Character attacker) {
      _door = door;
      _attacker = attacker;
    }

    public void run()
    {
      _door.getKnownList().updateKnownObjects();

      for (L2SiegeGuardInstance guard : _door.getKnownSiegeGuards())
        if ((_actor.isInsideRadius(guard, guard.getFactionRange(), false, true)) && (Math.abs(_attacker.getZ() - guard.getZ()) < 200))
        {
          guard.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _attacker, Integer.valueOf(15));
        }
    }
  }
}