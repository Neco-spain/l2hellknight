package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

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
    if (!(playable instanceof L2PcInstance))
      return;
    L2PcInstance activeChar = (L2PcInstance)playable;
    L2RecipeList rp = RecipeController.getInstance().getRecipeByItemId(item.getItemId());
    if (activeChar.hasRecipeList(rp.getId()))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.RECIPE_ALREADY_REGISTERED);
      activeChar.sendPacket(sm);
    }
    else if (rp.isDwarvenRecipe())
    {
      if (activeChar.hasDwarvenCraft())
      {
        if (rp.getLevel() > activeChar.getDwarvenCraft())
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
          activeChar.sendPacket(sm);
        }
        else if (activeChar.getDwarvenRecipeBook().length >= activeChar.GetDwarfRecipeLimit())
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
          sm.addNumber(activeChar.GetDwarfRecipeLimit());
          activeChar.sendPacket(sm);
        }
        else
        {
          activeChar.registerDwarvenRecipeList(rp);
          activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
          sm.addString("Added recipe \"" + rp.getRecipeName() + "\" to Dwarven RecipeBook");
          activeChar.sendPacket(sm);
        }
      }
      else
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
        activeChar.sendPacket(sm);
      }

    }
    else if (activeChar.hasCommonCraft())
    {
      if (rp.getLevel() > activeChar.getCommonCraft())
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
        activeChar.sendPacket(sm);
      }
      else if (activeChar.getCommonRecipeBook().length >= activeChar.GetCommonRecipeLimit())
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
        sm.addNumber(activeChar.GetCommonRecipeLimit());
        activeChar.sendPacket(sm);
      }
      else
      {
        activeChar.registerCommonRecipeList(rp);
        activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
        sm.addString("Added recipe \"" + rp.getRecipeName() + "\" to Common RecipeBook");
        activeChar.sendPacket(sm);
      }
    }
    else
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
      activeChar.sendPacket(sm);
    }
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}