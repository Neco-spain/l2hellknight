package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import scripts.items.IItemHandler;

public class Recipes
  implements IItemHandler
{
  private final int[] ITEM_IDS;

  public Recipes()
  {
    RecipeController rc = RecipeController.getInstance();
    ITEM_IDS = new int[rc.getRecipesCount()];
    for (int i = 0; i < rc.getRecipesCount(); i++)
    {
      ITEM_IDS[i] = rc.getRecipeList(i).getRecipeId();
    }
  }

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer())
      return;
    L2PcInstance activeChar = (L2PcInstance)playable;
    L2RecipeList rp = RecipeController.getInstance().getRecipeByItemId(item.getItemId());
    if (activeChar.hasRecipeList(rp.getId())) {
      activeChar.sendPacket(Static.RECIPE_ALREADY_REGISTERED);
    }
    else {
      if (rp.isDwarvenRecipe())
      {
        if (activeChar.hasDwarvenCraft())
        {
          if (rp.getLevel() > activeChar.getDwarvenCraft())
          {
            activeChar.sendPacket(Static.CREATE_LVL_TOO_LOW_TO_REGISTER);
          }
          else if (activeChar.getDwarvenRecipeBook().length >= activeChar.getDwarfRecipeLimit())
          {
            activeChar.sendPacket(SystemMessage.id(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(activeChar.getDwarfRecipeLimit()));
          }
          else
          {
            activeChar.registerDwarvenRecipeList(rp);
            activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_HAS_BEEN_ADDED).addString(item.getItem().getName()));
          }
        }
        else {
          activeChar.sendPacket(Static.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
        }

      }
      else if (activeChar.hasCommonCraft())
      {
        if (rp.getLevel() > activeChar.getCommonCraft())
        {
          activeChar.sendPacket(Static.CREATE_LVL_TOO_LOW_TO_REGISTER);
        }
        else if (activeChar.getCommonRecipeBook().length >= activeChar.getCommonRecipeLimit())
        {
          activeChar.sendPacket(SystemMessage.id(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(activeChar.getCommonRecipeLimit()));
        }
        else
        {
          activeChar.registerCommonRecipeList(rp);
          activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_HAS_BEEN_ADDED).addString(item.getItem().getName()));
        }
      }
      else {
        activeChar.sendPacket(Static.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
      }
      activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
    }
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}