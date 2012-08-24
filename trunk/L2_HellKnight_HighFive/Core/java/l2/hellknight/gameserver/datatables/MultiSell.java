/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2.hellknight.gameserver.datatables;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.multisell.Entry;
import l2.hellknight.gameserver.model.multisell.Ingredient;
import l2.hellknight.gameserver.model.multisell.ListContainer;
import l2.hellknight.gameserver.model.multisell.PreparedListContainer;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2.hellknight.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2.hellknight.gameserver.network.serverpackets.MultiSellList;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.network.serverpackets.UserInfo;
import l2.hellknight.util.file.filter.XMLFilter;

public class MultiSell
{
	public static final int PAGE_SIZE = 40;
	
	public static final int PC_BANG_POINTS = -100;
	public static final int CLAN_REPUTATION = -200;
	public static final int FAME = -300;
	
	private static final Logger _log = Logger.getLogger(MultiSell.class.getName());
	
	private final TIntObjectHashMap<ListContainer> _entries;
	
	public static MultiSell getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected MultiSell()
	{
		_entries = new TIntObjectHashMap<>();
		load();
	}
	
	public final void reload()
	{
		_entries.clear();
		load();
	}
	
	/**
	 * This will generate the multisell list for the items.  There exist various
	 * parameters in multisells that affect the way they will appear:
	 * 1) inventory only:
	 * 		* if true, only show items of the multisell for which the
	 * 		  "primary" ingredients are already in the player's inventory.  By "primary"
	 * 		  ingredients we mean weapon and armor.
	 * 		* if false, show the entire list.
	 * 2) maintain enchantment: presumably, only lists with "inventory only" set to true
	 * 		should sometimes have this as true.  This makes no sense otherwise...
	 * 		* If true, then the product will match the enchantment level of the ingredient.
	 * 		  if the player has multiple items that match the ingredient list but the enchantment
	 * 		  levels differ, then the entries need to be duplicated to show the products and
	 * 		  ingredients for each enchantment level.
	 * 		  For example: If the player has a crystal staff +1 and a crystal staff +3 and goes
	 * 		  to exchange it at the mammon, the list should have all exchange possibilities for
	 * 		  the +1 staff, followed by all possibilities for the +3 staff.
	 * 		* If false, then any level ingredient will be considered equal and product will always
	 * 		  be at +0
	 * 3) apply taxes: Uses the "taxIngredient" entry in order to add a certain amount of adena to the ingredients
	 * 4) additional product and ingredient multipliers
	 * @param listId 
	 * @param player 
	 * @param npc 
	 * @param inventoryOnly 
	 * @param productMultiplier 
	 * @param ingredientMultiplier 
	 */
	public final void separateAndSend(int listId, L2PcInstance player, L2Npc npc, boolean inventoryOnly, double productMultiplier, double ingredientMultiplier)
	{
		ListContainer template = _entries.get(listId);
		if (template == null)
		{
			_log.warning("[MultiSell] can't find list id: " + listId + " requested by player: " + player.getName() + ", npcId:" + (npc != null ? npc.getNpcId() : 0));
			return;
		}
		
		final PreparedListContainer list = new PreparedListContainer(template, inventoryOnly, player, npc);
		
		// Pass through this only when multipliers are different from 1
		if (productMultiplier != 1 || ingredientMultiplier != 1)
		{
			for(Entry entry : list.getEntries())
			{
				for(Ingredient product : entry.getProducts())
				{
					//Math.max used here to avoid dropping count to 0
					product.setItemCount((long) Math.max(product.getItemCount() * productMultiplier,1));
				}
				for(Ingredient ingredient : entry.getIngredients())
				{
					//Math.max used here to avoid dropping count to 0
					ingredient.setItemCount((long) Math.max(ingredient.getItemCount() * ingredientMultiplier, 1));
				}
			}
		}
		int index = 0;
		do
		{
			// send list at least once even if size = 0
			player.sendPacket(new MultiSellList(list, index));
			index += PAGE_SIZE;
		}
		while (index < list.getEntries().size());
		
		player.setMultiSell(list);
	}
	public final void separateAndSend(int listId, L2PcInstance player, L2Npc npc, boolean inventoryOnly)
	{
		separateAndSend(listId,  player, npc, inventoryOnly, 1, 1);
	}
	
