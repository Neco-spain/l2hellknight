package l2rt.gameserver.model;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.model.base.MultiSellEntry;
import l2rt.gameserver.model.base.MultiSellIngredient;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.MultiSellList;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2Item.Grade;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.templates.L2Weapon.WeaponType;
import l2rt.gameserver.templates.L2Armor;
import l2rt.gameserver.templates.L2Armor.ArmorType;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.GArray;
import l2rt.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Multisell list manager
 */
public class L2Multisell
{
	private static Logger _log = Logger.getLogger(L2Multisell.class.getName());
	private FastMap<Integer, MultiSellListContainer>	entries			= new FastMap<Integer, MultiSellListContainer>();
	private static L2Multisell _instance = new L2Multisell();

	public static final String NODE_PRODUCTION = "production";
	public static final String NODE_INGRIDIENT = "ingredient";

	public MultiSellListContainer getList(int id)
	{
		return entries.get(id);
	}

	public L2Multisell()
	{
		parseData();
	}

	public void reload()
	{
		parseData();
	}

	public static L2Multisell getInstance()
	{
		return _instance;
	}

	private synchronized void parseData()
	{
		entries.clear();
		parse();
	}

	public static class MultiSellListContainer
	{
		private int _listId;
		private boolean _showall = true;
		private boolean keep_enchanted = false;
		private boolean is_dutyfree = false;
		private boolean nokey = false;
		GArray<MultiSellEntry> entries = new GArray<MultiSellEntry>();

		public void setListId(int listId)
		{
			_listId = listId;
		}

		public int getListId()
		{
			return _listId;
		}

		public void setShowAll(boolean bool)
		{
			_showall = bool;
		}

		public boolean isShowAll()
		{
			return _showall;
		}

		public void setNoTax(boolean bool)
		{
			is_dutyfree = bool;
		}

		public boolean isNoTax()
		{
			return is_dutyfree;
		}

		public void setNoKey(boolean bool)
		{
			nokey = bool;
		}

		public boolean isNoKey()
		{
			return nokey;
		}

		public void setKeepEnchant(boolean bool)
		{
			keep_enchanted = bool;
		}

		public boolean isKeepEnchant()
		{
			return keep_enchanted;
		}

		public void addEntry(MultiSellEntry e)
		{
			entries.add(e);
		}

		public GArray<MultiSellEntry> getEntries()
		{
			return entries;
		}

		public boolean isEmpty()
		{
			return entries.isEmpty();
		}
	}

	private void hashFiles(String dirname, GArray<File> hash)
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
		if(!dir.exists())
		{
			_log.config("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		File[] files = dir.listFiles();
		for(File f : files)
			if(f.getName().endsWith(".xml"))
				hash.add(f);
			else if(f.isDirectory() && !f.getName().equals(".svn"))
				hashFiles(dirname + "/" + f.getName(), hash);
	}

	public void addMultiSellListContainer(int id, MultiSellListContainer list)
	{
		if(entries.containsKey(id))
			_log.warning("MultiSell redefined: " + id);

		list.setListId(id);
		entries.put(id, list);
	}

	public MultiSellListContainer remove(String s)
	{
		return remove(new File(s));
	}

	public MultiSellListContainer remove(File f)
	{
		return remove(Integer.parseInt(f.getName().replaceAll(".xml", "")));
	}

	public MultiSellListContainer remove(int id)
	{
		return entries.remove(id);
	}

	public void parseFile(File f)
	{
		int id = 0;
		try
		{
			id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Error loading file " + f, e);
			return;
		}
		Document doc = null;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(f);
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Error loading file " + f, e);
			return;
		}
		try
		{
			addMultiSellListContainer(id, parseDocument(doc, id));
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Error in file " + f, e);
		}
	}

	private void parse()
	{
		new File("log/game/multiselldebug.txt").delete();
		GArray<File> files = new GArray<File>();
		hashFiles("multisell", files);
		for(File f : files)
			parseFile(f);
	}

	protected MultiSellListContainer parseDocument(Document doc, int id)
	{
		MultiSellListContainer list = new MultiSellListContainer();
		int entId = 1;

		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			if("list".equalsIgnoreCase(n.getNodeName()))
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					if("item".equalsIgnoreCase(d.getNodeName()))
					{
						MultiSellEntry e = parseEntry(d, id);
						if(e != null)
						{
							e.setEntryId(entId++);
							list.addEntry(e);
						}
					}
					else if("config".equalsIgnoreCase(d.getNodeName()))
					{
						list.setShowAll(XMLUtil.getAttributeBooleanValue(d, "showall", true));
						list.setNoTax(XMLUtil.getAttributeBooleanValue(d, "notax", false));
						list.setKeepEnchant(XMLUtil.getAttributeBooleanValue(d, "keepenchanted", false));
						list.setNoKey(XMLUtil.getAttributeBooleanValue(d, "nokey", false));
					}

		return list;
	}

