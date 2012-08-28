package l2p.gameserver.listener.actor.ai;

import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.listener.AiListener;
import l2p.gameserver.model.Creature;

public abstract interface OnAiEventListener extends AiListener
{
  public abstract void onAiEvent(Creature paramCreature, CtrlEvent paramCtrlEvent, Object[] paramArrayOfObject);
}