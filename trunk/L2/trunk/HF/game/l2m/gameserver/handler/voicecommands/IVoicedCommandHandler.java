package l2m.gameserver.handler.voicecommands;

import l2m.gameserver.model.Player;

public abstract interface IVoicedCommandHandler
{
  public abstract boolean useVoicedCommand(String paramString1, Player paramPlayer, String paramString2);

  public abstract String[] getVoicedCommandList();
}