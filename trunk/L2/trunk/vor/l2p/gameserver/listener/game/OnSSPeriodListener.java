package l2p.gameserver.listener.game;

import l2p.gameserver.listener.GameListener;

public abstract interface OnSSPeriodListener extends GameListener
{
  public abstract void onPeriodChange(int paramInt);
}