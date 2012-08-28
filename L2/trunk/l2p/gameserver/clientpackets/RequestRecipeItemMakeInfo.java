package l2p.gameserver.clientpackets;

import l2p.gameserver.data.xml.holder.RecipeHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Recipe;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.RecipeItemMakeInfo;

public class RequestRecipeItemMakeInfo extends L2GameClientPacket
{
  private int _id;

  protected void readImpl()
  {
    _id = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Recipe recipeList = RecipeHolder.getInstance().getRecipeByRecipeId(_id);
    if (recipeList == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    sendPacket(new RecipeItemMakeInfo(activeChar, recipeList, -1));
  }
}