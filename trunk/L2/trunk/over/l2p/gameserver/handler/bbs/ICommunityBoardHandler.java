package l2p.gameserver.handler.bbs;

import l2p.gameserver.model.Player;

public abstract interface ICommunityBoardHandler
{
  public abstract String[] getBypassCommands();

  public abstract void onBypassCommand(Player paramPlayer, String paramString);

  public abstract void onWriteCommand(Player paramPlayer, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6);
}