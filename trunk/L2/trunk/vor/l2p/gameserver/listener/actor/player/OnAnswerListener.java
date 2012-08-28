package l2p.gameserver.listener.actor.player;

import l2p.gameserver.listener.PlayerListener;

public abstract interface OnAnswerListener extends PlayerListener
{
  public abstract void sayYes();

  public abstract void sayNo();
}