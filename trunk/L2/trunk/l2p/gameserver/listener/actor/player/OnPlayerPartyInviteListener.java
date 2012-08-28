package l2p.gameserver.listener.actor.player;

import l2p.gameserver.listener.PlayerListener;
import l2p.gameserver.model.Player;

public abstract interface OnPlayerPartyInviteListener extends PlayerListener
{
  public abstract void onPartyInvite(Player paramPlayer);
}