	protected MultiSellEntry parseEntry(Node n, int MultiSellId)
	{
		MultiSellEntry entry = new MultiSellEntry();

		l2rt.util.Log.add(MultiSellId + " loading new entry", "multiselldebug");
		GArray<String> debuglist = new GArray<String>();

		for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			//l2rt.util.Log.add(MultiSellId + " processing node " + d.getNodeName(), "multiselldebug");
			debuglist.add(d.getNodeName() + " " + d.getAttributes() + " " + d.getNodeName().hashCode() + " " + d.getNodeName().length());
			if(NODE_INGRIDIENT.equalsIgnoreCase(d.getNodeName()))
			{
				int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
				long count = Long.parseLong(d.getAttributes().getNamedItem("count").getNodeValue());
				int enchant = 0, element = L2Item.ATTRIBUTE_NONE, elementValue = 0;
				if(d.getAttributes().getNamedItem("enchant") != null)
					enchant = Integer.parseInt(d.getAttributes().getNamedItem("enchant").getNodeValue());
				if(d.getAttributes().getNamedItem("element") != null)
					element = Integer.parseInt(d.getAttributes().getNamedItem("element").getNodeValue());
				if(d.getAttributes().getNamedItem("elementValue") != null)
					elementValue = Integer.parseInt(d.getAttributes().getNamedItem("elementValue").getNodeValue());

				l2rt.util.Log.add(MultiSellId + " loaded ingredient " + id + " count " + count, "multiselldebug");
				entry.addIngredient(new MultiSellIngredient(id, count, enchant, element, elementValue));
			}
			else if(NODE_PRODUCTION.equalsIgnoreCase(d.getNodeName()))
			{
				int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
				long count = Long.parseLong(d.getAttributes().getNamedItem("count").getNodeValue());
				int enchant = 0, element = L2Item.ATTRIBUTE_NONE, elementValue = 0;
				if(d.getAttributes().getNamedItem("enchant") != null)
					enchant = Integer.parseInt(d.getAttributes().getNamedItem("enchant").getNodeValue());
				if(d.getAttributes().getNamedItem("element") != null)
					element = Integer.parseInt(d.getAttributes().getNamedItem("element").getNodeValue());
				if(d.getAttributes().getNamedItem("elementValue") != null)
					elementValue = Integer.parseInt(d.getAttributes().getNamedItem("elementValue").getNodeValue());

				if(!Config.ALT_ALLOW_SHADOW_WEAPONS && id > 0)
				{
					L2Item item = ItemTemplates.getInstance().getTemplate(id);
					if(item != null && item.isShadowItem() && item.isWeapon() && !Config.ALT_ALLOW_SHADOW_WEAPONS)
						return null;
				}

				l2rt.util.Log.add(MultiSellId + " loaded product " + id + " count " + count, "multiselldebug");
				entry.addProduct(new MultiSellIngredient(id, count, enchant, element, elementValue));
			}
			else
				l2rt.util.Log.add(MultiSellId + " skipping node " + d.getNodeName(), "multiselldebug");
		}

		if(entry.getIngredients().isEmpty() || entry.getProduction().isEmpty())
		{
			l2rt.util.Log.add(MultiSellId + " wrong node", "multiselldebug");
			l2rt.util.Log.add(MultiSellId + " LIST: " + debuglist.toString(), "multiselldebug");
			for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				l2rt.util.Log.add(d.getNodeName() + " " + d.getAttributes() + " " + d.getNodeName().hashCode() + " " + d.getNodeName().length() + " IsProduction: " + NODE_PRODUCTION.equalsIgnoreCase(d.getNodeName()), "multiselldebug");
			return null;
		}

		if(entry.getIngredients().size() == 1 && entry.getProduction().size() == 1 && entry.getIngredients().get(0).getItemId() == 57)
		{
			L2Item item = ItemTemplates.getInstance().getTemplate(entry.getProduction().get(0).getItemId());
			if(item == null)
			{
				_log.warning("WARNING!!! MultiSell [" + MultiSellId + "] Production [" + entry.getProduction().get(0).getItemId() + "] is null");
				return null;
			}
			//if(MultiSellId < 70000 || MultiSellId > 70010) // Все кроме GM Shop
			//	if(item.getReferencePrice() > entry.getIngredients().get(0).getItemCount())
			//		_log.warning("WARNING!!! MultiSell [" + MultiSellId + "] Production '" + item.getName() + "' [" + entry.getProduction().get(0).getItemId() + "] price is lower than referenced | " + item.getReferencePrice() + " > " + entry.getIngredients().get(0).getItemCount());
			//return null;
		}

