package l2p.gameserver.clientpackets;

import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.RecipeHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Recipe;
import l2p.gameserver.model.RecipeComponent;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.ManufactureItem;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.RecipeShopItemInfo;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.TradeHelper;

public class RequestRecipeShopMakeDo extends L2GameClientPacket
{
  private int _manufacturerId;
  private int _recipeId;
  private long _price;

  protected void readImpl()
  {
    _manufacturerId = readD();
    _recipeId = readD();
    _price = readQ();
  }

  protected void runImpl()
  {
    Player buyer = ((GameClient)getClient()).getActiveChar();
    if (buyer == null) {
      return;
    }
    if (buyer.isActionsDisabled())
    {
      buyer.sendActionFailed();
      return;
    }

    if (buyer.isInStoreMode())
    {
      buyer.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
      return;
    }

    if (buyer.isInTrade())
    {
      buyer.sendActionFailed();
      return;
    }

    if (buyer.isFishing())
    {
      buyer.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
      return;
    }

    if (!buyer.getPlayerAccess().UseTrade)
    {
      buyer.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
      return;
    }

    Player manufacturer = (Player)buyer.getVisibleObject(_manufacturerId);
    if ((manufacturer == null) || (manufacturer.getPrivateStoreType() != 5) || (!manufacturer.isInRangeZ(buyer, 200L)))
    {
      buyer.sendActionFailed();
      return;
    }

    Recipe recipeList = null;
    for (ManufactureItem mi : manufacturer.getCreateList()) {
      if ((mi.getRecipeId() == _recipeId) && 
        (_price == mi.getCost()))
      {
        recipeList = RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId);
        break;
      }
    }
    if (recipeList == null)
    {
      buyer.sendActionFailed();
      return;
    }

    int success = 0;

    if (recipeList.getRecipes().length == 0)
    {
      manufacturer.sendMessage(new CustomMessage("l2p.gameserver.RecipeController.NoRecipe", manufacturer, new Object[0]).addString(recipeList.getRecipeName()));
      buyer.sendMessage(new CustomMessage("l2p.gameserver.RecipeController.NoRecipe", manufacturer, new Object[0]).addString(recipeList.getRecipeName()));
      return;
    }

    if (!manufacturer.findRecipe(_recipeId))
    {
      buyer.sendActionFailed();
      return;
    }

    if (manufacturer.getCurrentMp() < recipeList.getMpCost())
    {
      manufacturer.sendPacket(Msg.NOT_ENOUGH_MP);
      buyer.sendPacket(new IStaticPacket[] { Msg.NOT_ENOUGH_MP, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success) });
      return;
    }

    buyer.getInventory().writeLock();
    try
    {
      if (buyer.getAdena() < _price) {
        buyer.sendPacket(new IStaticPacket[] { Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success) });
        return;
      }
      RecipeComponent[] recipes = recipeList.getRecipes();

      for (RecipeComponent recipe : recipes)
      {
        if (recipe.getQuantity() == 0) {
          continue;
        }
        ItemInstance item = buyer.getInventory().getItemByItemId(recipe.getItemId());

        if ((item != null) && (recipe.getQuantity() <= item.getCount()))
          continue;
        buyer.sendPacket(new IStaticPacket[] { Msg.NOT_ENOUGH_MATERIALS, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success) });
        return;
      }
      if (!buyer.reduceAdena(_price, false)) {
        buyer.sendPacket(new IStaticPacket[] { Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success) });
        return;
      }
      for (RecipeComponent recipe : recipes) {
        if (recipe.getQuantity() == 0)
          continue;
        buyer.getInventory().destroyItemByItemId(recipe.getItemId(), recipe.getQuantity());

        buyer.sendPacket(SystemMessage2.removeItems(recipe.getItemId(), recipe.getQuantity()));
      }

      long tax = TradeHelper.getTax(manufacturer, _price);
      if (tax > 0L)
      {
        _price -= tax;
        manufacturer.sendMessage(new CustomMessage("trade.HavePaidTax", manufacturer, new Object[0]).addNumber(tax));
      }

      manufacturer.addAdena(_price);
    }
    finally
    {
      buyer.getInventory().writeUnlock();
    }

    manufacturer.sendMessage(new CustomMessage("l2p.gameserver.RecipeController.GotOrder", manufacturer, new Object[0]).addString(recipeList.getRecipeName()));

    manufacturer.reduceCurrentMp(recipeList.getMpCost(), null);
    manufacturer.sendStatusUpdate(false, false, new int[] { 11 });

    int tryCount = 1; int successCount = 0;
    if (Rnd.chance(Config.CRAFT_DOUBLECRAFT_CHANCE)) {
      tryCount++;
    }
    for (int i = 0; i < tryCount; i++) {
      if (!Rnd.chance(recipeList.getSuccessRate()))
        continue;
      int itemId = recipeList.getFoundation() != 0 ? recipeList.getItemId() : Rnd.chance(Config.CRAFT_MASTERWORK_CHANCE) ? recipeList.getFoundation() : recipeList.getItemId();
      long count = recipeList.getCount();
      ItemFunctions.addItem(buyer, itemId, count, true);
      success = 1;
      successCount++;
    }

    if (successCount == 0)
    {
      SystemMessage sm = new SystemMessage(1150);
      sm.addString(manufacturer.getName());
      sm.addItemName(recipeList.getItemId());
      sm.addNumber(_price);
      buyer.sendPacket(sm);

      sm = new SystemMessage(1149);
      sm.addString(buyer.getName());
      sm.addItemName(recipeList.getItemId());
      sm.addNumber(_price);
      manufacturer.sendPacket(sm);
    }
    else if ((recipeList.getCount() > 1) || (successCount > 1))
    {
      SystemMessage sm = new SystemMessage(1148);
      sm.addString(manufacturer.getName());
      sm.addItemName(recipeList.getItemId());
      sm.addNumber(recipeList.getCount() * successCount);
      sm.addNumber(_price);
      buyer.sendPacket(sm);

      sm = new SystemMessage(1152);
      sm.addString(buyer.getName());
      sm.addItemName(recipeList.getItemId());
      sm.addNumber(recipeList.getCount() * successCount);
      sm.addNumber(_price);
      manufacturer.sendPacket(sm);
    }
    else
    {
      SystemMessage sm = new SystemMessage(1146);
      sm.addString(manufacturer.getName());
      sm.addItemName(recipeList.getItemId());
      sm.addNumber(_price);
      buyer.sendPacket(sm);

      sm = new SystemMessage(1151);
      sm.addString(buyer.getName());
      sm.addItemName(recipeList.getItemId());
      sm.addNumber(_price);
      manufacturer.sendPacket(sm);
    }

    buyer.sendChanges();
    buyer.sendPacket(new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
  }
}