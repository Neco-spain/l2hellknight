package services;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javolution.util.FastMap;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.RecipeController;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.geodata.GeoMove;
import l2rt.gameserver.model.L2ManufactureItem;
import l2rt.gameserver.model.L2ManufactureList;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Recipe;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance.ItemClass;
import l2rt.gameserver.network.serverpackets.ExShowTrace;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import l2rt.gameserver.network.serverpackets.PrivateStoreMsgSell;
import l2rt.gameserver.network.serverpackets.RadarControl;
import l2rt.gameserver.network.serverpackets.RecipeShopMsg;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Util;

public class ItemBroker extends Functions implements ScriptFile
{
	private static FastMap<Integer, NpcInfo> _npcInfos = new FastMap<Integer, NpcInfo>().setShared(true);

	public class NpcInfo
	{
		public long lastUpdate;
		public TreeMap<String, TreeMap<Long, Item>> bestSellItems;
		public TreeMap<String, TreeMap<Long, Item>> bestBuyItems;
		public TreeMap<String, TreeMap<Long, Item>> bestCraftItems;
	}

	public class Item
	{
		public int itemId;
		public int itemObjId;
		public int type;
		public long price;
		public long count;
		public long enchant;
		public long merchantStoredId;
		public String name;
		public String merchantName;
		public ArrayList<Location> path;
		public Element element;

		public Item(int itemId, int type, long price, long count, int enchant, String itemName, Long storedId, String merchantName, ArrayList<Location> path, int itemObjId, Element element)
		{
			this.itemId = itemId;
			this.type = type;
			this.price = price;
			this.count = count;
			this.enchant = enchant;
			this.name = itemName;
			this.merchantStoredId = storedId;
			this.merchantName = merchantName;
			this.path = path;
			this.itemObjId = itemObjId;
			this.element = element;
		}
	}

	public class Element
	{
		public int[] attackElement;
		public int defenceFire;
		public int defenceWater;
		public int defenceWind;
		public int defenceEarth;
		public int defenceHoly;
		public int defenceUnholy;

		public Element(TradeItem item)
		{
			attackElement = item.getAttackElement();
			defenceEarth = item.getDefenceEarth();
			defenceFire = item.getDefenceFire();
			defenceHoly = item.getDefenceHoly();
			defenceUnholy = item.getDefenceUnholy();
			defenceWater = item.getDefenceWater();
			defenceWind = item.getDefenceWind();
		}
	}

	private String parseElement(Item item)
	{
		String element = "";
		if(item.element != null)
			if(item.element.attackElement != null && item.element.attackElement[0] != L2Item.ATTRIBUTE_NONE)
			{
				element = " &nbsp;<font color=\"7CFC00\">+" + item.element.attackElement[1];
				switch(item.element.attackElement[0])
				{
					case L2Item.ATTRIBUTE_FIRE:
						element += " Fire";
						break;
					case L2Item.ATTRIBUTE_WATER:
						element += " Water";
						break;
					case L2Item.ATTRIBUTE_WIND:
						element += " Wind";
						break;
					case L2Item.ATTRIBUTE_EARTH:
						element += " Earth";
						break;
					case L2Item.ATTRIBUTE_HOLY:
						element += " Holy";
						break;
					case L2Item.ATTRIBUTE_DARK:
						element += " Unholy";
						break;
				}
				element += "</font>";
			}
			else if(item.element.defenceFire > 0)
				element = " &nbsp;<font color=\"7CFC00\">+" + item.element.defenceFire + " Fire</font>";
			else if(item.element.defenceWater > 0)
				element = " &nbsp;<font color=\"7CFC00\">+" + item.element.defenceWater + " Water</font>";
			else if(item.element.defenceWind > 0)
				element = " &nbsp;<font color=\"7CFC00\">+" + item.element.defenceWind + " Wind</font>";
			else if(item.element.defenceEarth > 0)
				element = " &nbsp;<font color=\"7CFC00\">+" + item.element.defenceEarth + " Earth</font>";
			else if(item.element.defenceHoly > 0)
				element = " &nbsp;<font color=\"7CFC00\">+" + item.element.defenceHoly + " Holy</font>";
			else if(item.element.defenceUnholy > 0)
				element = " &nbsp;<font color=\"7CFC00\">+" + item.element.defenceUnholy + " Unholy</font>";
		return element;
	}

