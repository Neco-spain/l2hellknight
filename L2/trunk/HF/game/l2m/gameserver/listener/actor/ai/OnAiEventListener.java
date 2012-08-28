package l2m.gameserver.listener.actor.ai;

import l2m.gameserver.ai.CtrlEvent;
import l2m.gameserver.listener.AiListener;
import l2m.gameserver.model.Creature;

public abstract interface OnAiEventListener extends AiListener
{
  public abstract void onAiEvent(Creature paramCreature, CtrlEvent paramCtrlEvent, Object[] paramArrayOfObject);
}