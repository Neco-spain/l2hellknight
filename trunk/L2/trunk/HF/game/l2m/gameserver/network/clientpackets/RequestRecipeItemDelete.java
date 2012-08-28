package l2m.gameserver.network.clientpackets;

import l2m.gameserver.data.xml.holder.RecipeHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Recipe;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.RecipeBookItemList;

public class RequestRecipeItemDelete extends L2GameClientPacket
{
  private int _recipeId;

  protected void readImpl()
  {
    _recipeId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.getPrivateStoreType() == 5)
    {
      activeChar.sendActionFailed();
      return;
    }

    Recipe rp = RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId);
    if (rp == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    activeChar.unregisterRecipe(_recipeId);
    activeChar.sendPacket(new RecipeBookItemList(activeChar, rp.isDwarvenRecipe()));
  }
}