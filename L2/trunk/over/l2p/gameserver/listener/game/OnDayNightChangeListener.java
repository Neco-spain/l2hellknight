package l2p.gameserver.listener.game;

import l2p.gameserver.listener.GameListener;

public abstract interface OnDayNightChangeListener extends GameListener
{
  public abstract void onDay();

  public abstract void onNight();
}