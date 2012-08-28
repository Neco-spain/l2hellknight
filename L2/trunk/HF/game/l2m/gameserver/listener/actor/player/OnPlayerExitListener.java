package l2m.gameserver.listener.actor.player;

import l2m.gameserver.listener.PlayerListener;
import l2m.gameserver.model.Player;

public abstract interface OnPlayerExitListener extends PlayerListener
{
  public abstract void onPlayerExit(Player paramPlayer);
}