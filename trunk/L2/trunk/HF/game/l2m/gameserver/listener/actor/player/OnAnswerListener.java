package l2m.gameserver.listener.actor.player;

import l2m.gameserver.listener.PlayerListener;

public abstract interface OnAnswerListener extends PlayerListener
{
  public abstract void sayYes();

  public abstract void sayNo();
}