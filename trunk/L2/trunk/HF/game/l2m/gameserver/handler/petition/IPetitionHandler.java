package l2m.gameserver.handler.petition;

import l2m.gameserver.model.Player;

public abstract interface IPetitionHandler
{
  public abstract void handle(Player paramPlayer, int paramInt, String paramString);
}