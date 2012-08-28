package l2p.gameserver.listener.actor.player;

import l2p.gameserver.listener.PlayerListener;
import l2p.gameserver.model.Player;

public abstract interface OnPlayerEnterListener extends PlayerListener
{
  public abstract void onPlayerEnter(Player paramPlayer);
}