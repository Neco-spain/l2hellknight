package l2m.gameserver.handler.bypass;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.instances.NpcInstance;

public abstract interface IBypassHandler
{
  public abstract String[] getBypasses();

  public abstract void onBypassFeedback(NpcInstance paramNpcInstance, Player paramPlayer, String paramString);
}