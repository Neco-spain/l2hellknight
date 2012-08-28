package l2m.gameserver.listener.game;

import l2m.gameserver.listener.GameListener;

public abstract interface OnSSPeriodListener extends GameListener
{
  public abstract void onPeriodChange(int paramInt);
}