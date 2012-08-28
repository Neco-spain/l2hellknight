package l2m.gameserver.listener.game;

import l2m.gameserver.listener.GameListener;

public abstract interface OnShutdownListener extends GameListener
{
  public abstract void onShutdown();
}