	private TreeMap<String, TreeMap<Long, Item>> getItems(int type)
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return null;
		updateInfo(player, npc);
		NpcInfo info = _npcInfos.get(getNpc().getObjectId());
		if(info == null)
			return null;
		switch(type)
		{
			case L2Player.STORE_PRIVATE_SELL:
				return info.bestSellItems;
			case L2Player.STORE_PRIVATE_BUY:
				return info.bestBuyItems;
			case L2Player.STORE_PRIVATE_MANUFACTURE:
				return info.bestCraftItems;
		}
		return null;
	}

	public String DialogAppend_32320(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_32321(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_32322(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String getHtmlAppends(Integer val)
	{
		StringBuffer append = new StringBuffer();
		int type = 0;
		String typeNameRu = "";
		String typeNameEn = "";

		switch(val)
		{
			case 0:
				if(((L2Player) getSelf()).isLangRus())
				{
					append.append("<br><font color=\"LEVEL\">Поиск торговцев:</font><br1>");
					append.append("[npc_%objectId%_Chat 11|<font color=\"FF9900\">Список продаваемых товаров</font>]<br1>");
					append.append("[npc_%objectId%_Chat 13|<font color=\"FF9900\">Список покупаемых товаров</font>]<br1>");
					append.append("[npc_%objectId%_Chat 15|<font color=\"FF9900\">Список создаваемых товаров</font>]<br1>");
				}
				else
				{
					append.append("<br><font color=\"LEVEL\">Search for dealers:</font><br1>");
					append.append("[npc_%objectId%_Chat 11|<font color=\"FF9900\">The list of goods for sale</font>]<br1>");
					append.append("[npc_%objectId%_Chat 13|<font color=\"FF9900\">The list of goods to buy</font>]<br1>");
					append.append("[npc_%objectId%_Chat 15|<font color=\"FF9900\">The list of goods to craft</font>]<br1>");
				}
				break;
			case 11:
				type = L2Player.STORE_PRIVATE_SELL;
				typeNameRu = "продаваемых";
				typeNameEn = "sell";
				break;
			case 13:
				type = L2Player.STORE_PRIVATE_BUY;
				typeNameRu = "покупаемых";
				typeNameEn = "buy";
				break;
			case 15:
				type = L2Player.STORE_PRIVATE_MANUFACTURE;
				typeNameRu = "создаваемых";
				typeNameEn = "craft";
				break;
		}

		if(type > 0)
			if(((L2Player) getSelf()).isLangRus())
			{
				append.append("!Список ").append(typeNameRu).append(" товаров:<br>");

				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 0 1 1 0 0|<font color=\"FF9900\">Весь список</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 1 0 0|<font color=\"FF9900\">Снаряжение</font>]<br1>");
				if(type != L2Player.STORE_PRIVATE_BUY)
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 1 1 0|<font color=\"FF9900\">Снаряжение+</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 1 0 1|<font color=\"FF9900\">Редкое снаряжение</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 2 1 1 0 0|<font color=\"FF9900\">Расходные материалы</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 3 1 1 0 0|<font color=\"FF9900\">Ингредиенты</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 4 1 1 0 0|<font color=\"FF9900\">Ключевые ингредиенты</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 5 1 1 0 0|<font color=\"FF9900\">Рецепты</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 6 1 1 0 0|<font color=\"FF9900\">Книги и амулеты</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 7 1 1 0 0|<font color=\"FF9900\">Предметы для улучшения</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 8 1 1 0 0|<font color=\"FF9900\">Разное</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 9 1 1 0 0|<font color=\"FF9900\">Стандартные предметы</font>]<br1>");

				append.append("<edit var=\"tofind\" width=100><br1>");
				append.append("[scripts_services.ItemBroker:find ").append(type).append(" 1 1 \\$tofind|<font color=\"FF9900\">Найти</font>]<br1>");

				append.append("<br>[npc_%objectId%_Chat 0|<font color=\"FF9900\">Назад</font>]");
			}
			else
			{
				append.append("!The list of goods to ").append(typeNameEn).append(":<br>");

				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 0 1 1 0 0|<font color=\"FF9900\">List all</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 1 0 0|<font color=\"FF9900\">Equipment</font>]<br1>");
				if(type != L2Player.STORE_PRIVATE_BUY)
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 1 1 0|<font color=\"FF9900\">Equipment+</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 1 0 1|<font color=\"FF9900\">Rare equipment</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 2 1 1 0 0|<font color=\"FF9900\">Consumable</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 3 1 1 0 0|<font color=\"FF9900\">Matherials</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 4 1 1 0 0|<font color=\"FF9900\">Key matherials</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 5 1 1 0 0|<font color=\"FF9900\">Recipies</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 6 1 1 0 0|<font color=\"FF9900\">Books and amulets</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 7 1 1 0 0|<font color=\"FF9900\">Enchant items</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 8 1 1 0 0|<font color=\"FF9900\">Other</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 9 1 1 0 0|<font color=\"FF9900\">Commons</font>]<br1>");

				append.append("<edit var=\"tofind\" width=100><br1>");
				append.append("[scripts_services.ItemBroker:find ").append(type).append(" 1 1 \\$tofind|<font color=\"FF9900\">Find</font>]<br1>");

				append.append("<br>[npc_%objectId%_Chat 0|<font color=\"FF9900\">Back</font>]");
			}

		return append.toString();
	}

	public void list(String[] var)
	{
		int countPerPage = 9;

		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(var.length != 6)
		{
			show("Некорректные данные", player, npc);
			return;
		}

		int type;
		int itemType;
		int min;
		int max;
		int minEnchant;
		int rare;

		try
		{
			type = Integer.valueOf(var[0]);
			itemType = Integer.valueOf(var[1]);
			min = Integer.valueOf(var[2]);
			max = Integer.valueOf(var[3]);
			minEnchant = Integer.valueOf(var[4]);
			rare = Integer.valueOf(var[5]);
		}
		catch(Exception e)
		{
			show("Некорректные данные", player, npc);
			return;
		}

		if(max < countPerPage)
			max = countPerPage;

		ItemClass itemClass = itemType > 8 ? null : ItemClass.values()[itemType];

		TreeMap<String, TreeMap<Long, Item>> allItems = getItems(type);
		if(allItems == null)
		{
			show("Неизвестная ошибка", player, npc);
			return;
		}

		GArray<Item> items = new GArray<Item>();
		for(TreeMap<Long, Item> tempItems : allItems.values())
		{
			TreeMap<Long, Item> tempItems2 = new TreeMap<Long, Item>();
			for(Entry<Long, Item> entry : tempItems.entrySet())
			{
				Item tempItem = entry.getValue();
				if(tempItem == null)
					continue;
				if(tempItem.enchant < minEnchant)
					continue;
				L2Item temp = ItemTemplates.getInstance().getTemplate(tempItem.itemId);
				if(temp == null || rare > 0 && !temp.isRare())
					continue;
				if(itemClass == null ? !temp.isCommonItem() : temp.isCommonItem())
					continue;
				if(itemClass != null && itemClass != ItemClass.ALL && temp.getItemClass() != itemClass)
					continue;
				tempItems2.put(entry.getKey(), tempItem);
			}
			if(tempItems2.isEmpty())
				continue;

			Item item = type == L2Player.STORE_PRIVATE_BUY ? tempItems2.lastEntry().getValue() : tempItems2.firstEntry().getValue();
			if(item != null)
				items.add(item);
		}

		StringBuffer out = new StringBuffer("[npc_%objectId%_Chat " + ("1" + type) + "|««]&nbsp;");
		int pages = Math.max(1, items.size() / countPerPage + 1);
		if(pages > 1)
			for(int j = 1; j <= pages; j++)
				if(min == (j - 1) * countPerPage + 1)
					out.append(j).append("&nbsp;");
				else
					out.append("[scripts_services.ItemBroker:list ").append(type).append(" ").append(itemType).append(" ").append(((j - 1) * countPerPage + 1)).append(" ").append((j * countPerPage)).append(" ").append(minEnchant).append(" ").append(rare).append("|").append(j).append("]&nbsp;");

		out.append("<table width=100%>");

		int i = 0;
		for(Item item : items)
		{
			i++;
			if(i < min || i > max)
				continue;
			L2Item temp = ItemTemplates.getInstance().getTemplate(item.itemId);
			if(temp == null)
				continue;

			String icon = "<img src=icon." + temp.getIcon() + " width=32 height=32>";

			String color = "<font color=\"LEVEL\">";
			if(item.enchant > 0)
				color = "<font color=\"7CFC00\">+" + item.enchant + " ";
			if(temp.isRare())
				color = "<font color=\"0000FF\">Rare ";
			if(temp.isRare() && item.enchant > 0)
				color = "<font color=\"FF0000\">+" + item.enchant + " Rare ";

			out.append("<tr><td>").append(icon);
			out.append("</td><td><table width=100%><tr><td>[scripts_services.ItemBroker:listForItem ").append(type).append(" ").append(item.itemId).append(" ").append(minEnchant).append(" ").append(rare).append(" ").append(itemType).append(" ").append(min).append(" ").append(max).append("|");
			out.append(color).append(item.name).append("</font>]").append(parseElement(item)).append("</td></tr><tr><td>price: ").append(Util.formatAdena(item.price));
			if(temp.isStackable())
				out.append(", count: ").append(Util.formatAdena(item.count));
			out.append("</td></tr></table></td></tr>");
		}
		out.append("</table><br>&nbsp;");

		show(out.toString(), player, npc);
	}

	public void listForItem(String[] var)
	{
		int maxItems = 20;

		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(var.length != 7)
		{
			show("Некорректные данные", player, npc);
			return;
		}

		int type;
		int itemId;
		int minEnchant;
		int rare;
		// нужны только для запоминания, на какую страницу возвращаться
		int itemType;
		int min;
		int max;

		try
		{
			type = Integer.valueOf(var[0]);
			itemId = Integer.valueOf(var[1]);
			minEnchant = Integer.valueOf(var[2]);
			rare = Integer.valueOf(var[3]);
			itemType = Integer.valueOf(var[4]);
			min = Integer.valueOf(var[5]);
			max = Integer.valueOf(var[6]);
		}
		catch(Exception e)
		{
			show("Некорректные данные", player, npc);
			return;
		}

		L2Item template = ItemTemplates.getInstance().getTemplate(itemId);
		if(template == null)
		{
			show("Неизвестная ошибка", player, npc);
			return;
		}

		TreeMap<String, TreeMap<Long, Item>> allItems = getItems(type);
		if(allItems == null)
		{
			show("Неизвестная ошибка", player, npc);
			return;
		}

		TreeMap<Long, Item> items = allItems.get(template.getName());
		if(items == null)
		{
			show("Неизвестная ошибка", player, npc);
			return;
		}

		StringBuffer out = new StringBuffer("[scripts_services.ItemBroker:list " + type + " " + itemType + " " + min + " " + max + " " + minEnchant + " " + rare + "|««]");

		out.append("<table width=100%>");

		NavigableMap<Long, Item> sortedItems = type == L2Player.STORE_PRIVATE_BUY ? items.descendingMap() : items;
		if(sortedItems == null)
		{
			show("Неизвестная ошибка", player, npc);
			return;
		}

		int i = 0;
		for(Item item : sortedItems.values())
		{
			if(item.enchant < minEnchant)
				continue;
			L2Item temp = ItemTemplates.getInstance().getTemplate(item.itemId);
			if(temp == null || rare > 0 && !temp.isRare())
				continue;

			i++;
			if(i > maxItems)
				break;

			String icon = "<img src=icon." + temp.getIcon() + " width=32 height=32>";

			String color = "<font color=\"LEVEL\">";
			if(item.enchant > 0)
				color = "<font color=\"7CFC00\">+" + item.enchant + " ";
			if(temp.isRare())
				color = "<font color=\"0000FF\">Rare ";
			if(temp.isRare() && item.enchant > 0)
				color = "<font color=\"FF0000\">+" + item.enchant + " Rare ";

			out.append("<tr><td>").append(icon);
			out.append("</td><td><table width=100%><tr><td>[scripts_services.ItemBroker:path ");
			out.append(type).append(" ").append(item.itemId).append(" ").append(item.itemObjId).append("|");
			out.append(color).append(item.name).append("</font>]").append(parseElement(item)).append("</td></tr><tr><td>price: ").append(Util.formatAdena(item.price));
			if(temp.isStackable())
				out.append(", count: ").append(Util.formatAdena(item.count));
			out.append(", owner: ").append(item.merchantName);
			out.append("</td></tr></table></td></tr>");
		}
		out.append("</table><br>&nbsp;");

		show(out.toString(), player, npc);
	}

	public void path(String[] var)
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(var.length != 3)
		{
			show("Некорректные данные", player, npc);
			return;
		}

		int type;
		int itemId;
		int itemObjId;

		try
		{
			type = Integer.valueOf(var[0]);
			itemId = Integer.valueOf(var[1]);
			itemObjId = Integer.valueOf(var[2]);
		}
		catch(Exception e)
		{
			show("Некорректные данные", player, npc);
			return;
		}

		L2Item temp = ItemTemplates.getInstance().getTemplate(itemId);
		if(temp == null)
		{
			show("Неизвестная ошибка", player, npc);
			return;
		}

		TreeMap<String, TreeMap<Long, Item>> allItems = getItems(type);
		if(allItems == null)
		{
			show("Неизвестная ошибка", player, npc);
			return;
		}

		TreeMap<Long, Item> items = allItems.get(temp.getName());
		if(items == null)
		{
			show("Неизвестная ошибка", player, npc);
			return;
		}

		Item item = null;
		for(Item i : items.values())
			if(i.itemObjId == itemObjId)
			{
				item = i;
				break;
			}

		if(item == null)
		{
			show("Неизвестная ошибка", player, npc);
			return;
		}

		L2Player trader = L2ObjectsStorage.getAsPlayer(item.merchantStoredId);
		player.sendPacket(new L2GameServerPacket[] { new RadarControl(2, 2, trader.getX(), trader.getY(), trader.getZ()), new RadarControl(0, 1, trader.getX(), trader.getY(), trader.getZ()) });
		player.setTarget(trader);
		
		
		// Показываем игроку торговца, если тот скрыт
		if(player.getVarB("notraders"))
		{
			
			if(trader != null)
			{
				player.sendPacket(new L2GameServerPacket[] { trader.newCharInfo() });
				if(trader.getPrivateStoreType() == L2Player.STORE_PRIVATE_BUY)
					player.sendPacket(new L2GameServerPacket[] { new PrivateStoreMsgBuy(trader) });
				else if(trader.getPrivateStoreType() == L2Player.STORE_PRIVATE_SELL || trader.getPrivateStoreType() == L2Player.STORE_PRIVATE_SELL_PACKAGE)
					player.sendPacket(new L2GameServerPacket[] { new PrivateStoreMsgSell(trader) });
				else if(trader.getPrivateStoreType() == L2Player.STORE_PRIVATE_MANUFACTURE)
					player.sendPacket(new L2GameServerPacket[] { new RecipeShopMsg(trader) });
			}
		}
	}

	public void updateInfo(L2Player player, L2NpcInstance npc)
	{
		NpcInfo info = _npcInfos.get(npc.getObjectId());
		if(info == null || info.lastUpdate < System.currentTimeMillis() - 300000)
		{
			info = new NpcInfo();
			info.lastUpdate = System.currentTimeMillis();
			info.bestBuyItems = new TreeMap<String, TreeMap<Long, Item>>();
			info.bestSellItems = new TreeMap<String, TreeMap<Long, Item>>();
			info.bestCraftItems = new TreeMap<String, TreeMap<Long, Item>>();

			int itemObjId = 0; // Обычный objId не подходит для покупаемых предметов

			for(L2Player pl : L2World.getAroundPlayers(npc, 4000, 400))
			{
				int type = pl.getPrivateStoreType();
				if(type == L2Player.STORE_PRIVATE_SELL || type == L2Player.STORE_PRIVATE_BUY || type == L2Player.STORE_PRIVATE_MANUFACTURE)
				{
					ArrayList<Location> path = new ArrayList<Location>();
					if(GeoEngine.canMoveToCoord(npc.getX(), npc.getY(), npc.getZ(), pl.getX(), pl.getY(), pl.getZ(), npc.getReflection().getGeoIndex()))
					{
						path.add(npc.getLoc());
						path.add(pl.getLoc());
					}
					else
						path = GeoMove.findPath(npc.getX(), npc.getY(), npc.getZ(), new Location(pl), player, false, npc.getReflection().getGeoIndex());
					if(!path.isEmpty())
					{
						TreeMap<String, TreeMap<Long, Item>> items = null;
						ConcurrentLinkedQueue<TradeItem> tradeList = null;

						switch(type)
						{
							case L2Player.STORE_PRIVATE_SELL:
								items = info.bestSellItems;
								tradeList = pl.getSellList();

								for(TradeItem item : tradeList)
								{
									L2Item temp = ItemTemplates.getInstance().getTemplate(item.getItemId());
									if(temp == null)
										continue;
									TreeMap<Long, Item> oldItems = items.get(temp.getName());
									if(oldItems == null)
									{
										oldItems = new TreeMap<Long, Item>();
										items.put(temp.getName(), oldItems);
									}
									Item newItem = new Item(item.getItemId(), type, item.getOwnersPrice(), item.getCount(), item.getEnchantLevel(), temp.getName(), pl.getStoredId(), pl.getName(), path, item.getObjectId(), new Element(item));
									long key = newItem.price * 100;
									while(key < newItem.price * 100 + 100 && oldItems.containsKey(key))
										// До 100 предметов с одинаковыми ценами
										key++;
									oldItems.put(key, newItem);
								}

								break;
							case L2Player.STORE_PRIVATE_BUY:
								items = info.bestBuyItems;
								tradeList = pl.getBuyList();

								for(TradeItem item : tradeList)
								{
									L2Item temp = ItemTemplates.getInstance().getTemplate(item.getItemId());
									if(temp == null)
										continue;
									TreeMap<Long, Item> oldItems = items.get(temp.getName());
									if(oldItems == null)
									{
										oldItems = new TreeMap<Long, Item>();
										items.put(temp.getName(), oldItems);
									}
									Item newItem = new Item(item.getItemId(), type, item.getOwnersPrice(), item.getCount(), item.getEnchantLevel(), temp.getName(), pl.getStoredId(), pl.getName(), path, itemObjId++, new Element(item));
									long key = newItem.price * 100;
									while(key < newItem.price * 100 + 100 && oldItems.containsKey(key))
										// До 100 предметов с одинаковыми ценами
										key++;
									oldItems.put(key, newItem);
								}

								break;
							case L2Player.STORE_PRIVATE_MANUFACTURE:
								items = info.bestCraftItems;
								L2ManufactureList createList = pl.getCreateList();
								if(createList == null)
									continue;

								for(L2ManufactureItem mitem : createList.getList())
								{
									int recipeId = mitem.getRecipeId();
									L2Recipe recipe = RecipeController.getInstance().getRecipeByRecipeId(recipeId);
									if(recipe == null)
										continue;

									L2Item temp = ItemTemplates.getInstance().getTemplate(recipe.getItemId());
									if(temp == null)
										continue;
									TreeMap<Long, Item> oldItems = items.get(temp.getName());
									if(oldItems == null)
									{
										oldItems = new TreeMap<Long, Item>();
										items.put(temp.getName(), oldItems);
									}
									Item newItem = new Item(recipe.getItemId(), type, mitem.getCost(), recipe.getCount(), 0, temp.getName(), pl.getStoredId(), pl.getName(), path, itemObjId++, null);
									long key = newItem.price * 100;
									while(key < newItem.price * 100 + 100 && oldItems.containsKey(key))
										// До 100 предметов с одинаковыми ценами
										key++;
									oldItems.put(key, newItem);
								}

								break;
							default:
								continue;
						}
					}
				}
			}
			_npcInfos.put(npc.getObjectId(), info);
		}
	}

	public void find(String[] var)
	{
		int countPerPage = 9;

		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(var.length < 4 || var.length > 8)
		{
			show("Некорректные данные", player, npc);
			return;
		}

		int type;
		int min;
		int max;
		String str = "";

		try
		{
			type = Integer.valueOf(var[0]);
			min = Integer.valueOf(var[1]);
			max = Integer.valueOf(var[2]);
			for(int i = 3; i < var.length; i++)
				str += var[i];
		}
		catch(Exception e)
		{
			show("Некорректные данные", player, npc);
			return;
		}

		if(max < countPerPage)
			max = countPerPage;

		TreeMap<String, TreeMap<Long, Item>> allItems = getItems(type);
		if(allItems == null)
		{
			show("Неизвестная ошибка", player, npc);
			return;
		}

		GArray<Item> items = new GArray<Item>();
		mainLoop: for(Entry<String, TreeMap<Long, Item>> entry : allItems.entrySet())
		{
			for(int i = 3; i < var.length; i++)
				if(entry.getKey().toLowerCase().indexOf(var[i].toLowerCase()) == -1)
					continue mainLoop;
			Item item = type == L2Player.STORE_PRIVATE_BUY ? entry.getValue().lastEntry().getValue() : entry.getValue().firstEntry().getValue();
			if(item != null && ItemTemplates.getInstance().getTemplate(item.itemId) != null)
				items.add(item);
		}

		StringBuffer out = new StringBuffer("[npc_%objectId%_Chat " + ("1" + type) + "|««]&nbsp;");
		int pages = Math.min(10, Math.max(1, items.size() / countPerPage + 1));
		
		if(pages > 1)
			for(int j = 1; j <= pages; j++)
				if(min == (j - 1) * countPerPage + 1)
					out.append(j).append("&nbsp;");
				else
					out.append("[scripts_services.ItemBroker:find ").append(type).append(" ").append(((j - 1) * countPerPage + 1)).append(" ").append((j * countPerPage)).append(" ").append(str).append("|").append(j).append("]&nbsp;");

		out.append("<table width=100%>");

		int i = 0;
		for(Item item : items)
		{
			i++;
			if(i < min || i > max)
				continue;
			L2Item temp = ItemTemplates.getInstance().getTemplate(item.itemId);
			if(temp == null)
				continue;

			out.append("<tr><td>").append("<img src=icon.").append(temp.getIcon()).append(" width=32 height=32>");
			out.append("</td><td><table width=100%><tr><td>[scripts_services.ItemBroker:listForItem ").append(type).append(" ").append(item.itemId).append(" ").append(0).append(" ").append(0).append(" ").append(0).append(" ").append(min).append(" ").append(max).append("|");
			out.append("<font color=\"LEVEL\">").append(item.name).append("</font>]").append("</td></tr>");
			out.append("</table></td></tr>");
		}

		out.append("</table><br>&nbsp;");

		show(out.toString(), player, npc);
	}

	public static ExShowTrace Points2Trace(L2Player player, ArrayList<Location> points, int step, int time)
	{
		ExShowTrace result = new ExShowTrace();
		Location _prev = null;
		int i = 0;
		for(Location p : points)
		{	
			i++;
			if(player.isGM())
				player.sendMessage(p.toString());
			if(_prev != null)
				result.addLine(_prev, p, step, time);
			_prev = p;
		}
		return result;
	}

	public void onLoad()
	{
		System.out.println("Loaded Service: Item Broker");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}