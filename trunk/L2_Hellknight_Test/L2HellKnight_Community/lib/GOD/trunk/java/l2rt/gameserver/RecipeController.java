package l2rt.gameserver;

import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.extensions.Stat;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.templates.L2EtcItem.EtcItemType;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Rnd;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

public class RecipeController
{
	protected static Logger _log = Logger.getLogger(RecipeController.class.getName());
	private static RecipeController _instance;

	private HashMap<Integer, L2Recipe> _listByRecipeId;
	private HashMap<Integer, L2Recipe> _listByRecipeItem;

	public static RecipeController getInstance()
	{
		if(_instance == null)
			_instance = new RecipeController();
		return _instance;
	}

	public RecipeController()
	{
		_listByRecipeId = new HashMap<Integer, L2Recipe>();
		_listByRecipeItem = new HashMap<Integer, L2Recipe>();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		FiltredPreparedStatement st2 = null;
		ResultSet list = null, rset2 = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM recipes");
			st2 = con.prepareStatement("SELECT * FROM `recitems` WHERE `rid`=?");
			list = statement.executeQuery();

			while(list.next())
			{
				Vector<L2RecipeComponent> recipePartList = new Vector<L2RecipeComponent>();

				boolean isDvarvenCraft = list.getBoolean("dwarven");
				String recipeName = list.getString("name");
				int id = list.getInt("id");
				int recipeId = list.getShort("recid");
				int level = list.getInt("lvl");
				short itemId = list.getShort("item");
				short foundation = list.getShort("foundation");
				short count = list.getShort("q");
				int mpCost = list.getInt("mp");
				int successRate = list.getInt("success");
				long exp = list.getLong("exp");
				long sp = list.getLong("sp");

				//material
				st2.setInt(1, id);
				rset2 = st2.executeQuery();
				while(rset2.next())
				{
					int rpItemId = rset2.getInt("item");
					int quantity = rset2.getInt("q");
					L2RecipeComponent rp = new L2RecipeComponent(rpItemId, quantity);
					recipePartList.add(rp);
				}

				// Верхняя или fullbody часть не может быть foundation
				if(!Config.CRAFT_MASTERWORK_CHEST && foundation > 0)
				{
					L2Item foundationItem = ItemTemplates.getInstance().getTemplate(foundation);
					if(foundationItem.getBodyPart() == L2Item.SLOT_CHEST || foundationItem.getBodyPart() == L2Item.SLOT_FULL_ARMOR)
						foundation = 0;
				}

				L2Recipe recipeList = new L2Recipe(id, level, recipeId, recipeName, successRate, mpCost, itemId, foundation, count, exp, sp, isDvarvenCraft);
				for(L2RecipeComponent recipePart : recipePartList)
					recipeList.addRecipe(recipePart);
				_listByRecipeId.put(id, recipeList);
				_listByRecipeItem.put(recipeId, recipeList);
			}

			_log.config("RecipeController: Loaded " + _listByRecipeId.size() + " Recipes.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseSR(st2, rset2);
			DatabaseUtils.closeDatabaseCSR(con, statement, list);
		}
	}

	public Collection<L2Recipe> getRecipes()
	{
		return _listByRecipeId.values();
	}

	public L2Recipe getRecipeByRecipeId(int listId)
	{
		return _listByRecipeId.get(listId);
	}

	public L2Recipe getRecipeByRecipeItem(int itemId)
	{
		return _listByRecipeItem.get(itemId);
	}

	public void requestBookOpen(L2Player player, boolean isDwarvenCraft)
	{
		RecipeBookItemList response = new RecipeBookItemList(isDwarvenCraft, (int) player.getCurrentMp());
		if(isDwarvenCraft)
			response.setRecipes(player.getDwarvenRecipeBook());
		else
			response.setRecipes(player.getCommonRecipeBook());
		player.sendPacket(response);
	}

