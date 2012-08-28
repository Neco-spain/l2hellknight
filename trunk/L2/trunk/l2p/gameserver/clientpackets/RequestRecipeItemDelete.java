package l2p.gameserver.clientpackets;

import l2p.gameserver.data.xml.holder.RecipeHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Recipe;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.RecipeBookItemList;

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