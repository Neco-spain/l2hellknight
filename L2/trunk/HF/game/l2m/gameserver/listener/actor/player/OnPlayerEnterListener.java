package l2m.gameserver.listener.actor.player;

import l2m.gameserver.listener.PlayerListener;
import l2m.gameserver.model.Player;

public abstract interface OnPlayerEnterListener extends PlayerListener
{
  public abstract void onPlayerEnter(Player paramPlayer);
}