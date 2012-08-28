package l2p.gameserver.listener.game;

import l2p.gameserver.listener.GameListener;

public abstract interface OnShutdownListener extends GameListener
{
  public abstract void onShutdown();
}