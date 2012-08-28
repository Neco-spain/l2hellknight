package l2p.gameserver.handler.bypass;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;

public abstract interface IBypassHandler
{
  public abstract String[] getBypasses();

  public abstract void onBypassFeedback(NpcInstance paramNpcInstance, Player paramPlayer, String paramString);
}