	public static final boolean checkSpecialIngredient(int id, long amount, L2PcInstance player)
	{
		switch (id)
		{
			case PC_BANG_POINTS:
				if (player.getPcBangPoints() < amount)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SHORT_OF_ACCUMULATED_POINTS));
					break;
				}
				return true;
			case CLAN_REPUTATION:
				if (player.getClan() == null)
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
					break;
				}
				if (!player.isClanLeader())
				{
					player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
					break;
				}
				if (player.getClan().getReputationScore() < amount)
				{
					player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
					break;
				}
				return true;
			case FAME:
				if (player.getFame() < amount)
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_FAME_POINTS);
					break;
				}
				return true;
			default:
				break;
		}
		return false;
	}
	
	public static final boolean getSpecialIngredient(int id, long amount, L2PcInstance player)
	{
		switch (id)
		{
			case PC_BANG_POINTS: //PcBang points
				final int cost = player.getPcBangPoints() - (int)(amount);
				player.setPcBangPoints(cost);
				SystemMessage smsgpc = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
				smsgpc.addNumber((int)amount);
				player.sendPacket(smsgpc);
				player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), (int)amount, false, false, 1));
				return true;
			case CLAN_REPUTATION:
				player.getClan().takeReputationScore((int) amount, true);
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
				smsg.addItemNumber(amount);
				player.sendPacket(smsg);
				return true;
			case FAME:
				player.setFame(player.getFame() - (int) amount);
				player.sendPacket(new UserInfo(player));
				player.sendPacket(new ExBrExtraUserInfo(player));
				return true;
			default:
				break;
		}
		return false;
	}
	
	public static final void addSpecialProduct(int id, long amount, L2PcInstance player)
	{
		switch (id)
		{
			case CLAN_REPUTATION:
				player.getClan().addReputationScore((int) amount, true);
				break;
			case FAME:
				player.setFame((int) (player.getFame() + amount));
				player.sendPacket(new UserInfo(player));
				player.sendPacket(new ExBrExtraUserInfo(player));
				break;
			default:
				break;
		}
	}
	
	private final void load()
	{
		Document doc = null;
		int id = 0;
		List<File> files = new FastList<>();
		hashFiles("data/multisell", files);
		if (Config.CUSTOM_MULTISELL_LOAD)
			hashFiles("data/multisell/custom", files);
		
		for (File f : files)
		{
			try
			{
				id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(true);
				doc = factory.newDocumentBuilder().parse(f);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Error loading file " + f, e);
				continue;
			}
			
			try
			{
				ListContainer list = parseDocument(doc);
				list.setListId(id);
				_entries.put(id, list);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Error in file " + f, e);
			}
		}
		verify();
		_log.log(Level.INFO, "MultiSell: Loaded " + _entries.size() + " lists.");
	}
	
	private final ListContainer parseDocument(Document doc)
	{
		int entryId = 1;
		Node attribute;
		ListContainer list = new ListContainer();
		
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				attribute = n.getAttributes().getNamedItem("applyTaxes");
				if (attribute == null)
					list.setApplyTaxes(false);
				else
					list.setApplyTaxes(Boolean.parseBoolean(attribute.getNodeValue()));
				
				attribute = n.getAttributes().getNamedItem("useRate");
				if (attribute != null)
				{
					try
					{
						
						list.setUseRate(Double.valueOf(attribute.getNodeValue()));
						if(list.getUseRate() == 0.0)
							throw new NumberFormatException("The value cannot be 0"); //threat 0 as invalid value
					}
					catch (NumberFormatException e)
					{

						try
						{
							list.setUseRate(Config.class.getField(attribute.getNodeValue()).getDouble(Config.class));
						}
						catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException | DOMException e1)
						{
							_log.warning(e.getMessage() + doc.getLocalName());
							list.setUseRate(1.0);
						}

					}
					catch (DOMException e) 
					{
						_log.warning(e.getMessage() + doc.getLocalName());
					}
				}
				
				attribute = n.getAttributes().getNamedItem("maintainEnchantment");
				if (attribute == null)
					list.setMaintainEnchantment(false);
				else
					list.setMaintainEnchantment(Boolean.parseBoolean(attribute.getNodeValue()));
				
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						Entry e = parseEntry(d, entryId++, list);
						list.getEntries().add(e);
					}
				}
			}
			else if ("item".equalsIgnoreCase(n.getNodeName()))
			{
				Entry e = parseEntry(n, entryId++, list);
				list.getEntries().add(e);
			}
		}
		
		return list;
	}
	
	private final Entry parseEntry(Node n, int entryId, ListContainer list)
	{
		Node attribute;
		Node first = n.getFirstChild();
		final Entry entry = new Entry(entryId);
		
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("ingredient".equalsIgnoreCase(n.getNodeName()))
			{
				int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				long count = Long.parseLong(n.getAttributes().getNamedItem("count").getNodeValue());
				boolean isTaxIngredient, mantainIngredient;
				
				attribute = n.getAttributes().getNamedItem("isTaxIngredient");
				if (attribute != null)
					isTaxIngredient = Boolean.parseBoolean(attribute.getNodeValue());
				else
					isTaxIngredient = false;
				
				attribute = n.getAttributes().getNamedItem("maintainIngredient");
				if (attribute != null)
					mantainIngredient = Boolean.parseBoolean(attribute.getNodeValue());
				else
					mantainIngredient = false;
				
				entry.addIngredient(new Ingredient(id, count, isTaxIngredient, mantainIngredient));
			}
			else if ("production".equalsIgnoreCase(n.getNodeName()))
			{
				int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				long count = (long) (Long.parseLong(n.getAttributes().getNamedItem("count").getNodeValue()) * list.getUseRate());
				
				entry.addProduct(new Ingredient(id, count, false, false));
			}
		}
		
		return entry;
	}
	
	private final void hashFiles(String dirname, List<File> hash)
	{
		File dir = new File(Config.DATAPACK_ROOT, dirname);
		if (!dir.exists())
		{
			_log.log(Level.WARNING, "Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		
		File[] files = dir.listFiles(new XMLFilter());
		for (File f : files)
			hash.add(f);
	}
	
	private final void verify()
	{
		ListContainer list;
		final TIntObjectIterator<ListContainer> iter = _entries.iterator();
		while (iter.hasNext())
		{
			iter.advance();
			list = iter.value();
			
			for (Entry ent : list.getEntries())
			{
				for (Ingredient ing : ent.getIngredients())
				{
					if (!verifyIngredient(ing))
						_log.warning("[MultiSell] can't find ingredient with itemId: " + ing.getItemId() + " in list: " + list.getListId());
				}
				for (Ingredient ing : ent.getProducts())
				{
					if (!verifyIngredient(ing))
						_log.warning("[MultiSell] can't find product with itemId: " + ing.getItemId() + " in list: " + list.getListId());
				}
			}
		}
	}
	
	private final boolean verifyIngredient(Ingredient ing)
	{
		switch (ing.getItemId())
		{
			case PC_BANG_POINTS:
			case CLAN_REPUTATION:
			case FAME:
				return true;
			default:
				if (ing.getTemplate() != null)
					return true;
		}
		
		return false;
	}
	
	private static class SingletonHolder
	{
		protected static final MultiSell _instance = new MultiSell();
	}
}
