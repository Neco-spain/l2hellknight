package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestRecipeBookOpen extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestRecipeBookOpen.class.getName());
  private boolean _isDwarvenCraft;

  protected void readImpl()
  {
    _isDwarvenCraft = (readD() == 0);
  }

  protected void runImpl()
  {
    if (((L2GameClient)getClient()).getActiveChar() == null) {
      return;
    }
    if (((L2GameClient)getClient()).getActiveChar().getPrivateStoreType() != 0)
    {
      ((L2GameClient)getClient()).getActiveChar().sendMessage("Cannot use recipe book while trading");
      return;
    }

    RecipeController.getInstance().requestBookOpen(((L2GameClient)getClient()).getActiveChar(), _isDwarvenCraft);
  }

  public String getType()
  {
    return "C.RecipeBookOpen";
  }
}