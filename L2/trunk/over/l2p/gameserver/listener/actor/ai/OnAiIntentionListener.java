package l2p.gameserver.listener.actor.ai;

import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.listener.AiListener;
import l2p.gameserver.model.Creature;

public abstract interface OnAiIntentionListener extends AiListener
{
  public abstract void onAiIntention(Creature paramCreature, CtrlIntention paramCtrlIntention, Object paramObject1, Object paramObject2);
}