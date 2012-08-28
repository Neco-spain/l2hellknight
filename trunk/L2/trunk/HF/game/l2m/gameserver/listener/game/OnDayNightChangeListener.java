package l2m.gameserver.listener.game;

import l2m.gameserver.listener.GameListener;

public abstract interface OnDayNightChangeListener extends GameListener
{
  public abstract void onDay();

  public abstract void onNight();
}