package l2p.gameserver.listener.actor.player;

import l2p.gameserver.listener.PlayerListener;
import l2p.gameserver.model.Player;

public abstract interface OnPlayerExitListener extends PlayerListener
{
  public abstract void onPlayerExit(Player paramPlayer);
}