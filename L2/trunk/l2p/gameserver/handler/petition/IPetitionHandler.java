package l2p.gameserver.handler.petition;

import l2p.gameserver.model.Player;

public abstract interface IPetitionHandler
{
  public abstract void handle(Player paramPlayer, int paramInt, String paramString);
}