		return entry;
	}

	private static long[] parseItemIdAndCount(String s)
	{
		if(s == null || s.isEmpty())
			return null;
		String[] a = s.split(":");
		try
		{
			long id = Integer.parseInt(a[0]);
			long count = a.length > 1 ? Long.parseLong(a[1]) : 1;
			return new long[] { id, count };
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static MultiSellEntry parseEntryFromStr(String s)
	{
		if(s == null || s.isEmpty())
			return null;

		String[] a = s.split("->");
		if(a.length != 2)
			return null;

		long[] ingredient, production;
		if((ingredient = parseItemIdAndCount(a[0])) == null || (production = parseItemIdAndCount(a[1])) == null)
			return null;

		MultiSellEntry entry = new MultiSellEntry();
		entry.addIngredient(new MultiSellIngredient((int) ingredient[0], ingredient[1]));
		entry.addProduct(new MultiSellIngredient((int) production[0], production[1]));
		return entry;
	}

	public void SeparateAndSend(int listId, L2Player player, double taxRate)
	{
		for(int i : Config.ALT_DISABLED_MULTISELL)
			if(i == listId)
			{
				player.sendMessage(new CustomMessage("common.Disabled", player));
				return;
			}

		MultiSellListContainer list = generateMultiSell(listId, player, taxRate);
		if(list == null)
			return;
		MultiSellListContainer temp = new MultiSellListContainer();
		int page = 1;

		temp.setListId(list.getListId());

		// Запоминаем отсылаемый лист, чтобы не подменили
		player.setMultisell(list);

		for(MultiSellEntry e : list.getEntries())
		{
			if(temp.getEntries().size() == Config.MULTISELL_SIZE)
			{
				player.sendPacket(new MultiSellList(temp, page, 0));
				page++;
				temp = new MultiSellListContainer();
				temp.setListId(list.getListId());
			}
			temp.addEntry(e);
		}

		player.sendPacket(new MultiSellList(temp, page, 1));
	}

	private MultiSellListContainer generateMultiSell(int listId, L2Player player, double taxRate)
	{
		MultiSellListContainer list = new MultiSellListContainer();
		list._listId = listId;

		// Hardcoded  - обмен вещей на равноценные
		GArray<L2ItemInstance> _items;
		if(listId == 9999)
		{
			list.setShowAll(false);
			list.setKeepEnchant(true);
			list.setNoTax(true);
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance itm : inv.getItems())
				if(itm.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& !itm.getItem().isSa() // SA менять нельзя
						&& !itm.getItem().isRare() // Rare менять нельзя
						&& !itm.getItem().isCommonItem() // Common менять нельзя
						&& !itm.getItem().isPvP() // PvP менять нельзя
						&& itm.canBeTraded(player) // универсальная проверка
						&& !itm.isStackable() //
						&& itm.getItem().getType2() == L2Item.TYPE2_WEAPON //
						&& itm.getItem().getCrystalType() != Grade.NONE //
						&& itm.getReferencePrice() <= Config.ALT_MAMMON_EXCHANGE //
						&& itm.getItem().getCrystalCount() > 0 //
						&& itm.getItem().isTradeable() //
						&& (itm.getCustomFlags() & L2ItemInstance.FLAG_NO_TRADE) != L2ItemInstance.FLAG_NO_TRADE //
				)
					_items.add(itm);

			for(final L2ItemInstance itm : _items)
				for(L2Weapon i : ItemTemplates.getInstance().getAllWeapons())
					if(i.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& !i.isSa() // На SA менять нельзя
							&& !i.isRare() // На Rare менять нельзя
							&& !i.isCommonItem() // На Common менять нельзя
							&& !i.isPvP() // На PvP менять нельзя
							&& !i.isShadowItem() // На Shadow менять нельзя
							&& !i.isHeroWeapon() // На HeroWeapon менять нельзя!!!
							&& !i.isAllGodWeapon() //На благословенное тоже нельзя!!!!
							&& !i.isConstrainedSa()
							&& !i.isConstrained()
							&& i.isTradeable() // можно использовать чтобы запретить менять специальные вещи
							&& i.getItemId() != itm.getItemId() //
							&& i.getType2() == L2Item.TYPE2_WEAPON //
							&& itm.getItem().getCrystalType() != Grade.NONE //
							&& i.getItemType() == WeaponType.DUAL == (itm.getItem().getItemType() == WeaponType.DUAL) //
							&& itm.getItem().getCrystalType() == i.getCrystalType() //
							&& itm.getItem().getCrystalCount() == i.getCrystalCount() //
					)
					{
						final int entry = new int[] { itm.getItemId(), i.getItemId(), itm.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, i.getItemId(), 1, itm.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(itm.getItemId(), 1, itm.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002,  10, 0));
						list.entries.add(possibleEntry);
					}
		}

		// Hardcoded  - обмен вещей с доплатой за AA
		else if(listId == 9998)
		{
			list.setShowAll(false);
			list.setKeepEnchant(false);
			list.setNoTax(true);
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance itm : inv.getItems())
				if(itm.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& !itm.getItem().isSa() // SA менять нельзя
						&& !itm.getItem().isRare() // Rare менять нельзя
						&& !itm.getItem().isCommonItem() // Common менять нельзя
						&& !itm.getItem().isPvP() // PvP менять нельзя
						&& !itm.getItem().isShadowItem() // Shadow менять нельзя
						&& !itm.isTemporalItem() // Temporal менять нельзя
						&& !itm.isStackable() //
						&& itm.getItem().getType2() == L2Item.TYPE2_WEAPON //
						&& itm.getItem().getCrystalType() != Grade.NONE //
						&& itm.getReferencePrice() <= Config.ALT_MAMMON_UPGRADE //
						&& itm.getItem().getCrystalCount() > 0 //
						&& !itm.isEquipped() //
						&& itm.getItem().isTradeable() //
						&& (itm.getCustomFlags() & L2ItemInstance.FLAG_NO_TRADE) != L2ItemInstance.FLAG_NO_TRADE //
				)
					_items.add(itm);

			for(final L2ItemInstance itemtosell : _items)
				for(final L2Weapon itemtobuy : ItemTemplates.getInstance().getAllWeapons())
					if(itemtobuy.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& !itemtobuy.isSa() // На SA менять нельзя
							&& !itemtobuy.isRare() // На Rare менять нельзя
							&& !itemtobuy.isCommonItem() // На Common менять нельзя
							&& !itemtobuy.isPvP() // На PvP менять нельзя
							&& !itemtobuy.isShadowItem() // На Shadow менять нельзя
							&& !itemtobuy.isHeroWeapon() // На HeroWeapon менять нельзя!!!
							&& !itemtobuy.isHeroWeapon() // На HeroWeapon менять нельзя!!!
							&& !itemtobuy.isAllGodWeapon() //На благословенное тоже нельзя!!!!
							&& !itemtobuy.isConstrainedSa()
							&& !itemtobuy.isConstrained()
							&& itemtobuy.isTradeable() //
							&& itemtobuy.getType2() == L2Item.TYPE2_WEAPON //
							&& itemtobuy.getItemType() == WeaponType.DUAL == (itemtosell.getItem().getItemType() == WeaponType.DUAL) //
							&& itemtobuy.getCrystalType().ordinal() >= itemtosell.getItem().getCrystalType().ordinal() //
							&& itemtobuy.getReferencePrice() <= Config.ALT_MAMMON_UPGRADE //
							&& itemtosell.getItem().getReferencePrice() < itemtobuy.getReferencePrice() //
							&& itemtosell.getReferencePrice() * 1.7 > itemtobuy.getReferencePrice() //
					)
					{
						final int entry = new int[] { itemtosell.getItemId(), itemtobuy.getItemId(), itemtosell.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, itemtobuy.getItemId(), 1, 0);
						possibleEntry.addIngredient(new MultiSellIngredient(itemtosell.getItemId(), 1, itemtosell.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 5575, (int) ((itemtobuy.getReferencePrice() - itemtosell.getReferencePrice()) * 1.2), 0));
						list.entries.add(possibleEntry);
					}
		}

		// Hardcoded  - обмен вещей на кристаллы
		else if(listId == 9997)
		{
			list.setShowAll(false);
			list.setKeepEnchant(true);
			list.setNoTax(false);
			final Inventory inv = player.getInventory();
			for(final L2ItemInstance itm : inv.getItems())
				if(!itm.isStackable() && itm.getItem().isCrystallizable() && itm.getItem().getCrystalType() != Grade.NONE && itm.getItem().getCrystalCount() > 0 && !itm.isShadowItem() && !itm.isTemporalItem() && !itm.isEquipped() && (itm.getCustomFlags() & L2ItemInstance.FLAG_NO_CRYSTALLIZE) != L2ItemInstance.FLAG_NO_CRYSTALLIZE)
				{
					final L2Item crystal = ItemTemplates.getInstance().getTemplate(itm.getItem().getCrystalType().cry);
					final int entry = new int[] { itm.getItemId(), itm.getEnchantLevel() }.hashCode();
					MultiSellEntry possibleEntry = new MultiSellEntry(entry, crystal.getItemId(), itm.getItem().getCrystalCount(), itm.getEnchantLevel());
					possibleEntry.addIngredient(new MultiSellIngredient(itm.getItemId(), 1, itm.getEnchantLevel()));
					possibleEntry.addIngredient(new MultiSellIngredient((short) 57, (int) (itm.getItem().getCrystalCount() * crystal.getReferencePrice() * 0.05), 0));
					list.entries.add(possibleEntry);
				}
		}
		
		
		//Обмен S оружия на династию.
		else if(listId == 20002)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(ingridient.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& !ingridient.getItem().isRare() // Rare менять нельзя
						&& !ingridient.getItem().isCommonItem() // Common менять нельзя
						&& !ingridient.getItem().isPvP() // PvP менять нельзя
						&& ingridient.canBeTraded(player) // универсальная проверка
						&& !ingridient.isStackable() //
						&& ingridient.getItem().getType2() == L2Item.TYPE2_WEAPON //
						&& ingridient.getItem().isS() // Обмениваем Ы оружие.
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Weapon production : ItemTemplates.getInstance().getAllWeapons())
					if(production.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& !production.isSa() // На SA менять нельзя
							&& !production.isRare() // На Rare менять нельзя
							&& !production.isCommonItem() // На Common менять нельзя
							&& !production.isPvP() // На PvP менять нельзя
							&& !production.isShadowItem() // На Shadow менять нельзя
							&& !production.isUnknown() // На неопознанное менять нельзя
							&& !production.isConstrained() // На скованное менять нельзя
							&& !production.isBlessed() // На благословенное менять нельзя
							&& production.isTradeable() // можно использовать чтобы запретить менять специальные вещи
							&& production.getItemId() != ingridient.getItemId() //
							&& production.getType2() == L2Item.TYPE2_WEAPON // Веапон на веапон
							&& production.isDynasty() // Обмен на династию.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 30, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 30, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 30, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 30, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//Обмен династии на венеру.
		else if(listId == 20003)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(ingridient.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& !ingridient.getItem().isRare() // Rare менять нельзя
						&& !ingridient.getItem().isCommonItem() // Common менять нельзя
						&& !ingridient.getItem().isPvP() // PvP менять нельзя
						&& ingridient.canBeTraded(player) // универсальная проверка
						&& !ingridient.isStackable() //
						&& ingridient.getItem().getType2() == L2Item.TYPE2_WEAPON //
						&& ingridient.getItem().isDynasty() // Обмениваем венеру.
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Weapon production : ItemTemplates.getInstance().getAllWeapons())
					if(production.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& !production.isSa() // На SA менять нельзя
							&& !production.isRare() // На Rare менять нельзя
							&& !production.isCommonItem() // На Common менять нельзя
							&& !production.isPvP() // На PvP менять нельзя
							&& !production.isShadowItem() // На Shadow менять нельзя
							&& !production.isUnknown() // На неопознанное менять нельзя
							&& !production.isConstrained() // На скованное менять нельзя
							&& !production.isBlessed() // На благословенное менять нельзя
							&& production.isTradeable() // можно использовать чтобы запретить менять специальные вещи
							&& production.getItemId() != ingridient.getItemId() //
							&& production.getType2() == L2Item.TYPE2_WEAPON // Веапон на веапон
							&& production.isVesper() // Обмен на династию.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 70, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 70, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 70, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 70, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//Обмен веспера на фрею.
		else if(listId == 20004)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(ingridient.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& !ingridient.getItem().isRare() // Rare менять нельзя
						&& !ingridient.getItem().isCommonItem() // Common менять нельзя
						&& !ingridient.getItem().isPvP() // PvP менять нельзя
						&& ingridient.canBeTraded(player) // универсальная проверка
						&& !ingridient.isStackable() //
						&& ingridient.getItem().getType2() == L2Item.TYPE2_WEAPON //
						&& ingridient.getItem().isVesper() // Обмениваем венеру.
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Weapon production : ItemTemplates.getInstance().getAllWeapons())
					if(production.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& !production.isSa() // На SA менять нельзя
							&& !production.isRare() // На Rare менять нельзя
							&& !production.isCommonItem() // На Common менять нельзя
							&& !production.isPvP() // На PvP менять нельзя
							&& !production.isShadowItem() // На Shadow менять нельзя
							&& !production.isUnknown() // На неопознанное менять нельзя
							&& !production.isConstrained() // На скованное менять нельзя
							&& !production.isBlessed() // На благословенное менять нельзя
							&& production.isTradeable() // можно использовать чтобы запретить менять специальные вещи
							&& production.getItemId() != ingridient.getItemId() //
							&& production.getType2() == L2Item.TYPE2_WEAPON // Веапон на веапон
							&& production.isFreya() // Обмен на фрею.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002,  60, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003,  300, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004,  300, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005,  300, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006,  300, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//Обмен фреи на реквием.
		else if(listId == 20005)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(ingridient.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& !ingridient.getItem().isRare() // Rare менять нельзя
						&& !ingridient.getItem().isCommonItem() // Common менять нельзя
						&& !ingridient.getItem().isPvP() // PvP менять нельзя
						&& ingridient.canBeTraded(player) // универсальная проверка
						&& !ingridient.isStackable() //
						&& ingridient.getItem().getType2() == L2Item.TYPE2_WEAPON //
						&& ingridient.getItem().isFreya() // Обмениваем фрею.
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Weapon production : ItemTemplates.getInstance().getAllWeapons())
					if(production.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& !production.isSa() // На SA менять нельзя
							&& !production.isRare() // На Rare менять нельзя
							&& !production.isCommonItem() // На Common менять нельзя
							&& !production.isPvP() // На PvP менять нельзя
							&& !production.isShadowItem() // На Shadow менять нельзя
							&& !production.isUnknown() // На неопознанное менять нельзя
							&& !production.isConstrained() // На скованное менять нельзя
							&& !production.isBlessed() // На благословенное менять нельзя
							&& production.isTradeable() // можно использовать чтобы запретить менять специальные вещи
							&& production.getItemId() != ingridient.getItemId() //
							&& production.getType2() == L2Item.TYPE2_WEAPON // Веапон на веапон
							&& production.isRequiem() // Обмен на фрею.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 120, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 600, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 600, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 600, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 600, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//Обмен реквием на апокалипсис.
		else if(listId == 20006)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(ingridient.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& !ingridient.getItem().isRare() // Rare менять нельзя
						&& !ingridient.getItem().isCommonItem() // Common менять нельзя
						&& !ingridient.getItem().isPvP() // PvP менять нельзя
						&& ingridient.canBeTraded(player) // универсальная проверка
						&& !ingridient.isStackable() //
						&& ingridient.getItem().getType2() == L2Item.TYPE2_WEAPON //
						&& ingridient.getItem().isRequiem() // Обмениваем Requiem.
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Weapon production : ItemTemplates.getInstance().getAllWeapons())
					if(production.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& !production.isSa() // На SA менять нельзя
							&& !production.isRare() // На Rare менять нельзя
							&& !production.isCommonItem() // На Common менять нельзя
							&& !production.isPvP() // На PvP менять нельзя
							&& !production.isShadowItem() // На Shadow менять нельзя
							&& !production.isUnknown() // На неопознанное менять нельзя
							&& !production.isConstrained() // На скованное менять нельзя
							&& !production.isBlessed() // На благословенное менять нельзя
							&& production.isTradeable() // можно использовать чтобы запретить менять специальные вещи
							&& production.getItemId() != ingridient.getItemId() //
							&& production.getType2() == L2Item.TYPE2_WEAPON // Веапон на веапон
							&& production.isApocalipsis() // Обмен на Apocalipsis.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 180, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 1200, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 1200, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 1200, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 1200, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//Обмен апокалипсис на фантазм.
		else if(listId == 20007)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(ingridient.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& !ingridient.getItem().isRare() // Rare менять нельзя
						&& !ingridient.getItem().isCommonItem() // Common менять нельзя
						&& !ingridient.getItem().isPvP() // PvP менять нельзя
						&& ingridient.canBeTraded(player) // универсальная проверка
						&& !ingridient.isStackable() //
						&& ingridient.getItem().getType2() == L2Item.TYPE2_WEAPON //
						&& ingridient.getItem().isApocalipsis() // Обмениваем Apocalipsis.
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Weapon production : ItemTemplates.getInstance().getAllWeapons())
					if(production.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& !production.isSa() // На SA менять нельзя
							&& !production.isRare() // На Rare менять нельзя
							&& !production.isCommonItem() // На Common менять нельзя
							&& !production.isPvP() // На PvP менять нельзя
							&& !production.isShadowItem() // На Shadow менять нельзя
							&& !production.isUnknown() // На неопознанное менять нельзя
							&& !production.isConstrained() // На скованное менять нельзя
							&& !production.isBlessed() // На благословенное менять нельзя
							&& production.isTradeable() // можно использовать чтобы запретить менять специальные вещи
							&& production.getItemId() != ingridient.getItemId() //
							&& production.getType2() == L2Item.TYPE2_WEAPON // Веапон на веапон
							&& production.isFantazm() // Обмен на Fantazm.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 300, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 5000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 5000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 5000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 5000, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//Обмен фантазм на хелиос.
		else if(listId == 20008)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(ingridient.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& !ingridient.getItem().isRare() // Rare менять нельзя
						&& !ingridient.getItem().isCommonItem() // Common менять нельзя
						&& !ingridient.getItem().isPvP() // PvP менять нельзя
						&& ingridient.canBeTraded(player) // универсальная проверка
						&& !ingridient.isStackable() //
						&& ingridient.getItem().getType2() == L2Item.TYPE2_WEAPON //
						&& ingridient.getItem().isFantazm() // Обмениваем Fantazm.
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Weapon production : ItemTemplates.getInstance().getAllWeapons())
					if(production.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& !production.isSa() // На SA менять нельзя
							&& !production.isRare() // На Rare менять нельзя
							&& !production.isCommonItem() // На Common менять нельзя
							&& !production.isPvP() // На PvP менять нельзя
							&& !production.isShadowItem() // На Shadow менять нельзя
							&& !production.isUnknown() // На неопознанное менять нельзя
							&& !production.isConstrained() // На скованное менять нельзя
							&& !production.isBlessed() // На благословенное менять нельзя
							&& production.isTradeable() // можно использовать чтобы запретить менять специальные вещи
							&& production.getItemId() != ingridient.getItemId() //
							&& production.getType2() == L2Item.TYPE2_WEAPON // Веапон на веапон
							&& production.isHelios() // Обмен на Helios.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 500, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 10000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 10000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 10000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 10000, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		
		
		//Обмен Ы брони на династию.
		else if(listId == 20102)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(ingridient.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& ingridient.canBeTraded(player) // универсальная проверка
						&& !ingridient.isStackable() //
						&& ingridient.getItem().isArmorS() // Обмениваем S grade
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Armor production : ItemTemplates.getInstance().getAllArmors())
					if(production.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& production.getItemId() != ingridient.getItemId() //
							&& production.isArmorDynasty() // Обмен на Dynasty.
							&& ingridient.getItem().isFullArmor() //Если полная броня.
							&& production.isChestLegs() //Берем верх и низ..
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 6, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 6, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 6, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 6, 0));
						list.entries.add(possibleEntry);
					}
					else if(production.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& production.getItemId() != ingridient.getItemId() //
							&& production.isArmorDynasty() // Обмен на Dynasty.
							&& production.getBodyPart() == ingridient.getBodyPart() // Обмен на ту-же часть.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 15, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 15, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 15, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 15, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//Обмен династии на венеру.
		else if(listId == 20103)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(ingridient.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& ingridient.canBeTraded(player) // универсальная проверка
						&& !ingridient.isStackable() //
						&& ingridient.getItem().isArmorDynasty() // Обмениваем династию
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Armor production : ItemTemplates.getInstance().getAllArmors())
					if(production.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& production.getItemId() != ingridient.getItemId() //
							&& production.getBodyPart() == ingridient.getBodyPart() // Обмен на ту-же часть.
							&& production.isArmorVesper() // Обмен на Венеру.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 5, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 25, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 25, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 25, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 25, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//Обмен венеры на верпес.
		else if(listId == 20104)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(ingridient.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& ingridient.canBeTraded(player) // универсальная проверка
						&& !ingridient.isStackable() //
						&& ingridient.getItem().isArmorVesper() // Обмениваем венеру
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Armor production : ItemTemplates.getInstance().getAllArmors())
					if(production.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& production.getItemId() != ingridient.getItemId() //
							&& production.getBodyPart() == ingridient.getBodyPart() // Обмен на ту-же часть.
							&& production.isArmorVerpes() // Обмен на Верпес.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 30, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 125, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 125, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 125, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 125, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//верпес на элегию.
		else if(listId == 20105)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(ingridient.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& ingridient.canBeTraded(player) // универсальная проверка
						&& !ingridient.isStackable() //
						&& ingridient.getItem().isArmorVerpes() // Обмениваем верпес
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Armor production : ItemTemplates.getInstance().getAllArmors())
					if(production.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& production.getItemId() != ingridient.getItemId() //
							&& production.getBodyPart() == ingridient.getBodyPart() // Обмен на ту-же часть.
							&& production.isArmorElegia() // Обмен на элегию.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 45, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 250, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 250, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 250, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 250, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//элегию на бессмертие.
		else if(listId == 20106)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(
						ingridient.canBeTraded(player) // универсальная проверка
						&& ingridient.getItem().isArmorElegia() // Обмениваем isArmorElegia()
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Armor production : ItemTemplates.getInstance().getAllArmors())
					if(
							production.getBodyPart() == ingridient.getBodyPart() // Обмен на ту-же часть.
							&& production.isArmorInvul() // Обмен на isArmorInvul.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 60, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 1000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 1000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 1000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 1000, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//бессмертие на ад.
		else if(listId == 20107)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(
						ingridient.canBeTraded(player) // универсальная проверка
						&& ingridient.getItem().isArmorInvul() // Обмениваем isArmorInvul
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Armor production : ItemTemplates.getInstance().getAllArmors())
					if(		production.getBodyPart() == ingridient.getBodyPart() // Обмен на ту-же часть.
							&& production.isArmorAd() // Обмен на isArmorAd().
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 100, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 3000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 3000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 3000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 3000, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//ад на кадейру.
		else if(listId == 20108)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(
						ingridient.canBeTraded(player) // универсальная проверка
						&& ingridient.getItem().isArmorAd() // Обмениваем isArmorAd
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Armor production : ItemTemplates.getInstance().getAllArmors())
					if(
							production.getBodyPart() == ingridient.getBodyPart() // Обмен на ту-же часть.
							&& production.isArmorKadeyra() // Обмен на isArmorKadeyra.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 150, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 9000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 9000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 9000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 9000, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		//кадейру на йдиос.
		else if(listId == 20109)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
				if(
						ingridient.canBeTraded(player) // универсальная проверка
						&& ingridient.getItem().isArmorKadeyra() // Обмениваем isArmorKadeyra
				)
					_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Armor production : ItemTemplates.getInstance().getAllArmors())
					if(
							production.getBodyPart() == ingridient.getBodyPart() // Обмен на ту-же часть.
							&& production.isArmorAidios() // Обмен на Венеру.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 200, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 20000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 20000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 20000, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 20000, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		else if(listId == 100500)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
					if(
							ingridient.canBeTraded(player) // Берем все предметы, которые можно передавать.
							&& ingridient.getItem().isSa() // Берем все предметы с СА из инвентаря.
					)
						_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Weapon production : ItemTemplates.getInstance().getAllWeapons())
					if(!production.isRare() // Отсеиваем Rare.
							&& !production.isCommonItem() // Отсеиваем Common.
							&& !production.isSa() // Отсеиваем с СА.
							&& !production.isPvP() //Отсеиваем пвп.
							&& production.getCrystalCount() == ingridient.getItem().getCrystalCount() //Отсеиваем по кристаллам.
							&& production.getCrystalType() == ingridient.getItem().getCrystalType() //Отсеиваем по грейду.
							&& production.getWeight() == ingridient.getItem().getWeight() //Отсеиваем по весу.
							&& production.getItemType() == ingridient.getItem().getItemType() //Отсеиваем по типу.
							&& production.isTradeable() //Отсеиваем всё, то что не передается.
							&& production.getAttachedSkills() == null //Отсеиваем сука всё, что без скиллов.
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 1, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 2, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 2, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 2, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 2, 0));
						list.entries.add(possibleEntry);
					}
		}
		//Доработать!!!!!!!
		else if(listId == 100505)
		{
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance ingridient : inv.getItems())
					if(
							ingridient.canBeTraded(player) // Берем все предметы, которые можно передавать.
							&& !ingridient.getItem().isSa() // Берем все предметы без СА из инвентаря.
							&& ingridient.getItem().getType2() == L2Item.TYPE2_WEAPON //Берем всё оружие
					)
						_items.add(ingridient);

			for(final L2ItemInstance ingridient : _items)
				for(L2Weapon production : ItemTemplates.getInstance().getAllWeapons())
					if(!production.isRare() // Отсеиваем Rare.
							&& !production.isCommonItem() // Отсеиваем Common.
							&& !production.isPvP() //Отсеиваем пвп.
							&& production.isSa() // Отсеиваем без са
							&& !production.isAllGodWeapon() //Отсеиваем Благословенные пухи
							&& !production.isConstrained() //Скованные
							&& !production.isConstrainedSa() //Скованные
							&& !production.isArtefact() //Артефакты
							&& production.getWeight() == ingridient.getItem().getWeight() //Отсеиваем по весу.
							&& production.getCrystalCount() == ingridient.getItem().getCrystalCount() //Отсеиваем по кристаллам.
							&& production.getItemType() == ingridient.getItem().getItemType() //Отсеиваем по типу.
							&& production.getCrystalType() == ingridient.getItem().getCrystalType() //Отсеиваем по грейду.
							&& !production.isDualSa() //Отсеиваем dual sa
					)
					{
						final int entry = new int[] { ingridient.getItemId(), production.getItemId(), ingridient.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, production.getItemId(), 1, ingridient.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(ingridient.getItemId(), 1, ingridient.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21002, 1, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21003, 2, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21004, 2, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21005, 2, 0));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 21006, 2, 0));
						list.entries.add(possibleEntry);
					}
		}
		
		// Все мультиселлы из датапака
		else
		{
			MultiSellListContainer container = L2Multisell.getInstance().getList(listId);
			if(container == null)
			{
				_log.warning("Not found myltisell " + listId);
				return null;
			}
			else if(container.isEmpty())
			{
				player.sendMessage(new CustomMessage("common.Disabled", player));
				return null;
			}

			boolean enchant = container.isKeepEnchant();
			boolean notax = container.isNoTax();
			boolean showall = container.isShowAll();
			boolean nokey = container.isNoKey();

			list.setShowAll(showall);
			list.setKeepEnchant(enchant);
			list.setNoTax(notax);
			list.setNoKey(nokey);

			final Inventory inv = player.getInventory();
			for(MultiSellEntry origEntry : container.getEntries())
			{
				MultiSellEntry ent = origEntry.clone();

				// Обработка налога, если лист не безналоговый
				// Адены добавляются в лист если отсутствуют или прибавляются к существующим
				GArray<MultiSellIngredient> ingridients;
				if(!notax && taxRate > 0.)
				{
					double tax = 0, adena = 0;
					ingridients = new GArray<MultiSellIngredient>(ent.getIngredients().size() + 1);
					for(MultiSellIngredient i : ent.getIngredients())
					{
						if(i.getItemId() == 57)
						{
							adena += i.getItemCount();
							tax += i.getItemCount() * (taxRate);
							continue;
						}
						ingridients.add(i);
						if(i.getItemId() == L2Item.ITEM_ID_CLAN_REPUTATION_SCORE)
							// hardcoded. Налог на клановую репутацию. Формула проверена на с6 и соответсвует на 100%.
							//TODO: Проверить на корейском(?) оффе налог на банг поинты и fame
							tax += i.getItemCount() / 120 * 1000 * taxRate * 100;
						if(i.getItemId() < 1)
							continue;

						final L2Item item = ItemTemplates.getInstance().getTemplate(i.getItemId());
						if(item == null)
							System.out.println("Not found template for itemId: " + i.getItemId());
						else if(item.isStackable())
							tax += item.getReferencePrice() * i.getItemCount() * taxRate;
					}

					adena = Math.round(adena + tax);
					if(adena >= 1)
						ingridients.add(new MultiSellIngredient(57, (long) adena));

					tax = Math.round(tax);
					if(tax >= 1)
						ent.setTax((long) tax);

					ent.getIngredients().clear();
					ent.getIngredients().addAll(ingridients);
				}
				else
					ingridients = ent.getIngredients();

				// Если стоит флаг "показывать все" не проверять наличие ингридиентов
				if(showall)
					list.entries.add(ent);
				else
				{
					GArray<Integer> _itm = new GArray<Integer>();
					// Проверка наличия у игрока ингридиентов
					for(MultiSellIngredient i : ingridients)
					{
						L2Item template = i.getItemId() <= 0 ? null : ItemTemplates.getInstance().getTemplate(i.getItemId());
						if(i.getItemId() <= 0 || template.getType2() <= L2Item.TYPE2_ACCESSORY || template.getType2() >= (nokey ? L2Item.TYPE2_OTHER : L2Item.TYPE2_PET_WOLF)) // Экипировка
						{
							if(i.getItemId() == 12374) // Mammon's Varnish Enhancer
								continue;

							//TODO: а мы должны тут сверять count?
							if(i.getItemId() == L2Item.ITEM_ID_CLAN_REPUTATION_SCORE)
							{
								if(!_itm.contains(i.getItemId()) && player.getClan() != null && player.getClan().getReputationScore() >= i.getItemCount())
									_itm.add(i.getItemId());
								continue;
							}
							else if(i.getItemId() == L2Item.ITEM_ID_PC_BANG_POINTS)
							{
								if(!_itm.contains(i.getItemId()) && player.getPcBangPoints() >= i.getItemCount())
									_itm.add(i.getItemId());
								continue;
							}
							else if(i.getItemId() == L2Item.ITEM_ID_FAME)
							{
								if(!_itm.contains(i.getItemId()) && player.getFame() >= i.getItemCount())
									_itm.add(i.getItemId());
								continue;
							}

							for(final L2ItemInstance item : inv.getItems())
								if(item.getItemId() == i.getItemId() && !item.isEquipped() && (item.getCustomFlags() & L2ItemInstance.FLAG_NO_TRADE) != L2ItemInstance.FLAG_NO_TRADE)
								{
									if(_itm.contains(enchant ? i.getItemId() + i.getItemEnchant() * 100000L : i.getItemId())) // Не проверять одинаковые вещи
										continue;

									if(item.getEnchantLevel() < i.getItemEnchant()) // Некоторые мультиселлы требуют заточки
										continue;

									if(item.isStackable() && item.getCount() < i.getItemCount())
										break;

									_itm.add(enchant ? i.getItemId() + i.getItemEnchant() * 100000 : i.getItemId());
									MultiSellEntry possibleEntry = new MultiSellEntry(enchant ? ent.getEntryId() + item.getEnchantLevel() * 100000 : ent.getEntryId());

									for(MultiSellIngredient p : ent.getProduction())
									{
										p.setItemEnchant(item.getEnchantLevel());
										possibleEntry.addProduct(p);
									}

									for(MultiSellIngredient ig : ingridients)
									{
										if(template != null && template.getType2() <= L2Item.TYPE2_ACCESSORY)
											ig.setItemEnchant(item.getEnchantLevel());

										possibleEntry.addIngredient(ig);
									}
									list.entries.add(possibleEntry);
									break;
								}
						}
					}
				}
			}
		}

		return list;
	}

	public static void unload()
	{
		if(_instance != null)
		{
			_instance.entries.clear();
			_instance = null;
		}
	}
}