	public void requestMakeItem(L2Player player, int recipeListId)
	{
		L2Recipe recipeList = getRecipeByRecipeId(recipeListId);
		player.resetWaitSitTime();

		if(recipeList == null || recipeList.getRecipes().length == 0)
		{
			player.sendPacket(Msg.THE_RECIPE_IS_INCORRECT);
			return;
		}

		synchronized (player)
		{
			if(player.getCurrentMp() < recipeList.getMpCost())
			{
				player.sendPacket(Msg.NOT_ENOUGH_MP, new RecipeItemMakeInfo(recipeList.getId(), player, 0));
				return;
			}

			if(!player.findRecipe(recipeListId))
			{
				player.sendPacket(Msg.PLEASE_REGISTER_A_RECIPE, Msg.ActionFail);
				return;
			}
		}

		synchronized (player.getInventory())
		{
			L2RecipeComponent[] recipes = recipeList.getRecipes();
			Inventory inventory = player.getInventory();
			for(L2RecipeComponent recipe : recipes)
			{
				if(recipe.getQuantity() == 0)
					continue;

				if(Config.ALT_GAME_UNREGISTER_RECIPE && ItemTemplates.getInstance().getTemplate(recipe.getItemId()).getItemType() == EtcItemType.RECIPE)
				{
					L2Recipe rp = RecipeController.getInstance().getRecipeByRecipeItem(recipe.getItemId());
					if(player.hasRecipe(rp))
						continue;
					player.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeItemMakeInfo(recipeList.getId(), player, 0));
					return;
				}

				L2ItemInstance invItem = inventory.getItemByItemId(recipe.getItemId());

				if(invItem == null || recipe.getQuantity() > invItem.getCount())
				{
					player.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeItemMakeInfo(recipeList.getId(), player, 0));
					return;
				}
			}

			player.reduceCurrentMp(recipeList.getMpCost(), null);

