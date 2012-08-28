package l2m.gameserver.listener.actor.ai;

import l2m.gameserver.ai.CtrlIntention;
import l2m.gameserver.listener.AiListener;
import l2m.gameserver.model.Creature;

public abstract interface OnAiIntentionListener extends AiListener
{
  public abstract void onAiIntention(Creature paramCreature, CtrlIntention paramCtrlIntention, Object paramObject1, Object paramObject2);
}