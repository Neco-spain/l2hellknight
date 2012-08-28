package l2p.gameserver.listener.actor.player;

import l2p.gameserver.listener.PlayerListener;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.Reflection;

public abstract interface OnTeleportListener extends PlayerListener
{
  public abstract void onTeleport(Player paramPlayer, int paramInt1, int paramInt2, int paramInt3, Reflection paramReflection);
}