			for(L2RecipeComponent recipe : recipes)
				if(recipe.getQuantity() != 0)
				{
					L2ItemInstance invItem = inventory.getItemByItemId(recipe.getItemId());
					if(Config.ALT_GAME_UNREGISTER_RECIPE && ItemTemplates.getInstance().getTemplate(recipe.getItemId()).getItemType() == EtcItemType.RECIPE)
						player.unregisterRecipe(RecipeController.getInstance().getRecipeByRecipeItem(recipe.getItemId()).getId());
					else
					{
						inventory.destroyItem(invItem, recipe.getQuantity(), false);
						player.sendPacket(SystemMessage.removeItems(invItem.getItemId(), recipe.getQuantity()));
					}
				}
		}

		int count = 1;
		if(rollMW(player, recipeList))
			count++;

		int success = 0;
		for(int i = 0; i < count; i++)
			if(Rnd.chance(recipeList.getSuccessRate()))
			{
				L2ItemInstance createdItem = ItemTemplates.getInstance().createItem(rollMW(player, recipeList) ? recipeList.getFoundation() : recipeList.getItemId());
				createdItem.setCount(recipeList.getCount());

				if(Config.CRAFT_COUNTER)
					player.incrementCraftCounter(createdItem.getItemId(), recipeList.getCount());

				player.sendPacket(SystemMessage.obtainItems(createdItem));
				player.getInventory().addItem(createdItem);
				success = 1;
			}

		if(success == 0)
			player.sendPacket(new SystemMessage(SystemMessage.S1_MANUFACTURING_FAILURE).addItemName(recipeList.getItemId()));

		if(Config.ALT_GAME_EXP_FOR_CRAFT)
			player.addExpAndSp((long) (recipeList.getExp() * Config.RATE_XP), (long) (recipeList.getSp() * Config.RATE_SP), true, false);

		player.sendStatusUpdate(false, StatusUpdate.CUR_LOAD, StatusUpdate.CUR_MP);
		player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(), player, success));
	}

	/***************************************************************************/

	public void requestManufactureItem(L2Player player, L2Player employer, int recipeListId)
	{
		L2Recipe recipeList = getRecipeByRecipeId(recipeListId);
		if(recipeList == null)
			return;

		player.resetWaitSitTime();
		int success = 0;

		player.sendMessage(new CustomMessage("l2rt.gameserver.RecipeController.GotOrder", player).addString(recipeList.getRecipeName()));

		if(recipeList.getRecipes().length == 0)
		{
			player.sendMessage(new CustomMessage("l2rt.gameserver.RecipeController.NoRecipe", player).addString(recipeList.getRecipeName()));
			employer.sendMessage(new CustomMessage("l2rt.gameserver.RecipeController.NoRecipe", player).addString(recipeList.getRecipeName()));
			return;
		}

		long price = 0;
		for(L2ManufactureItem temp : player.getCreateList().getList())
			if(temp.getRecipeId() == recipeList.getId())
			{
				price = temp.getCost();
				break;
			}

		synchronized (player)
		{
			if(player.getCurrentMp() < recipeList.getMpCost())
			{
				player.sendPacket(Msg.NOT_ENOUGH_MP);
				employer.sendPacket(Msg.NOT_ENOUGH_MP, new RecipeShopItemInfo(player.getObjectId(), recipeListId, price, success, employer));
				return;
			}

			if(!player.findRecipe(recipeListId))
			{
				player.sendPacket(Msg.PLEASE_REGISTER_A_RECIPE, Msg.ActionFail);
				return;
			}
		}

		if(employer.getAdena() < price)
		{
			employer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(player.getObjectId(), recipeListId, price, success, employer));
			return;
		}

		synchronized (employer.getInventory())
		{
			L2RecipeComponent[] recipes = recipeList.getRecipes();
			Inventory inventory = employer.getInventory();
			for(L2RecipeComponent recipe : recipes)
			{
				if(recipe.getQuantity() == 0)
					continue;

				L2ItemInstance invItem = inventory.getItemByItemId(recipe.getItemId());

				if(invItem == null || recipe.getQuantity() > invItem.getCount())
				{
					employer.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeShopItemInfo(player.getObjectId(), recipeListId, price, success, employer));
					return;
				}
			}

			player.reduceCurrentMp(recipeList.getMpCost(), null);

			for(L2RecipeComponent recipe : recipes)
				if(recipe.getQuantity() != 0)
				{
					L2ItemInstance invItem = inventory.getItemByItemId(recipe.getItemId());
					inventory.destroyItem(invItem, recipe.getQuantity(), false);
					employer.sendPacket(SystemMessage.removeItems(invItem.getItemId(), recipe.getQuantity()));
				}
		}

		if(price > 0)
		{
			employer.reduceAdena(price, false);
			player.addAdena(price);

			int tax = (int) (price * Config.SERVICES_TRADE_TAX / 100);
			if(player.isInZone(L2Zone.ZoneType.offshore))
				tax = (int) (price * Config.SERVICES_OFFSHORE_TRADE_TAX / 100);
			if(Config.SERVICES_TRADE_TAX_ONLY_OFFLINE && !player.isInOfflineMode())
				tax = 0;
			if(player.getReflection().getId() == -1) // Особая зона в Parnassus
				tax = 0;
			if(tax > 0)
			{
				player.reduceAdena(tax, false);
				Stat.addTax(tax);
				player.sendMessage(new CustomMessage("trade.HavePaidTax", player).addNumber(tax));
			}
		}

		int tryCount = 1, successCount = 0;
		if(rollMW(player, recipeList))
			tryCount++;

		SystemMessage msgtoemployer;
		SystemMessage msgtomaster;

		for(int i = 0; i < tryCount; i++)
			if(Rnd.chance(recipeList.getSuccessRate()))
			{
				L2ItemInstance createdItem = ItemTemplates.getInstance().createItem(rollMW(player, recipeList) ? recipeList.getFoundation() : recipeList.getItemId());
				createdItem.setCount(recipeList.getCount());
				employer.getInventory().addItem(createdItem);
				employer.sendPacket(SystemMessage.obtainItems(createdItem));
				if(Config.CRAFT_COUNTER)
					player.incrementCraftCounter((int) recipeList.getItemId(), recipeList.getCount());
				success = 1;
				successCount++;
			}

		if(successCount == 0)
		{
			msgtoemployer = new SystemMessage(SystemMessage.S1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA);
			msgtoemployer.addString(player.getName());
			msgtoemployer.addItemName(recipeList.getItemId());
			msgtoemployer.addNumber(price);
			msgtomaster = new SystemMessage(SystemMessage.THE_ATTEMPT_TO_CREATE_S2_FOR_S1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED);
			msgtomaster.addString(employer.getName());
			msgtomaster.addItemName(recipeList.getItemId());
			msgtomaster.addNumber(price);
			player.sendPacket(msgtomaster);
			employer.sendPacket(msgtoemployer);
		}
		else if(recipeList.getCount() > 1 || successCount > 1)
		{
			msgtoemployer = new SystemMessage(SystemMessage.S1_CREATED_S2_S3_AT_THE_PRICE_OF_S4_ADENA);
			msgtoemployer.addString(player.getName());
			msgtoemployer.addItemName(recipeList.getItemId());
			msgtoemployer.addNumber(recipeList.getCount() * successCount);
			msgtoemployer.addNumber(price);
			msgtomaster = new SystemMessage(SystemMessage.S2_S3_HAVE_BEEN_SOLD_TO_S1_FOR_S4_ADENA);
			msgtomaster.addString(employer.getName());
			msgtomaster.addItemName(recipeList.getItemId());
			msgtomaster.addNumber(recipeList.getCount() * successCount);
			msgtomaster.addNumber(price);
			player.sendPacket(msgtomaster);
			employer.sendPacket(msgtoemployer);
		}
		else
		{
			msgtoemployer = new SystemMessage(SystemMessage.S1_CREATED_S2_AFTER_RECEIVING_S3_ADENA);
			msgtoemployer.addString(player.getName());
			msgtoemployer.addItemName(recipeList.getItemId());
			msgtoemployer.addNumber(price);
			msgtomaster = new SystemMessage(SystemMessage.S2_IS_SOLD_TO_S1_AT_THE_PRICE_OF_S3_ADENA);
			msgtomaster.addString(employer.getName());
			msgtomaster.addItemName(recipeList.getItemId());
			msgtomaster.addNumber(price);
			player.sendPacket(msgtomaster);
			employer.sendPacket(msgtoemployer);
		}

		if(Config.ALT_GAME_EXP_FOR_CRAFT)
			player.addExpAndSp((long) (recipeList.getExp() * Config.RATE_XP), (long) (recipeList.getSp() * Config.RATE_SP), true, false);
		player.sendStatusUpdate(false, StatusUpdate.CUR_LOAD, StatusUpdate.CUR_MP);

		employer.sendChanges();
		employer.sendStatusUpdate(false, StatusUpdate.CUR_LOAD);
		employer.sendPacket(new RecipeShopItemInfo(player.getObjectId(), recipeListId, price, success, employer));
	}

	private boolean rollMW(L2Player p, L2Recipe recipeList)
	{
		return recipeList.getFoundation() > 0 && Rnd.chance(Config.CRAFT_MASTERWORK_CHANCE + (p.getLevel() - craft_lvl[p.getSkillLevel(L2Skill.SKILL_CRAFTING)]) * Config.CRAFT_MASTERWORK_LEVEL_MOD);
	}

	private static final int[] craft_lvl = { 0, 5, 20, 28, 36, 43, 49, 55, 62, 70, 82 };
}