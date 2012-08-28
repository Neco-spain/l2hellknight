package net.sf.l2j.gameserver.ai;

import net.sf.l2j.gameserver.model.L2Character;

public abstract interface Ctrl
{
  public abstract L2Character getActor();

  public abstract CtrlIntention getIntention();

  public abstract L2Character getAttackTarget();

  public abstract void setIntention(CtrlIntention paramCtrlIntention);

  public abstract void setIntention(CtrlIntention paramCtrlIntention, Object paramObject);

  public abstract void setIntention(CtrlIntention paramCtrlIntention, Object paramObject1, Object paramObject2);

  public abstract void notifyEvent(CtrlEvent paramCtrlEvent);

  public abstract void notifyEvent(CtrlEvent paramCtrlEvent, Object paramObject);

  public abstract void notifyEvent(CtrlEvent paramCtrlEvent, Object paramObject1, Object paramObject2);
}