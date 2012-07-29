package l2p.gameserver.clientpackets;

import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.data.xml.holder.RecipeHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Recipe;
import l2p.gameserver.model.RecipeComponent;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.RecipeItemMakeInfo;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2p.gameserver.utils.ItemFunctions;

public class RequestRecipeItemMakeSelf extends L2GameClientPacket {
    private int _recipeId;

    /**
     * packet type id 0xB8
     * format:		cd
     */
    @Override
    protected void readImpl() {
        _recipeId = readD();
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;

        if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
            return;
        }

        if (activeChar.isInStoreMode()) {
            activeChar.sendActionFailed();
            return;
        }

        if (activeChar.isProcessingRequest()) {
            activeChar.sendActionFailed();
            return;
        }

        if (activeChar.isFishing()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
            return;
        }

        Recipe recipeList = RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId);

        if (recipeList == null || recipeList.getRecipes().length == 0) {
            activeChar.sendPacket(Msg.THE_RECIPE_IS_INCORRECT);
            return;
        }

        if (activeChar.getCurrentMp() < recipeList.getMpCost()) {
            activeChar.sendPacket(Msg.NOT_ENOUGH_MP, new RecipeItemMakeInfo(activeChar, recipeList, 0));
            return;
        }

        if (!activeChar.findRecipe(_recipeId)) {
            activeChar.sendPacket(Msg.PLEASE_REGISTER_A_RECIPE, ActionFail.STATIC);
            return;
        }

        activeChar.getInventory().writeLock();
        try {
            RecipeComponent[] recipes = recipeList.getRecipes();

            for (RecipeComponent recipe : recipes) {
                if (recipe.getQuantity() == 0)
                    continue;

                if (Config.ALT_GAME_UNREGISTER_RECIPE && ItemHolder.getInstance().getTemplate(recipe.getItemId()).getItemType() == EtcItemType.RECIPE) {
                    Recipe rp = RecipeHolder.getInstance().getRecipeByRecipeItem(recipe.getItemId());
                    if (activeChar.hasRecipe(rp))
                        continue;
                    activeChar.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeItemMakeInfo(activeChar, recipeList, 0));
                    return;
                }

                ItemInstance item = activeChar.getInventory().getItemByItemId(recipe.getItemId());
                if (item == null || item.getCount() < recipe.getQuantity()) {
                    activeChar.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeItemMakeInfo(activeChar, recipeList, 0));
                    return;
                }
            }

            for (RecipeComponent recipe : recipes)
                if (recipe.getQuantity() != 0)
                    if (Config.ALT_GAME_UNREGISTER_RECIPE && ItemHolder.getInstance().getTemplate(recipe.getItemId()).getItemType() == EtcItemType.RECIPE)
                        activeChar.unregisterRecipe(RecipeHolder.getInstance().getRecipeByRecipeItem(recipe.getItemId()).getId());
                    else {
                        if (!activeChar.getInventory().destroyItemByItemId(recipe.getItemId(), recipe.getQuantity()))
                            continue;//TODO audit
                        activeChar.sendPacket(SystemMessage2.removeItems(recipe.getItemId(), recipe.getQuantity()));
                    }
        } finally {
            activeChar.getInventory().writeUnlock();
        }

        activeChar.resetWaitSitTime();
        activeChar.reduceCurrentMp(recipeList.getMpCost(), null);

        int tryCount = 1, success = 0;
        if (Rnd.chance(Config.CRAFT_DOUBLECRAFT_CHANCE))
            tryCount++;

        for (int i = 0; i < tryCount; i++)
            if (Rnd.chance(recipeList.getSuccessRate())) {
                int itemId = recipeList.getFoundation() != 0 ? Rnd.chance(Config.CRAFT_MASTERWORK_CHANCE) ? recipeList.getFoundation() : recipeList.getItemId() : recipeList.getItemId();
                long count = recipeList.getCount();
                //TODO [G1ta0] добавить проверку на перевес
                ItemFunctions.addItem(activeChar, itemId, count, true);
                success = 1;
            }

        if (success == 0)
            activeChar.sendPacket(new SystemMessage(SystemMessage.S1_MANUFACTURING_FAILURE).addItemName(recipeList.getItemId()));
        activeChar.sendPacket(new RecipeItemMakeInfo(activeChar, recipeList, success));
    }
}