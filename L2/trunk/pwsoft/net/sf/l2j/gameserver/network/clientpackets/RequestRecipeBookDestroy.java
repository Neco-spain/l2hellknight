package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.RecipeBookItemList;

public final class RequestRecipeBookDestroy extends L2GameClientPacket
{
  private int _recipeID;

  protected void readImpl()
  {
    _recipeID = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player != null)
    {
      L2RecipeList rp = RecipeController.getInstance().getRecipeList(_recipeID - 1);
      if (rp == null)
        return;
      player.unregisterRecipeList(_recipeID);

      RecipeBookItemList response = new RecipeBookItemList(rp.isDwarvenRecipe(), player.getMaxMp());
      if (rp.isDwarvenRecipe())
        response.addRecipes(player.getDwarvenRecipeBook());
      else {
        response.addRecipes(player.getCommonRecipeBook());
      }
      player.sendPacket(response);
    }
  }

  public String getType()
  {
    return "C.RecipeBookDestroy";
  }
}