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

package custom.poker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;

import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.model.L2ItemInstance;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

/**
 * @author bnb
 *
 */

public class Poker extends Quest
{
	private final int POKER_NPC_ID = 8405;
	private final int[] CURRENCY = { 57, 8570 };
	private FastMap<Integer, Integer> allPlayers = new FastMap<Integer, Integer>();
	private FastMap<Integer, Table> tableList = new FastMap<Integer, Table>();
	private FastMap<Integer, Integer> Bank = new FastMap<Integer, Integer>();
	private FastMap<Integer, Integer> BankCurrency = new FastMap<Integer, Integer>();
	static String qn = "Poker";
	private static final String htmlPath = "data/scripts/custom/Poker/";
	public static final String TABLE_CARD_GETTER = "ThisTableCards";
	
	/**
	 * Constructor
	 * 
	 * @param questId
	 * @param name
	 * @param descr
	 */
	public Poker(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(POKER_NPC_ID);
		addFirstTalkId(POKER_NPC_ID);
		addTalkId(POKER_NPC_ID);
	}
	
	public static void main(String[] args)
	{
		new Poker(-1, qn, "custom");
	}
	
	/**
	 * Checks if this table is already created.
	 * Checks if this player joined on another table.
	 * Creats a new Poker Table and set its isCreated() to true.
	 * @param id : the poker table id.
	 * @param player : The player who created it.
	 * @param cash : The Buy In value of this player.
	 * @param minAdena : The min Buy In value of adena to join this table.
	 * @param maxAdena : The max Buy In value of adena to join this table.
	 * @param initialBet : the Starting Bet amount of each poker game round on this table.
	 */
	
	private synchronized void creatTable(int id, L2PcInstance player, int cash, int minAdena, int maxAdena, int initialBet, int currency)
	{
		if (tableList.containsKey(id))
		{
			player.sendMessage("Sorry this table is already created.");
			return;
		}
		else if (allPlayers.containsKey(player.getObjectId()))
		{
			player.sendMessage("You are already joined in a table.");
			return;
		}
		else
		{
			Table _table = new Table(id, player, cash, minAdena, maxAdena, initialBet, currency);
			_table.setIsCreated(true, player);
			player.sendMessage("A New Poker Table Created!!");
			player.setIsImmobilized(true);
			startQuestTimer("destroyTable" + id, 60000, null, null);
		}
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final int npcId = npc.getNpcId();
		if (player.getQuestState(qn) == null)
			newQuestState(player);
		if (npcId == POKER_NPC_ID)
		{
			if (allPlayers.containsKey(player.getObjectId()))
			{
				player.sendMessage("Poker Manager: You are already joined on a table!!");
				return "";
			}
			else
			{
				if (Bank.containsKey(player.getObjectId()) && BankCurrency.containsKey(player.getObjectId()))
				{
					int currency = BankCurrency.get(player.getObjectId());
					if (Bank.get(player.getObjectId()) > 0 && Util.contains(CURRENCY, currency))
					{
						if (player.getInventory().getSize() < player.getInventoryLimit())
						{
							if (currency == CURRENCY[0])
							{
								player.addItem("PokerBank", 6673, Bank.get(player.getObjectId()), null, true);
								Bank.remove(player.getObjectId());
								BankCurrency.remove(player.getObjectId());
							}
							else if (currency == CURRENCY[1])
							{
								player.addItem("PokerBank", CURRENCY[1], Bank.get(player.getObjectId()), null, true);
								Bank.remove(player.getObjectId());
								BankCurrency.remove(player.getObjectId());
							}
						}
						else
						{
							player.sendMessage("Poker Manager: Your Inventory Is Full !!!");
							return "";
						}
					}
				}
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "check.htm");
				for (int i = 1; i < 11; i++)
				{
					
					if (tableList.containsKey(i))
					{
						Table _table = tableList.get(i);
						String currency = _table.getCurrencyName().startsWith("KK") ? _table.getCurrencyName().substring(3) : _table.getCurrencyName();
						String freeSits = String.valueOf(_table.getFreeSits());
						String initBet = String.valueOf(_table.getInitialBet());
						String green = "009900";
						
						htmContent = htmReplace(htmContent, "%RRGGBB" + i + "%", green);
						htmContent = htmReplace(htmContent, "%currency" + i + "%", currency);
						htmContent = htmReplace(htmContent, "%freeSits" + i + "%", freeSits);
						htmContent = htmReplace(htmContent, "%initBet" + i + "%", initBet);
					}
					else
					{
						htmContent = htmReplace(htmContent, "%RRGGBB" + i + "%", "990000");
						htmContent = htmReplace(htmContent, "%currency" + i + "%", "_");
						htmContent = htmReplace(htmContent, "%freeSits" + i + "%", "9");
						htmContent = htmReplace(htmContent, "%initBet" + i + "%", "_");
						
					}
				}
				return htmContent;
			}
		}
		return "";
	}
	
	private String htmReplace(String htmContent, String data, String replacement)
	{
		htmContent = htmContent.replaceAll(data, replacement.replaceAll("\\$", "\\\\\\$"));
		return htmContent;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("checktable1"))
		{
			int tableId = 1;
			if (!tableList.containsKey(tableId))
			{
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "creat.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				return htmContent;
			}
			else
			{
				Table _table = tableList.get(tableId);
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "join.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				htmContent = htmReplace(htmContent, "%maxAdena%", String.valueOf(_table.getMaxAdena()));
				htmContent = htmReplace(htmContent, "%minAdena%", String.valueOf(_table.getMinAdena()));
				htmContent = htmReplace(htmContent, "%initBet%", String.valueOf(_table.getInitialBet()));
				htmContent = htmReplace(htmContent, "%freeSits%", String.valueOf(_table.getFreeSits()));
				htmContent = htmReplace(htmContent, "%currency%", _table.getCurrencyName());
				Integer i = new Integer(1);
				for (Iterator<L2PcInstance> it = _table.getPlayers().iterator(); i < 10; i++)
				{
					L2PcInstance playerInstance;
					
					try
					{
						playerInstance = it.next();
					}
					catch (NoSuchElementException e)
					{
						playerInstance = null;
					}
					
					if (playerInstance != null)
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "<font color=\"009900\">" + playerInstance.getName() + "</font>, ");
						player.sendMessage(playerInstance.getName());
					}
					else
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "");
					}
					
				}
				
				return htmContent;
			}
		}
		else if (event.equalsIgnoreCase("checktable2"))
		{
			int tableId = 2;
			if (!tableList.containsKey(tableId))
			{
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "creat.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				return htmContent;
			}
			else
			{
				Table _table = tableList.get(tableId);
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "join.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				htmContent = htmReplace(htmContent, "%maxAdena%", String.valueOf(_table.getMaxAdena()));
				htmContent = htmReplace(htmContent, "%minAdena%", String.valueOf(_table.getMinAdena()));
				htmContent = htmReplace(htmContent, "%initBet%", String.valueOf(_table.getInitialBet()));
				htmContent = htmReplace(htmContent, "%freeSits%", String.valueOf(_table.getFreeSits()));
				htmContent = htmReplace(htmContent, "%currency%", _table.getCurrencyName());
				Integer i = new Integer(1);
				for (Iterator<L2PcInstance> it = _table.getPlayers().iterator(); i < 10; i++)
				{
					L2PcInstance playerInstance;
					
					try
					{
						playerInstance = it.next();
					}
					catch (NoSuchElementException e)
					{
						playerInstance = null;
					}
					
					if (playerInstance != null)
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "<font color=\"009900\">" + playerInstance.getName() + "</font>, ");
						player.sendMessage(playerInstance.getName());
					}
					else
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "");
					}
					
				}
				
				return htmContent;
			}
		}
		else if (event.equalsIgnoreCase("checktable3"))
		{
			int tableId = 3;
			if (!tableList.containsKey(tableId))
			{
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "creat.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				return htmContent;
			}
			else
			{
				Table _table = tableList.get(tableId);
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "join.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				htmContent = htmReplace(htmContent, "%maxAdena%", String.valueOf(_table.getMaxAdena()));
				htmContent = htmReplace(htmContent, "%minAdena%", String.valueOf(_table.getMinAdena()));
				htmContent = htmReplace(htmContent, "%initBet%", String.valueOf(_table.getInitialBet()));
				htmContent = htmReplace(htmContent, "%freeSits%", String.valueOf(_table.getFreeSits()));
				htmContent = htmReplace(htmContent, "%currency%", _table.getCurrencyName());
				Integer i = new Integer(1);
				for (Iterator<L2PcInstance> it = _table.getPlayers().iterator(); i < 10; i++)
				{
					L2PcInstance playerInstance;
					
					try
					{
						playerInstance = it.next();
					}
					catch (NoSuchElementException e)
					{
						playerInstance = null;
					}
					
					if (playerInstance != null)
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "<font color=\"009900\">" + playerInstance.getName() + "</font>, ");
						player.sendMessage(playerInstance.getName());
					}
					else
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "");
					}
					
				}
				
				return htmContent;
			}
		}
		else if (event.equalsIgnoreCase("checktable4"))
		{
			int tableId = 4;
			if (!tableList.containsKey(tableId))
			{
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "creat.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				return htmContent;
			}
			else
			{
				Table _table = tableList.get(tableId);
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "join.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				htmContent = htmReplace(htmContent, "%maxAdena%", String.valueOf(_table.getMaxAdena()));
				htmContent = htmReplace(htmContent, "%minAdena%", String.valueOf(_table.getMinAdena()));
				htmContent = htmReplace(htmContent, "%initBet%", String.valueOf(_table.getInitialBet()));
				htmContent = htmReplace(htmContent, "%freeSits%", String.valueOf(_table.getFreeSits()));
				htmContent = htmReplace(htmContent, "%currency%", _table.getCurrencyName());
				Integer i = new Integer(1);
				for (Iterator<L2PcInstance> it = _table.getPlayers().iterator(); i < 10; i++)
				{
					L2PcInstance playerInstance;
					
					try
					{
						playerInstance = it.next();
					}
					catch (NoSuchElementException e)
					{
						playerInstance = null;
					}
					
					if (playerInstance != null)
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "<font color=\"009900\">" + playerInstance.getName() + "</font>, ");
						player.sendMessage(playerInstance.getName());
					}
					else
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "");
					}
					
				}
				
				return htmContent;
			}
		}
		else if (event.equalsIgnoreCase("checktable5"))
		{
			int tableId = 5;
			if (!tableList.containsKey(tableId))
			{
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "creat.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				return htmContent;
			}
			else
			{
				Table _table = tableList.get(tableId);
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "join.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				htmContent = htmReplace(htmContent, "%maxAdena%", String.valueOf(_table.getMaxAdena()));
				htmContent = htmReplace(htmContent, "%minAdena%", String.valueOf(_table.getMinAdena()));
				htmContent = htmReplace(htmContent, "%initBet%", String.valueOf(_table.getInitialBet()));
				htmContent = htmReplace(htmContent, "%freeSits%", String.valueOf(_table.getFreeSits()));
				htmContent = htmReplace(htmContent, "%currency%", _table.getCurrencyName());
				Integer i = new Integer(1);
				for (Iterator<L2PcInstance> it = _table.getPlayers().iterator(); i < 10; i++)
				{
					L2PcInstance playerInstance;
					
					try
					{
						playerInstance = it.next();
					}
					catch (NoSuchElementException e)
					{
						playerInstance = null;
					}
					
					if (playerInstance != null)
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "<font color=\"009900\">" + playerInstance.getName() + "</font>, ");
						player.sendMessage(playerInstance.getName());
					}
					else
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "");
					}
					
				}
				
				return htmContent;
			}
		}
		else if (event.equalsIgnoreCase("checktable6"))
		{
			int tableId = 6;
			if (!tableList.containsKey(tableId))
			{
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "creat.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				return htmContent;
			}
			else
			{
				Table _table = tableList.get(tableId);
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "join.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				htmContent = htmReplace(htmContent, "%maxAdena%", String.valueOf(_table.getMaxAdena()));
				htmContent = htmReplace(htmContent, "%minAdena%", String.valueOf(_table.getMinAdena()));
				htmContent = htmReplace(htmContent, "%initBet%", String.valueOf(_table.getInitialBet()));
				htmContent = htmReplace(htmContent, "%freeSits%", String.valueOf(_table.getFreeSits()));
				htmContent = htmReplace(htmContent, "%currency%", _table.getCurrencyName());
				Integer i = new Integer(1);
				for (Iterator<L2PcInstance> it = _table.getPlayers().iterator(); i < 10; i++)
				{
					L2PcInstance playerInstance;
					
					try
					{
						playerInstance = it.next();
					}
					catch (NoSuchElementException e)
					{
						playerInstance = null;
					}
					
					if (playerInstance != null)
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "<font color=\"009900\">" + playerInstance.getName() + "</font>, ");
						player.sendMessage(playerInstance.getName());
					}
					else
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "");
					}
					
				}
				
				return htmContent;
			}
		}
		else if (event.equalsIgnoreCase("checktable7"))
		{
			int tableId = 7;
			if (!tableList.containsKey(tableId))
			{
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "creat.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				return htmContent;
			}
			else
			{
				Table _table = tableList.get(tableId);
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "join.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				htmContent = htmReplace(htmContent, "%maxAdena%", String.valueOf(_table.getMaxAdena()));
				htmContent = htmReplace(htmContent, "%minAdena%", String.valueOf(_table.getMinAdena()));
				htmContent = htmReplace(htmContent, "%initBet%", String.valueOf(_table.getInitialBet()));
				htmContent = htmReplace(htmContent, "%freeSits%", String.valueOf(_table.getFreeSits()));
				htmContent = htmReplace(htmContent, "%currency%", _table.getCurrencyName());
				Integer i = new Integer(1);
				for (Iterator<L2PcInstance> it = _table.getPlayers().iterator(); i < 10; i++)
				{
					L2PcInstance playerInstance;
					
					try
					{
						playerInstance = it.next();
					}
					catch (NoSuchElementException e)
					{
						playerInstance = null;
					}
					
					if (playerInstance != null)
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "<font color=\"009900\">" + playerInstance.getName() + "</font>, ");
						player.sendMessage(playerInstance.getName());
					}
					else
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "");
					}
					
				}
				
				return htmContent;
			}
		}
		else if (event.equalsIgnoreCase("checktable8"))
		{
			int tableId = 8;
			if (!tableList.containsKey(tableId))
			{
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "creat.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				return htmContent;
			}
			else
			{
				Table _table = tableList.get(tableId);
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "join.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				htmContent = htmReplace(htmContent, "%maxAdena%", String.valueOf(_table.getMaxAdena()));
				htmContent = htmReplace(htmContent, "%minAdena%", String.valueOf(_table.getMinAdena()));
				htmContent = htmReplace(htmContent, "%initBet%", String.valueOf(_table.getInitialBet()));
				htmContent = htmReplace(htmContent, "%freeSits%", String.valueOf(_table.getFreeSits()));
				htmContent = htmReplace(htmContent, "%currency%", _table.getCurrencyName());
				Integer i = new Integer(1);
				for (Iterator<L2PcInstance> it = _table.getPlayers().iterator(); i < 10; i++)
				{
					L2PcInstance playerInstance;
					
					try
					{
						playerInstance = it.next();
					}
					catch (NoSuchElementException e)
					{
						playerInstance = null;
					}
					
					if (playerInstance != null)
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "<font color=\"009900\">" + playerInstance.getName() + "</font>, ");
						player.sendMessage(playerInstance.getName());
					}
					else
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "");
					}
					
				}
				
				return htmContent;
			}
		}
		else if (event.equalsIgnoreCase("checktable9"))
		{
			int tableId = 9;
			if (!tableList.containsKey(tableId))
			{
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "creat.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				return htmContent;
			}
			else
			{
				Table _table = tableList.get(tableId);
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "join.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				htmContent = htmReplace(htmContent, "%maxAdena%", String.valueOf(_table.getMaxAdena()));
				htmContent = htmReplace(htmContent, "%minAdena%", String.valueOf(_table.getMinAdena()));
				htmContent = htmReplace(htmContent, "%initBet%", String.valueOf(_table.getInitialBet()));
				htmContent = htmReplace(htmContent, "%freeSits%", String.valueOf(_table.getFreeSits()));
				htmContent = htmReplace(htmContent, "%currency%", _table.getCurrencyName());
				Integer i = new Integer(1);
				for (Iterator<L2PcInstance> it = _table.getPlayers().iterator(); i < 10; i++)
				{
					L2PcInstance playerInstance;
					
					try
					{
						playerInstance = it.next();
					}
					catch (NoSuchElementException e)
					{
						playerInstance = null;
					}
					
					if (playerInstance != null)
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "<font color=\"009900\">" + playerInstance.getName() + "</font>, ");
						player.sendMessage(playerInstance.getName());
					}
					else
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "");
					}
					
				}
				
				return htmContent;
			}
		}
		else if (event.equalsIgnoreCase("checktable10"))
		{
			int tableId = 10;
			if (!tableList.containsKey(tableId))
			{
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "creat.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				return htmContent;
			}
			else
			{
				Table _table = tableList.get(tableId);
				String htmContent;
				htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlPath + "join.htm");
				htmContent = htmReplace(htmContent, "%tableId%", String.valueOf(tableId));
				htmContent = htmReplace(htmContent, "%maxAdena%", String.valueOf(_table.getMaxAdena()));
				htmContent = htmReplace(htmContent, "%minAdena%", String.valueOf(_table.getMinAdena()));
				htmContent = htmReplace(htmContent, "%initBet%", String.valueOf(_table.getInitialBet()));
				htmContent = htmReplace(htmContent, "%freeSits%", String.valueOf(_table.getFreeSits()));
				htmContent = htmReplace(htmContent, "%currency%", _table.getCurrencyName());
				Integer i = new Integer(1);
				for (Iterator<L2PcInstance> it = _table.getPlayers().iterator(); i < 10; i++)
				{
					L2PcInstance playerInstance;
					
					try
					{
						playerInstance = it.next();
					}
					catch (NoSuchElementException e)
					{
						playerInstance = null;
					}
					
					if (playerInstance != null)
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "<font color=\"009900\">" + playerInstance.getName() + "</font>, ");
						player.sendMessage(playerInstance.getName());
					}
					else
					{
						htmContent = htmReplace(htmContent, "%player" + i + "%", "");
					}
					
				}
				
				return htmContent;
			}
		}
		else if (event.startsWith("creatTable"))
		{
			//make sure the player at least entered 1 value in each edit box.
			if (event.length() < 28)
			{
				player.sendMessage("Please Fill Down All Required info");
				return "";
			}
			
			int tableId = Integer.parseInt(event.substring(10, 12).trim());
			
			String[] values = event.split(" ");
			
			//make sure that every box have a value
			if (values.length < 5)
			{
				player.sendMessage("Please Fill Down All Required info");
				return "";
			}
			
			String minAdenaS = values[1].trim();
			String maxAdenaS = values[2].trim();
			String cashS = values[3].trim();
			String currencyS = values[4].trim();
			
			//makes sure none of fields returns an empty String.
			if (minAdenaS.isEmpty() || maxAdenaS.isEmpty() || cashS.isEmpty())
			{
				player.sendMessage("Please Fill Down All Required info");
				return "";
			}
			int minAdena, maxAdena, cash;
			try
			{
				minAdena = Integer.parseInt(minAdenaS);
				maxAdena = Integer.parseInt(maxAdenaS);
				cash = Integer.parseInt(cashS);
			}
			
			catch (NumberFormatException e)
			{
				//makes sure all entries on boxes are integers.
				player.sendMessage("Please make sure you insert only Positive numbers!!!");
				return "";
			}
			//makes sure all entries are positive and hieghr than 0.
			if (minAdena < 2 || maxAdena <= 0 || cash <= 0)
			{
				player.sendMessage("Negative Values are not Accepted!!. Min. Buy In can't be less than 2");
				return "";
			}
			//makes sure entries are valid 
			//Example : you cannot set your Pay In(cash) > Max adena allowed to join this table.
			else if (minAdena > maxAdena || minAdena > cash || maxAdena < cash)
			{
				player.sendMessage("Wrong Values!!");
				return "";
			}
			
			int initBet;
			
			if (minAdena % 2 == 0)
			{
				initBet = minAdena / 2;
			}
			else
			{
				initBet = (minAdena - 1) / 2;
			}
			
			if (currencyS.equals("MillionAdena"))
			{
				long playerAdena = player.getInventory().getAdena();
				
				//the player cannot set max adena over than his whole adena in inventory
				if (playerAdena < maxAdena * 1000000L)
				{
					player.sendMessage("You must have at Least " + maxAdena + " KK Adena in your inventory to creat this table");
					return "";
				}
				int currency = CURRENCY[0];
				player.sendMessage("Attemp to Creat A Table!! MinBuyIn = " + minAdena + " MaxBuyIn = " + maxAdena + " InitBet = " + initBet + " Cash = " + cash + " Currency = " + currencyS + ".");
				creatTable(tableId, player, cash, minAdena, maxAdena, initBet, currency);
				
			}
			else if (currencyS.equals("EventBoxes"))
			{
				L2ItemInstance boxes = player.getInventory().getItemByItemId(CURRENCY[1]);
				long count = boxes.getCount();
				if (count < maxAdena)
				{
					player.sendMessage("You must have at Least " + maxAdena + " Event Gift Boxes in your inventory to creat this table");
					return "";
				}
				int currency = CURRENCY[1];
				player.sendMessage("Attemp to Creat A Table!! MinBuyIn = " + minAdena + " MaxBuyIn = " + maxAdena + " InitBet = " + initBet + " Cash = " + cash + " Currency = " + currencyS + ".");
				creatTable(tableId, player, cash, minAdena, maxAdena, initBet, currency);
			}
			//finally creats the table :D
			return "";
		}
		
		else if (event.startsWith("joinTable"))
		{
			if (event.length() < 12)
			{
				player.sendMessage("Please Fill down your Buy In");
				return "";
			}
			int tableId = Integer.parseInt(event.substring(9, 11).trim());
			
			String values[] = event.split(" ");
			
			if (values.length < 2)
			{
				player.sendMessage("Please Fill down your Buy In");
				return "";
			}
			
			String cashS = values[1].trim();
			
			if (cashS.isEmpty())
			{
				player.sendMessage("Please insert a valid value for your Buy In.");
				return "";
			}
			
			int cash;
			
			try
			{
				cash = Integer.parseInt(cashS);
			}
			catch (NumberFormatException e)
			{
				//makes sure all entries on boxes are integers.
				player.sendMessage("Please make sure you insert only Positive numbers!!!");
				return "";
			}
			if (!tableList.containsKey(tableId))
			{
				player.sendMessage("Sorry this Table is already Destroyed.");
				return "";
			}
			
			Table _table = tableList.get(tableId);
			if (!_table.isCreated())
			{
				return "";
			}
			int maxAdena = _table.getMaxAdena();
			int minAdena = _table.getMinAdena();
			
			if (cash < minAdena || cash > maxAdena)
			{
				player.sendMessage("Make sure that your Buy in is Greater than table Min. Buy In and Lesser than table Max. Buy In.");
				return "";
			}
			if (_table.getCurrencyName().equals("KK Adena"))
			{
				long playerAdena = player.getInventory().getAdena();
				
				if (cash * 1000000L > playerAdena)
				{
					player.sendMessage("You can't set a Buy In Greater than Adena ammount in your Inventory!!!");
					return "";
				}
			}
			else if (_table.getCurrencyName().equals("Boxes"))
			{
				long boxesCount = player.getInventory().getItemByItemId(CURRENCY[1]).getCount();
				if (cash > boxesCount)
				{
					player.sendMessage("You can't set a Buy In Greater than Event Boxes ammount in your Inventory!!!");
					return "";
				}
			}
			player.sendMessage("Joining Table...........");
			joinTable(tableId, player, cash);
		}
		
		else if (event.startsWith("play"))
		{
			int tableId = allPlayers.get(player.getObjectId());
			Table _table = tableList.get(tableId);
			cancelQuestTimer("AFK Table" + _table.getTableId(), null, player);
			
			if (event.startsWith("playcheck"))
			{
				_log.info("yes event starts with playcheck");
				
				{
					_log.info("setNextCurrentPlayerIndex() returned true");
					
					if (_table.getGame().getPlayerCash(player) == 0)
						_table.getGame().playersAction.put(player, "All In");
					else
						_table.getGame().playersAction.put(player, "Check");
					
					if (_table.getGame().setNextCurrentPlayerIndex())
					{
						player.sendMessage("yes there is next player after this check");
					}
					else
						player.sendMessage("calculate winners or new round after this check");
					
				}
			}
			else if (event.startsWith("playraise"))
			{
				if (event.length() < 11)
				{
					if (_table.getGame().event.equals("start"))
					{
						player.sendMessage("Empty Entry for Raise. Treated like Check.");
						startQuestTimer("playcheck", 50, npc, player);
						return "";
					}
					else if (_table.getGame().event.equals("raise"))
					{
						player.sendMessage("Empty Entry for Raise. Treated like Call.");
						startQuestTimer("playcall", 50, npc, player);
						return "";
					}
					player.sendMessage("SomeThing Wrong in playraise");
					return "";
				}
				
				int raiseValue = 0;
				try
				{
					raiseValue = Integer.parseInt(event.substring(10).trim());
				}
				catch (NumberFormatException e)
				{
					if (_table.getGame().event.equals("start"))
					{
						player.sendMessage("Wrong Entry for Raise. Treated like Check.");
						startQuestTimer("playcheck", 50, npc, player);
						return "";
					}
					else if (_table.getGame().event.equals("raise"))
					{
						player.sendMessage("Wrong Entry for Raise. Treated like Call.");
						startQuestTimer("playcall", 50, npc, player);
						return "";
					}
					player.sendMessage("SomeThing Wrong in catch playraise");
					return "";
				}
				
				int playerCash = _table.getGame().getPlayerCash(player);
				
				if (raiseValue <= 0 || playerCash == 0)
				{
					if (_table.getGame().event.equals("start"))
					{
						player.sendMessage("Wrong Entry for Raise. Treated like Check.");
						startQuestTimer("playcheck", 50, npc, player);
						return "";
					}
					else if (_table.getGame().event.equals("raise"))
					{
						player.sendMessage("Wrong Entry for Raise. Treated like Call.");
						startQuestTimer("playcall", 50, npc, player);
						return "";
					}
					player.sendMessage("SomeThing Wrong in catch playraise");
					return "";
				}
				
				int maxRaise = _table.getGame().getMaxAllowedRaise();
				
				if (_table.getGame().event.equals("start"))
				{
					_table.getGame().event = "raise";
					
					if (raiseValue > playerCash)
						raiseValue = playerCash;
					
					if (raiseValue > maxRaise)
						raiseValue = maxRaise;
					
					_table.getGame().currentRaise = raiseValue;
					
					if (playerCash - raiseValue == 0)
						_table.getGame().playersAction.put(player, "All In");
					else
					{
						String filler = (_table.getCurrencyName().startsWith("KK")) ? " KK" : " Box";
						_table.getGame().playersAction.put(player, "Raised " + raiseValue + filler);
					}
					_table.getGame().playersCash.put(player, playerCash - raiseValue);
					if (raiseValue > 0)
						_table.getGame().playersRaise.put(player, raiseValue);
				}
				else
				{
					_table.getGame().startPlayerIndex = _table.getGame().getActivePlayerIndex(player);
					
					if (raiseValue > playerCash)
						raiseValue = playerCash;
					
					if (raiseValue > maxRaise)
						raiseValue = maxRaise;
					
					_table.getGame().currentRaise = _table.getGame().currentRaise + raiseValue;
					
					if (playerCash - raiseValue == 0)
						_table.getGame().playersAction.put(player, "All In");
					else
					{
						String filler = (_table.getCurrencyName().startsWith("KK")) ? " KK" : " Box";
						_table.getGame().playersAction.put(player, "Raised " + _table.getGame().currentRaise + filler);
					}
					_table.getGame().playersCash.put(player, playerCash - raiseValue);
					_table.getGame().playersRaise.put(player, _table.getGame().currentRaise);
					
				}
				
				_table.getGame().startPlayerIndex = _table.getGame().getActivePlayerIndex(player);
				
				if (_table.getGame().setNextCurrentPlayerIndex())
				{
					player.sendMessage("there is next player after raise");
				}
				else
				{
					player.sendMessage("new round or calculate winner after raise");
				}
			}
			else if (event.startsWith("playleave"))
			{
				if (!_table.getGame().leaveTablePlayers.contains(player))
					_table.getGame().leaveTablePlayers.add(player);
				player.sendMessage("You Will be Auto Kicked from Table after this Round");
			}
			else if (event.startsWith("playfold"))
			{
				_table.getGame().playersAction.put(player, "Fold");
				
				Integer i = new Integer(0);
				for (L2PcInstance plr : _table.getGame().getActivePlayers())
				{
					if (_table.getGame().playersAction.get(plr).equals("Fold") || _table.getGame().playersAction.get(plr).equals("Left"))
						continue;
					i++;
					
				}
				//check if only one player remaining. size 2 means this fold player and only one another player (i didn't remove him from the list yet)
				if (i == 1)
				{
					// immediately give all money on table to the last player and start new game
					
					int cash = 0;
					String winnerName = "";
					
					if (!_table.getGame().playersRaise.isEmpty())
					{
						for (Iterator<L2PcInstance> it = _table.getGame().playersRaise.keySet().iterator(); it.hasNext();)
						{
							L2PcInstance plr = it.next();
							cash = cash + _table.getGame().playersRaise.get(plr);
						}
					}
					if (!_table.getGame().getPots().isEmpty())
					{
						for (Iterator<PotManager> it = _table.getGame().getPots().iterator(); it.hasNext();)
						{
							PotManager pot = it.next();
							cash = cash + pot.getValue();
						}
					}
					
					for (L2PcInstance plr : _table.getGame().getActivePlayers())
					{
						if (!_table.getGame().playersAction.get(plr).equals("Fold"))
						{
							cash = cash + _table.getGame().playersCash.get(plr);
							winnerName = plr.getName();
							_table.getGame().playersCash.put(plr, cash);
							String htmContent = HtmCache.getInstance().getHtm(null, htmlPath + "foldwinner.htm");
							plr.sendPacket(new NpcHtmlMessage(1, htmContent));
						}
					}
					
					for (L2PcInstance plr : _table.getGame().getActivePlayers())
					{
						if (!plr.getName().equals(winnerName))
						{
							String htmContent = HtmCache.getInstance().getHtm(null, htmlPath + "foldlooser.htm");
							htmContent = htmReplace(htmContent, "%winner%", winnerName);
							plr.sendPacket(new NpcHtmlMessage(1, htmContent));
						}
					}
					
					startQuestTimer("progNewGame" + _table.getTableId(), 3000, null, null);
				}
				else if (_table.getGame().setNextCurrentPlayerIndex())
				{
					player.sendMessage("there is next player after fold");
				}
				else
				{
					player.sendMessage("new round or calculate winners after this fold");
				}
			}
			
			else if (event.startsWith("playcall"))
			{
				int newPlayerCash = _table.getGame().getPlayerCash(player) - _table.getGame().currentRaise;
				if (newPlayerCash <= 0)
					startQuestTimer("playallin", 50, npc, player);
				else
				{
					_table.getGame().playersCash.put(player, newPlayerCash);
					_table.getGame().playersRaise.put(player, _table.getGame().currentRaise);
					_table.getGame().playersAction.put(player, "Call " + _table.getGame().currentRaise);
					
					if (_table.getGame().setNextCurrentPlayerIndex())
					{
						player.sendMessage("there is next player after call");
					}
					else
					{
						player.sendMessage("new round or calculate winners after this call");
					}
				}
			}
			else if (event.startsWith("playallin"))
			{
				int allCash = _table.getGame().getPlayerCash(player);
				_table.getGame().playersCash.put(player, 0);
				if (allCash > 0)
					_table.getGame().playersRaise.put(player, allCash);
				_table.getGame().playersAction.put(player, "All In");
				if (_table.getGame().setNextCurrentPlayerIndex())
				{
					player.sendMessage("there is next player after All In");
				}
				else
				{
					player.sendMessage("new round or calculate winners after this All In");
				}
			}
		}
		else if (event.startsWith("AFK Table"))
		{
			int tableId = Integer.parseInt(event.substring(9));
			Table _table = tableList.get(tableId);
			if (!_table.getGame().leaveTablePlayers.contains(player))
				_table.getGame().leaveTablePlayers.add(player);
			
			if (_table.getGame().event.equals("start"))
			{
				player.sendMessage("Wrong Entry for Raise. Treated like Check.");
				startQuestTimer("playcheck", 50, npc, player);
				return "";
			}
			else if (_table.getGame().event.equals("raise"))
			{
				player.sendMessage("Wrong Entry for Raise. Treated like Call.");
				startQuestTimer("playcall", 50, npc, player);
				return "";
			}
		}
		else if (event.startsWith("destroyTable"))
		{
			int id = Integer.parseInt(event.substring(12));
			Table _table;
			if (tableList.containsKey(id))
			{
				_table = tableList.get(id);
				if (!_table.getPlayers().isEmpty())
				{
					for (L2PcInstance plr : _table.getPlayers())
					{
						if (allPlayers.containsKey(plr.getObjectId()))
						{
							int playerTableId;
							playerTableId = allPlayers.get(plr.getObjectId());
							if (playerTableId == tableList.get(id).getTableId())
							{
								allPlayers.remove(plr.getObjectId());
								plr.sendMessage("Table is auto Destroyed due to Inactivity.");
								plr.setIsImmobilized(false);
							}
						}
					}
				}
				
				_table.setIsCreated(false, player);
			}
		}
		else if (event.startsWith("startPokerGame"))
		{
			int tableId = Integer.parseInt(event.substring(14));
			Table _table = tableList.get(tableId);
			_table.startGame();
		}
		else if (event.startsWith("sendWinnerMsg"))
		{
			int tableId = Integer.parseInt(event.substring(13));
			Table _table = tableList.get(tableId);
			_table.getGame().sendWinnersMsg();
			startQuestTimer("progNewGame" + tableId, 3000, null, null);
		}
		else if (event.startsWith("progNewGame"))
		{
			int tableId = Integer.parseInt(event.substring(11));
			Table _table = tableList.get(tableId);
			_table.progressNewGame();
		}
		
		return "";
	}
	
	private synchronized void joinTable(int id, L2PcInstance player, int cash)
	{
		if (!tableList.containsKey(id))
		{
			player.sendMessage("This Table is not even Created yet !!");
			return;
		}
		else if (allPlayers.containsKey(player.getObjectId()))
		{
			player.sendMessage("You are already joined in another table.");
			return;
		}
		
		Table _table = tableList.get(id);
		
		if (_table.getFreeSits() < 1)
		{
			player.sendMessage("Sorry This Table is full.");
			return;
		}
		else
		{
			if (_table.getFreeSits() == 8)
			{
				allPlayers.put(player.getObjectId(), id);
				_table.addPlayer(player, cash);
				player.sendMessage("You Joined table. Starting Game....");
				player.setIsImmobilized(true);
				_table.startGame();
			}
			else if (_table.getFreeSits() == 9)
			{
				allPlayers.put(player.getObjectId(), id);
				_table.addPlayer(player, cash);
				player.setIsImmobilized(true);
				player.sendMessage("You Joined table. Please wait for someone else to join. Table will be Auto destroyed if nobody joins withen 1 min.");
				startQuestTimer("destroyTable" + _table.getTableId(), 60000, null, null);
			}
			else
			{
				allPlayers.put(player.getObjectId(), id);
				_table.addPlayer(player, cash);
				player.setIsImmobilized(true);
				player.sendMessage("You Joined table. Please wait for next round to start game.");
			}
			
		}
		
	}
	
	private class PokerGame
	{
		ArrayList<L2PcInstance> leaveTablePlayers = new ArrayList<L2PcInstance>();
		ArrayList<String> playersNames = new ArrayList<String>(); //holds players names (just used to generate hands)
		ArrayList<L2PcInstance> players = new ArrayList<L2PcInstance>(); //main holder for players playing this round on this table
		ArrayList<L2PcInstance> activePlayers = new ArrayList<L2PcInstance>();
		FastList<PotManager> pots = new FastList<PotManager>();
		FastMap<L2PcInstance, Integer> playersCash = new FastMap<L2PcInstance, Integer>();//<player, chash>
		FastMap<L2PcInstance, String> playersAction = new FastMap<L2PcInstance, String>();//<player, lastAction>
		FastMap<L2PcInstance, Integer> playersRaise = new FastMap<L2PcInstance, Integer>(); //<player , Raise on this round>
		FastMap<L2PcInstance, Integer> winnersRewards = new FastMap<L2PcInstance, Integer>();
		FastMap<L2PcInstance, String> WinnersEvent = new FastMap<L2PcInstance, String>();
		FastMap<L2PcInstance, ArrayList<Integer>> Winners5Cards = new FastMap<L2PcInstance, ArrayList<Integer>>();
		private int startPlayerIndex = 0; //to hold from where the round starts
		private int currentPlayerIndex = 0; //to hold who is the current player (his packet must contain buttons unless his Action AllIn or Fold)
		private int moneyOnTableValue = 0; //to hold the amount of total bets
		private int cond; //to hold which ontable cards to show
		private String event = "";
		private int currentRaise;
		CardManager CardManager;
		Table table;
		
		public PokerGame(Table _table)
		{
			cancelQuestTimer("destroyTable" + _table.getTableId(), null, null);
			CardManager = new CardManager();
			table = _table;
			
			//iterates over all registered players pn this table
			for (Iterator<L2PcInstance> it = _table.getPlayers().iterator(); it.hasNext();)
			{
				L2PcInstance plr = it.next();
				/*if (haveInitBet(plr))
				{*/
				playersNames.add(plr.getName());
				players.add(plr);
				activePlayers.add(plr);
				playersCash.put(plr, _table.playersOnTable.get(plr) - _table.getInitialBet());
				plr.sendMessage("Starting New Poker Round...");
				playersAction.put(plr, "Waiting");
				//}
			}
			winnersRewards.clear();
			Winners5Cards.clear();
			WinnersEvent.clear();
			
			pots.addFirst(new PotManager(_table.getInitialBet() * activePlayers.size(), players));
			moneyOnTableValue = pots.getFirst().getValue();
			event = "start";
			cond = 0;
			currentRaise = 0;
			CardManager.giveCards(playersNames);
			sendUpdatePlayMsg(players, currentPlayerIndex);
			startQuestTimer("AFK Table" + table.getTableId(), 20000, null, activePlayers.get(currentPlayerIndex));
		}
		
		private FastList<PotManager> getPots()
		{
			return pots;
		}
		
		/*	private boolean haveInitBet(L2PcInstance player)
			{
				if (table.getOnTableCash(player) < table.getInitialBet())
				{
					table.removePlayer(player);
					return false;
				}
				return true;
			}*/

		private void sendUpdatePlayMsg(ArrayList<L2PcInstance> _players, int _currentPlayerIndex)
		{
			String htmGlobal; //this is the global part of the htm window (all players allowed to see this info)
			htmGlobal = HtmCache.getInstance().getHtm(null, htmlPath + "playstatus.htm");
			
			String filler = "";
			
			for (Iterator<L2PcInstance> it = players.iterator(); it.hasNext();)
			{
				L2PcInstance plr = it.next();
				if (plr != null)
				{
					if (players.indexOf(plr) == _currentPlayerIndex)
					{
						filler = filler + "<tr><td width = \"100\"><font color = \"009900\">" + plr.getName() + "</font></td><td width = \"100\"><font color = \"009900\">" + playersCash.get(plr) + "</font></td><td width = \"100\"><font color = \"009900\">Playing..</font></td></tr>";
					}
					else
					{
						filler = filler + "<tr><td width = \"100\"><font color = \"990000\">" + plr.getName() + "</font></td><td width = \"100\"><font color = \"990000\">" + playersCash.get(plr) + "</font></td><td width = \"100\"><font color = \"990000\">" + playersAction.get(plr) + "</font></td></tr>";
					}
				}
			}
			
			htmGlobal = htmReplace(htmGlobal, "%status%", filler);
			htmGlobal = htmReplace(htmGlobal, "%moneyOnTable%", String.valueOf(moneyOnTableValue) + " " + table.getCurrencyName());
			
			for (Iterator<L2PcInstance> it = players.iterator(); it.hasNext();)
			{
				String htmContent = htmGlobal; //this is special string for each player
				L2PcInstance plr = it.next();
				String filler1 = "";
				for (int i = 1; i < 3; i++)
				{
					String card = CardManager.getHands().get(plr.getName()).get(i);
					int cardSuit = Integer.parseInt(card.substring(0, 1));
					String cardValue = card.substring(1).trim();
					if (cardValue.length() > 1 && !cardValue.equalsIgnoreCase("10"))
					{
						if (cardValue.equalsIgnoreCase("11"))
							cardValue = "Jack";
						else if (cardValue.equalsIgnoreCase("12"))
							cardValue = "Queen";
						else if (cardValue.equalsIgnoreCase("13"))
							cardValue = "King";
						else if (cardValue.equalsIgnoreCase("14"))
							cardValue = "Ace";
					}
					if (cardSuit == 1)
					{
						filler1 = filler1 + "<td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_b_i00\" width=32 height=32></td>";//Spades
					}
					else if (cardSuit == 2)
					{
						filler1 = filler1 + "<td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_a_i00\" width=32 height=32></td>";//Hearts
					}
					else if (cardSuit == 3)
					{
						filler1 = filler1 + "<td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_d_i00\" width=32 height=32></td>";//Diamonds
					}
					else if (cardSuit == 4)
					{
						filler1 = filler1 + "<td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_c_i00\" width=32 height=32></td>";//Clubs
					}
				}
				
				String filler2 = "";
				
				if (cond >= 3)
				{
					
					for (int i = 1; i < cond + 1; i++)
					{
						String card = CardManager.getHands().get(TABLE_CARD_GETTER).get(i);
						int cardSuit = Integer.parseInt(card.substring(0, 1));
						
						String cardValue = card.substring(1).trim();
						
						if (cardValue.length() > 1 && !cardValue.equalsIgnoreCase("10"))
						{
							if (cardValue.equalsIgnoreCase("11"))
								cardValue = "Jack";
							else if (cardValue.equalsIgnoreCase("12"))
								cardValue = "Queen";
							else if (cardValue.equalsIgnoreCase("13"))
								cardValue = "King";
							else if (cardValue.equalsIgnoreCase("14"))
								cardValue = "Ace";
						}
						if (i == 3)
						{
							if (cardSuit == 1)
							{
								filler2 = filler2 + "</tr><tr></tr><tr><td></td><td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_b_i00\" width=32 height=32></td>";//Spades
							}
							else if (cardSuit == 2)
							{
								filler2 = filler2 + "</tr><tr></tr><tr><td></td><td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_a_i00\" width=32 height=32></td>";//Hearts
							}
							else if (cardSuit == 3)
							{
								filler2 = filler2 + "</tr><tr></tr><tr><td></td><td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_d_i00\" width=32 height=32></td>";//Diamonds
							}
							else if (cardSuit == 4)
							{
								filler2 = filler2 + "</tr><tr></tr><tr><td></td><td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_c_i00\" width=32 height=32></td>";//Clubs
							}
						}
						else if (i == 5)
						{
							if (cardSuit == 1)
							{
								filler2 = filler2 + "</tr><tr></tr><tr><td></td><td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_b_i00\" width=32 height=32></td>";//Spades
							}
							else if (cardSuit == 2)
							{
								filler2 = filler2 + "</tr><tr></tr><tr><td></td><td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_a_i00\" width=32 height=32></td>";//Hearts
							}
							else if (cardSuit == 3)
							{
								filler2 = filler2 + "</tr><tr></tr><tr><td></td><td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_d_i00\" width=32 height=32></td>";//Diamonds
							}
							else if (cardSuit == 4)
							{
								filler2 = filler2 + "</tr><tr></tr><tr><td></td><td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_c_i00\" width=32 height=32></td>";//Clubs
							}
						}
						else
						{
							if (cardSuit == 1)
							{
								filler2 = filler2 + "<td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_b_i00\" width=32 height=32></td>";//Spades
							}
							else if (cardSuit == 2)
							{
								filler2 = filler2 + "<td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_a_i00\" width=32 height=32></td>";//Hearts
							}
							else if (cardSuit == 3)
							{
								filler2 = filler2 + "<td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_d_i00\" width=32 height=32></td>";//Diamonds
							}
							else if (cardSuit == 4)
							{
								filler2 = filler2 + "<td align = \"right\">" + cardValue + "</td>" + "<td align = \"left\"><img src=\"icon.etc_dice_c_i00\" width=32 height=32></td>";//Clubs
							}
						}
					}
				}
				
				String filler3 = "";
				
				if (players.indexOf(plr) == _currentPlayerIndex)
				{
					filler3 = getButtons(plr);
				}
				else if (_currentPlayerIndex != 500)
				{
					filler3 = "<table width =\"300\"><tr><td align = \"center\"><a action=\"bypass -h Quest Poker playleave\">Leave Table</a></td></tr></table>";
				}
				
				htmContent = htmReplace(htmContent, "%cardsOnHand%", filler1);
				htmContent = htmReplace(htmContent, "%cardsOnTable%", filler2);
				htmContent = htmReplace(htmContent, "%buttons%", filler3);
				
				plr.sendPacket(new NpcHtmlMessage(1, htmContent));
				
			}
		}
		
		private String getButtons(L2PcInstance player)
		{
			String filler = "";
			int cash = playersCash.get(player);
			
			if (event.equals("start"))
			{
				if (cash < 1) //check , fold
				{
					filler = "<table width =\"300\"><tr><td align = \"center\"><a action=\"bypass -h Quest Poker playcheck\">Check</a></td><td align = \"center\"><a action=\"bypass -h Quest Poker playfold\">Fold</a></td></tr></table>";
				}
				else
				//check , fold , raise
				{
					filler = "<table width = \"300\"><tr><td align = \"center\"><a action=\"bypass -h Quest Poker playcheck\">Check</a></td><td align = \"center\"><a action=\"bypass -h Quest Poker playfold\">Fold</a></td></tr><tr><td align = \"center\"><a action=\"bypass -h Quest Poker playraise $raise\">Raise</a></td><td align = \"center\"><edit var=\"raise\" width=\"50\" height=\"17\" length=\"4\">KK</td></tr></table>";
				}
			}
			else if (event.equals("raise"))
			{
				if (cash <= currentRaise) //All In, fold
				{
					filler = "<table width = \"300\"><tr><td align =\"center\"><a action=\"bypass -h Quest Poker playallin\">All In</a></td><td align =\"center\"><a action=\"bypass -h Quest Poker playfold\">Fold</a></td></tr></table>";
				}
				else
				//call number, fold , raise
				{
					filler = "<table width = \"300\"><tr><td align = \"center\"><a action=\"bypass -h Quest Poker playcall\">Call (%currentRaiseDiff% KK)</a></td><td align = \"center\"><a action=\"bypass -h Quest Poker playfold\">Fold</a></td></tr><tr><td align = \"center\"><a action=\"bypass -h Quest Poker playraise $raise\">Raise</a></td><td align = \"center\"><edit var=\"raise\" width=\"50\" height=\"17\" length=\"4\">KK</td></tr></table>";
					int currentRaiseDiff = currentRaise - (playersRaise.containsKey(player) ? playersRaise.get(player) : 0);
					filler = htmReplace(filler, "%currentRaiseDiff%", String.valueOf(currentRaiseDiff));
				}
			}
			return filler;
		}
		
		private int getActivePlayerIndex(L2PcInstance player)
		{
			return activePlayers.indexOf(player);
		}
		
		private ArrayList<L2PcInstance> getActivePlayers()
		{
			return activePlayers;
		}
		
		private int getMaxActiveIndex()
		{
			return activePlayers.size() - 1;
		}
		
		private int getPlayerCash(L2PcInstance player)
		{
			return playersCash.get(player);
		}
		
		private int getNextActivePlayerIndex(int index)
		{
			int _index = index;
			if (index >= getMaxActiveIndex())
			{
				_index = 0;
			}
			else
			{
				_index = _index + 1;
			}
			
			return _index;
		}
		
		private int getMaxAllowedRaise()
		{
			int max = 0;
			int limit = 0;
			for (Iterator<L2PcInstance> it = activePlayers.iterator(); it.hasNext();)
			{
				L2PcInstance plr = it.next();
				if (playersAction.get(plr).equals("Fold") || playersAction.get(plr).equals("All In"))
				{
					continue;
				}
				int cash = playersCash.get(plr);
				if (max == 0)
				{
					max = cash;
				}
				else if (max <= cash)
				{
					limit = max;
					max = cash;
				}
				else if (limit < cash)
				{
					limit = cash;
				}
				
			}
			return limit;
		}
		
		private boolean setNextCurrentPlayerIndex()
		{
			int old = currentPlayerIndex;
			int y = 0;
			for (L2PcInstance plr : activePlayers)
			{
				if (!playersAction.get(plr).equals("Fold") && !playersAction.get(plr).equals("All In"))
				{
					y++;
				}
			}
			for (int i = getNextActivePlayerIndex(old); old <= getMaxActiveIndex(); i = getNextActivePlayerIndex(i))
			{
				if (i == currentPlayerIndex)
				{
					cond = 5;
					startNewRound();
					sendUpdatePlayMsg(players, 500);
					return false;
				}
				else if (i == startPlayerIndex)
				{
					if (playersAction.get(activePlayers.get(i)).equals("All In") || playersAction.get(activePlayers.get(i)).equals("Fold"))
					{
						startPlayerIndex = getNextActivePlayerIndex(startPlayerIndex);
						continue;
					}
					else if (y <= 1)
					{
						cond = 5;
						startNewRound();
						sendUpdatePlayMsg(players, 500);
						return false;
					}
					else
					{
						if (cond == 5)
						{
							startNewRound();
							sendUpdatePlayMsg(players, 500);
							return false;
						}
						else
						{
							startNewRound();
							currentPlayerIndex = startPlayerIndex;
							startQuestTimer("AFK Table" + table.getTableId(), 20000, null, activePlayers.get(currentPlayerIndex));
							sendUpdatePlayMsg(players, currentPlayerIndex);
							
							return false;
						}
					}
				}
				
				else if (playersAction.get(activePlayers.get(i)).equals("All In") || playersAction.get(activePlayers.get(i)).equals("Fold"))
					continue;
				
				else
				{
					currentPlayerIndex = i;
					startQuestTimer("AFK Table" + table.getTableId(), 20000, null, activePlayers.get(currentPlayerIndex));
					sendUpdatePlayMsg(players, currentPlayerIndex);
					
					return true;
				}
			}
			return false;
		}
		
		/*private boolean nextIsNewRound(int playerIndex)
		{
			if (getNextActivePlayerIndex(playerIndex) == startPlayerIndex)
				return true;
			else
				return false;
		}*/

		private void startNewRound()
		{
			for (L2PcInstance plr : activePlayers)
			{
				if (!playersAction.get(plr).equals("All In") && !playersAction.get(plr).equals("Fold"))
				{
					playersAction.put(plr, "Waiting");
				}
				
				/*if (playersRaise.get(plr) > 0)
				{
					int old = moneyOnTable.get(plr);
					moneyOnTable.put(plr, old + playersRaise.get(plr));
					moneyOnTableValue = moneyOnTableValue + playersRaise.get(plr);
					playersRaise.put(plr, 0);
				}*/
			}
			
			while (!playersRaise.isEmpty())
			{
				int minRaise = 0;
				for (Iterator<L2PcInstance> it = playersRaise.keySet().iterator(); it.hasNext();)
				{
					L2PcInstance plr = it.next();
					int nextRaise = playersRaise.get(plr);
					
					if (nextRaise <= 0)
						throw new IllegalArgumentException("Wrong conditions in new round pots calculator");
					else if (minRaise == 0)
						minRaise = nextRaise;
					else if (minRaise > nextRaise)
						minRaise = nextRaise;
				}
				if (playersRaise.isEmpty())
					break;
				
				if (minRaise <= 0)
				{
					throw new IllegalArgumentException("Wrong conditions in new round pots calculator");
				}
				
				pots.addFirst(new PotManager(minRaise));
				
				for (Iterator<L2PcInstance> it = activePlayers.iterator(); it.hasNext();)
				{
					L2PcInstance plr = it.next();
					if (playersRaise.containsKey(plr))
					{
						int raise = playersRaise.get(plr);
						int remainRaise = raise - minRaise;
						
						if (remainRaise == 0)
							playersRaise.remove(plr);
						else
							playersRaise.put(plr, remainRaise);
						
						pots.getFirst().addPlayer(plr);
						pots.getFirst().addValue();
					}
				}
				
			}
			
			moneyOnTableValue = 0;
			
			for (PotManager pot : pots)
			{
				moneyOnTableValue = moneyOnTableValue + pot.getValue();
			}
			if (cond == 0)
				cond = 3;
			else if (cond < 5)
				cond = cond + 1;
			else
			{
				RankManager ranker = new RankManager(this, CardManager);
				_log.info("calculate game starting new game");
				boolean keepGoing = true;
				int i = 0;
				while (keepGoing)
				{
					i++;
					ArrayList<String> temp = new ArrayList<String>();
					temp.addAll(ranker.getPossibleRanks());
					FastList<String> winners = ranker.getWinners(ranker.eventHandler, temp);
					for (PotManager pot : pots)
					{
						if (pot.getValue() == 0)
							continue;
						
						int livePlayers = 0;
						for (L2PcInstance plr : pot.getPotPlayers())
						{
							if (playersAction.get(plr).equals("Fold"))
								continue;
							livePlayers++;
						}
						
						if (livePlayers == 0)
						{
							for (L2PcInstance plr : activePlayers)
							{
								if (!winners.contains(plr.getName()))
									continue;
								if (winners.size() == 1)
								{
									int oldCash = playersCash.get(plr);
									int earnedCash = pot.getValue();
									int newCash = oldCash + earnedCash;
									playersCash.put(plr, newCash);
									pot.value = 0;
									if (!winnersRewards.containsKey(plr))
									{
										winnersRewards.put(plr, earnedCash);
										Winners5Cards.put(plr, ranker.rankedCards.get(plr.getName()));
										String event = String.valueOf(ranker.eventHandler);
										WinnersEvent.put(plr, event);
										
									}
									else
									{
										winnersRewards.put(plr, winnersRewards.get(plr) + earnedCash);
									}
									
									break;
								}
								else
								{
									for (String name : winners)
									{
										if (plr.getName().equals(name))
										{
											int oldCash = playersCash.get(plr);
											int earnedCash = pot.getValue() / winners.size();
											int newCash = oldCash + earnedCash;
											playersCash.put(plr, newCash);
											
											if (!winnersRewards.containsKey(plr))
											{
												winnersRewards.put(plr, earnedCash);
												Winners5Cards.put(plr, ranker.rankedCards.get(plr.getName()));
												String event = String.valueOf(ranker.eventHandler);
												WinnersEvent.put(plr, event);
											}
											else
											{
												winnersRewards.put(plr, winnersRewards.get(plr) + earnedCash);
											}
										}
									}
								}
							}
							pot.value = 0;
						}
						else if (winners.size() == 1)
						{
							for (L2PcInstance plr : pot.getPotPlayers())
							{
								if (winners.contains(plr.getName()))
								{
									int oldCash = playersCash.get(plr);
									int earnedCash = pot.getValue();
									int newCash = oldCash + earnedCash;
									playersCash.put(plr, newCash);
									pot.value = 0;
									
									if (!winnersRewards.containsKey(plr))
									{
										winnersRewards.put(plr, earnedCash);
										Winners5Cards.put(plr, ranker.rankedCards.get(plr.getName()));
										String event = String.valueOf(ranker.eventHandler);
										WinnersEvent.put(plr, event);
									}
									else
									{
										winnersRewards.put(plr, winnersRewards.get(plr) + earnedCash);
									}
									break;
								}
							}
						}
						else if (winners.size() > 1)
						{
							ArrayList<L2PcInstance> tempWinners = new ArrayList<L2PcInstance>();
							for (L2PcInstance plr : pot.getPotPlayers())
							{
								if (winners.contains(plr.getName()))
								{
									tempWinners.add(plr);
								}
							}
							if (!tempWinners.isEmpty())
							{
								for (L2PcInstance plr : tempWinners)
								{
									int oldCash = playersCash.get(plr);
									int earnedCash = pot.getValue() / winners.size();
									int newCash = oldCash + earnedCash;
									playersCash.put(plr, newCash);
									
									if (!winnersRewards.containsKey(plr))
									{
										winnersRewards.put(plr, earnedCash);
										Winners5Cards.put(plr, ranker.rankedCards.get(plr.getName()));
										String event = String.valueOf(ranker.eventHandler);
										WinnersEvent.put(plr, event);
									}
									else
									{
										winnersRewards.put(plr, winnersRewards.get(plr) + earnedCash);
									}
								}
								pot.value = 0;
							}
						}
					}
					keepGoing = false;
					for (PotManager pot : pots)
					{
						if (pot.getValue() != 0)
						{
							keepGoing = true;
							break;
						}
					}
				}
				startQuestTimer("sendWinnerMsg" + table.getTableId(), 4000, null, null);
				return;
			}
			
			event = "start";
			currentRaise = 0;
		}
		
		private String[] getCardsName(ArrayList<Integer> cardsRank)
		{
			String[] cardsName = new String[5];
			for (int i = 0; i < cardsRank.size(); i++)
			{
				String rank = String.valueOf(cardsRank.get(i));
				
				if (rank.equals("14") || rank.equals("1"))
					rank = "A";
				else if (rank.equals("13"))
					rank = "K";
				else if (rank.equals("12"))
					rank = "Q";
				else if (rank.equals("11"))
					rank = "J";
				cardsName[i] = rank;
			}
			return cardsName;
		}
		
		private void sendWinnersMsg()
		{
			String htmContent;
			String filler = "";
			htmContent = HtmCache.getInstance().getHtm(null, htmlPath + "winners.htm");
			for (L2PcInstance plr : winnersRewards.keySet())
			{
				String cards[] = getCardsName(Winners5Cards.get(plr));
				int reward = winnersRewards.get(plr);
				String event = WinnersEvent.get(plr);
				String name = plr.getName();
				
				if (event.equals("Royal Flush"))
				{
					filler = filler + name + " Won $" + reward + " With " + event + " !!!<br>";
				}
				
				else if (event.equals("Straight Flush"))
				{
					filler = filler + name + " Won $" + reward + " With " + event + "to " + cards[0] + " !!!<br>";
				}
				else if (event.equals("Four of a Kind"))
				{
					filler = filler + name + " Won $" + reward + " With " + event + " of " + cards[0] + "'s. And " + cards[1] + " as a Kicker !!!<br>";
				}
				else if (event.equals("Full House"))
				{
					filler = filler + name + " Won $" + reward + " With " + event + " " + cards[0] + "'s to " + cards[1] + "'s !!!<br>";
				}
				else if (event.equals("Flush"))
				{
					filler = filler + name + " Won $" + reward + " With " + event + " of " + cards[0] + ", " + cards[1] + ", " + cards[2] + ", " + cards[3] + ", " + cards[4] + " !!!<br>";
				}
				else if (event.equals("Straight"))
				{
					filler = filler + name + " Won $" + reward + " With " + event + " to " + cards[0] + " !!!<br>";
				}
				else if (event.equals("Three of a Kind"))
				{
					filler = filler + name + " Won $" + reward + " With " + event + " of " + cards[0] + "'s. And " + cards[1] + " " + cards[2] + " as Kickers!!!<br>";
				}
				else if (event.equals("Two Pair"))
				{
					filler = filler + name + " Won $" + reward + " With " + event + "s of " + cards[0] + "'s and " + cards[1] + "'s. With " + cards[2] + " as a Kicker!!!<br>";
				}
				else if (event.equals("Pair"))
				{
					filler = filler + name + " Won $" + reward + " With One " + event + " of " + cards[0] + "'s. And " + cards[1] + ", " + cards[2] + ", " + cards[3] + " as Kickers!!!<br>";
				}
				else if (event.equals("High Card"))
				{
					filler = filler + name + " Won $" + reward + " With " + event + " " + cards[0] + ". And " + cards[1] + ", " + cards[2] + ", " + cards[3] + ", " + cards[4] + "as Kickers!!!<br>";
				}
			}
			
			htmContent = htmReplace(htmContent, "%winners%", filler);
			
			for (L2PcInstance plr : players)
			{
				plr.sendPacket(new NpcHtmlMessage(1, htmContent));
			}
		}
	}//End of PokerGame Class
	
	private class Table
	{
		private final int MAX_PLAYERS_ON_TABLE = 9;
		private int _tableId;
		private int _freeSits;
		private int _minAdena;
		private int _maxAdena;
		private int _initialBet;
		private boolean _created;
		private PokerGame game;
		private int _currency;
		
		FastMap<L2PcInstance, Integer> playersOnTable = new FastMap<L2PcInstance, Integer>();
		
		public Table(int tableId, L2PcInstance player, int cash, int minAdena, int maxAdena, int initialBet, int currency)
		{
			_tableId = tableId;
			_currency = currency;
			addPlayer(player, cash);
			_minAdena = minAdena;
			_maxAdena = maxAdena;
			_initialBet = initialBet;
			
		}
		
		public String getCurrencyName()
		{
			if (_currency == CURRENCY[0])
			{
				return "KK Adena";
			}
			else if (_currency == CURRENCY[1])
			{
				return "Boxes";
			}
			return "Conflict!";
		}
		
		public int getCurrencyId()
		{
			return _currency;
		}
		
		public void progressNewGame()
		{
			for (L2PcInstance plr : getGame().playersAction.keySet())
			{
				int currency = getCurrencyId();
				int cash = getGame().playersCash.get(plr);
				boolean left = false;
				playersOnTable.put(plr, getGame().playersCash.get(plr));
				Bank.put(plr.getObjectId(), cash);
				BankCurrency.put(plr.getObjectId(), currency);
				if (getGame().leaveTablePlayers.contains(plr))
				{
					if (playersOnTable.containsKey(plr))
						playersOnTable.remove(plr);
					if (allPlayers.containsKey(plr.getObjectId()))
						allPlayers.remove(plr.getObjectId());
					plr.setIsImmobilized(false);
					sendHtm(plr, "leave.htm");
					left = true;
				}
				if (cash <= 0)
				{
					if (playersOnTable.containsKey(plr))
						playersOnTable.remove(plr);
					if (Bank.containsKey(plr.getObjectId()))
					{
						Bank.remove(plr.getObjectId());
						BankCurrency.remove(plr.getObjectId());
					}
					if (allPlayers.containsKey(plr.getObjectId()))
						allPlayers.remove(plr.getObjectId());
					plr.setIsImmobilized(false);
					if (!left)
						sendHtm(plr, "nocash.htm");
				}
				else if (cash < getInitialBet())
				{
					
					if (playersOnTable.containsKey(plr))
						playersOnTable.remove(plr);
					if (allPlayers.containsKey(plr.getObjectId()))
						allPlayers.remove(plr.getObjectId());
					plr.setIsImmobilized(false);
					if (!left)
						sendHtm(plr, "noinitbet.htm");
				}
			}
			
			getGame().activePlayers.clear();
			getGame().CardManager = null;
			getGame().cond = 0;
			getGame().currentRaise = 0;
			getGame().leaveTablePlayers.clear();
			getGame().moneyOnTableValue = 0;
			getGame().players.clear();
			getGame().playersAction.clear();
			getGame().playersCash.clear();
			getGame().playersNames.clear();
			getGame().playersRaise.clear();
			getGame().pots.clear();
			getGame().startPlayerIndex = 0;
			getGame().Winners5Cards.clear();
			getGame().WinnersEvent.clear();
			getGame().winnersRewards.clear();
			
			if (playersOnTable.size() > 1)
			{
				startQuestTimer("startPokerGame" + getTableId(), 5000, null, null);
			}
			else
			{
				startQuestTimer("destroyTable" + getTableId(), 60000, null, null);
				for (Iterator<L2PcInstance> it = playersOnTable.keySet().iterator(); it.hasNext();)
					sendHtm(it.next(), "wait.htm");
			}
			
		}
		
		public void sendHtm(L2PcInstance plr, String fileName)
		{
			String htmContent = HtmCache.getInstance().getHtm(null, htmlPath + fileName);
			plr.sendPacket(new NpcHtmlMessage(1, htmContent));
		}
		
		public void startGame()
		{
			
			game = new PokerGame(this);
			
		}
		
		public PokerGame getGame()
		{
			return game;
		}
		
		public boolean isCreated()
		{
			return _created;
		}
		
		public void setIsCreated(boolean b, L2PcInstance player)
		{
			if (b)
			{
				allPlayers.put(player.getObjectId(), getTableId());
				tableList.put(getTableId(), this);
				this._created = true;
			}
			else
			{
				playersOnTable.clear();
				_minAdena = 0;
				_maxAdena = 0;
				_initialBet = 0;
				_currency = 0;
				this._created = false;
				tableList.remove(this._tableId);
			}
		}
		
		public int getTableId()
		{
			return _tableId;
		}
		
		public Set<L2PcInstance> getPlayers()
		{
			return playersOnTable.keySet();
		}
		
		public int getFreeSits()
		{
			_freeSits = MAX_PLAYERS_ON_TABLE - playersOnTable.size();
			return _freeSits;
		}
		
		public void addPlayer(L2PcInstance player, int cash)
		{
			final int currencyId = getCurrencyId();
			if (!playersOnTable.containsKey(player) && Util.contains(CURRENCY, currencyId) && player.destroyItemByItemId("PokerAdena", currencyId, (currencyId == CURRENCY[0]) ? cash * 1000000L : cash, null, true))
			{
				Bank.put(player.getObjectId(), cash);
				BankCurrency.put(player.getObjectId(), currencyId);
				playersOnTable.put(player, cash);
				return;
			}
		}
		
		public int getMaxAdena()
		{
			return _maxAdena;
		}
		
		public int getMinAdena()
		{
			return _minAdena;
		}
		
		public int getInitialBet()
		{
			return _initialBet;
		}
		
	}
	
	/**
	 * Creats a new Deck for each table
	 * Give a 2 cards hand to each player on table
	 * Set a 5 cards on land of table
	 */
	private class CardManager
	{
		final int[] CARDS_VALUE = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };
		final int[] CARDS_SUIT = { 1, 2, 3, 4 };
		FastMap<String, FastMap<Integer, String>> hands = new FastMap<String, FastMap<Integer, String>>();
		ArrayList<Integer> rankHandler = new ArrayList<Integer>();
		
		private void giveCards(ArrayList<String> players)
		{
			final ArrayList<String> drawnCards = new ArrayList<String>();
			int playersCount = players.size();
			for (int i = 0; i < playersCount; i++)
			{
				hands.put(players.get(i), new FastMap<Integer, String>());
				
				int counter = 1;
				do
				{
					int x = Rnd.get(CARDS_VALUE.length);
					int y = Rnd.get(CARDS_SUIT.length);
					if (!drawnCards.isEmpty() && drawnCards.contains(String.valueOf(CARDS_SUIT[y]) + String.valueOf(CARDS_VALUE[x])))
					{
						continue;
					}
					else
					{
						hands.get(players.get(i)).put(counter, String.valueOf(CARDS_SUIT[y]) + String.valueOf(CARDS_VALUE[x]));
						counter++;
						drawnCards.add(String.valueOf(CARDS_SUIT[y]) + String.valueOf(CARDS_VALUE[x]));
					}
				}
				while (counter < 3);
			}
			
			int counter2 = 1;
			
			hands.put(TABLE_CARD_GETTER, new FastMap<Integer, String>());
			
			while (counter2 < 6)
			{
				int x = Rnd.get(CARDS_VALUE.length);
				int y = Rnd.get(CARDS_SUIT.length);
				if (!drawnCards.isEmpty() && drawnCards.contains(String.valueOf(CARDS_SUIT[y]) + String.valueOf(CARDS_VALUE[x])))
				{
					continue;
				}
				else
				{
					hands.get(TABLE_CARD_GETTER).put(counter2, String.valueOf(CARDS_SUIT[y]) + String.valueOf(CARDS_VALUE[x]));
					counter2++;
					drawnCards.add(String.valueOf(CARDS_SUIT[y]) + String.valueOf(CARDS_VALUE[x]));
				}
			}
		}
		
		/**
		 * @return
		 */
		private FastMap<String, FastMap<Integer, String>> getHands()
		{
			return hands;
		}
		
		private ArrayList<Integer> getRankHandler()
		{
			return rankHandler;
		}
		
		private String getHandValue(String playerName, int cardsNumber)
		{
			String[] cards = new String[cardsNumber];
			if (hands.containsKey(playerName))
			{
				for (int i = 0; i < 2; i++)
				{
					cards[i] = hands.get(playerName).get(i + 1);
				}
				if (cardsNumber > 2)
				{
					for (int i = 0; i < 5; i++)
					{
						cards[i + 2] = hands.get(TABLE_CARD_GETTER).get(i + 1);
					}
				}
			}
			if (isRoyalFlush(cards) == true)
				return "Royal Flush";
			if (isStraightFlush(cards) == true)
				return "Straight Flush";
			if (isFourOfAKind(cards) == true)
				return "Four of a Kind";
			if (isFullHouse(cards) == true)
				return "Full House";
			if (isFlush(cards) == true)
				return "Flush";
			if (isStraight(cards) == true)
				return "Straight";
			if (isThreeOfAKind(cards) == true)
				return "Three of a Kind";
			if (isTwoPair(cards) == true)
				return "Two Pair";
			if (isPair(cards) == true)
				return "Pair";
			if (isHighCard(cards) == true)
				return "High Card";
			_log.info("OMG getHandValue returned NOTHING !!!!!!!!!! FIX MEEEE");
			return "Nothing";
		}
		
		private boolean isStraightFlush(String[] _cards)
		{
			if (_cards.length < 5 || !isFlush(_cards))
				return false;
			
			int spadesCounter = 0;
			int heartsCounter = 0;
			int diamondsCounter = 0;
			int clubsCounter = 0;
			
			for (int i = 0; i < _cards.length; i++)
			{
				if (_cards[i].charAt(0) == '1')
					spadesCounter++;
				else if (_cards[i].charAt(0) == '2')
					heartsCounter++;
				else if (_cards[i].charAt(0) == '3')
					diamondsCounter++;
				else if (_cards[i].charAt(0) == '4')
					clubsCounter++;
				
			}
			if (spadesCounter >= 5)
			{
				String[] cards = new String[spadesCounter];
				int counter = 0;
				for (int i = 0; i < _cards.length; i++)
				{
					if (_cards[i].charAt(0) == '1')
					{
						cards[counter] = _cards[i];
						counter++;
					}
				}
				if (isStraight(cards))
					return true;
			}
			else if (heartsCounter >= 5)
			{
				String[] cards = new String[heartsCounter];
				int counter = 0;
				for (int i = 0; i < _cards.length; i++)
				{
					if (_cards[i].charAt(0) == '2')
					{
						cards[counter] = _cards[i];
						counter++;
					}
				}
				if (isStraight(cards))
					return true;
			}
			else if (diamondsCounter >= 5)
			{
				String[] cards = new String[diamondsCounter];
				int counter = 0;
				for (int i = 0; i < _cards.length; i++)
				{
					if (_cards[i].charAt(0) == '3')
					{
						cards[counter] = _cards[i];
						counter++;
					}
				}
				if (isStraight(cards))
					return true;
			}
			else if (clubsCounter >= 5)
			{
				String[] cards = new String[clubsCounter];
				int counter = 0;
				for (int i = 0; i < _cards.length; i++)
				{
					if (_cards[i].charAt(0) == '4')
					{
						cards[counter] = _cards[i];
						counter++;
					}
				}
				if (isStraight(cards))
					return true;
			}
			return false;
		}
		
		private boolean isFullHouse(String[] _cards)
		{
			if (_cards.length < 5)
				return false;
			
			//arange cards from big to small 
			String[] cards = new String[_cards.length];
			cards = arrangeMe(_cards);
			
			//now lets check if its 3 of kind
			int counter = 1;
			int threeKindCardsVlaue = 0;
			for (int i = 0; i < cards.length; i++)
			{
				if (threeKindCardsVlaue != 0)
					break;
				for (int x = i + 1; x < cards.length; x++)
				{
					if (cards[i].substring(1).equals(cards[x].substring(1)))
					{
						counter++;
						if (counter == 3)
						{
							threeKindCardsVlaue = Integer.parseInt(cards[i].substring(1));
							break;
						}
					}
				}
				counter = 1;
			}
			//check if this 7 cards not contain 3 kind then they cant be full house so return false
			if (threeKindCardsVlaue == 0)
				return false;
			counter = 1;
			//now lets check if there is another pair (not same value with the 3 kind)
			for (int i = 0; i < cards.length; i++)
			{
				for (int x = i + 1; x < cards.length; x++)
				{
					if (cards[i].substring(1).equals(cards[x].substring(1)))
					{
						counter++;
						if (counter == 2 && Integer.parseInt(cards[i].substring(1)) != threeKindCardsVlaue)
						{
							rankHandler.clear();
							rankHandler.add(threeKindCardsVlaue);
							rankHandler.add(Integer.parseInt(cards[i].substring(1)));
							return true;
						}
					}
				}
				counter = 1;
			}
			return false;
			
		}
		
		private boolean isFlush(String[] _cards)
		{
			if (_cards.length < 5)
				return false;
			
			//organize cards from big to small
			String[] cards = new String[_cards.length];
			cards = arrangeMe(_cards);
			
			//check if flush
			for (int i = 0; i < 3; i++)
			{
				int counter = 1;
				
				for (int x = i + 1; x < cards.length; x++)
				{
					
					if (cards[i].charAt(0) == cards[x].charAt(0))
					{
						counter++;
						if (counter == 5)
						{
							rankHandler.clear();
							String flushSuit = String.valueOf(cards[i].charAt(0));
							for (int y = 0; y < cards.length; y++)
							{
								if (cards[y].startsWith(flushSuit))
								{
									rankHandler.add(Integer.parseInt(cards[y].substring(1)));
									
									if (rankHandler.size() == 5)
										break;
								}
							}
							return true;
						}
					}
				}
			}
			return false;
		}
		
		/**
		 * 
		 * @param _cards : String array of cards (1st char always stands for suit, all other chars stands for rank
		 * @return	true if those cards contains at least 5 cards in straight order 
		 */
		private boolean isStraight(String[] _cards)
		{
			if (_cards.length < 5)
				return false;
			
			FastList<String> tempCards = new FastList<String>();
			FastList<String> temp = new FastList<String>();
			
			//remove duplicates
			for (int i = 0; i < _cards.length; i++)
			{
				if (temp.contains(_cards[i].substring(1)))
					continue;
				temp.add(_cards[i].substring(1));
				tempCards.add(_cards[i]);
			}
			
			//check if without duplicate the list size at least is 5 or else it cant be flush
			if (tempCards.size() < 5)
				return false;
			
			//copy the ArrayList to normal Array
			String[] cards = new String[tempCards.size()];
			for (int i = 0; i < tempCards.size(); i++)
			{
				cards[i] = tempCards.get(i);
			}
			
			//arrange the normal Array
			cards = arrangeMe(cards);
			
			rankHandler.clear();
			
			//check if every card Rank value - next card Rank value == 1 and if this happens for 5 cards
			int counter = 0;
			for (int i = 0; i < cards.length - 1; i++)
			{
				if (Integer.parseInt(cards[i].substring(1)) - Integer.parseInt(cards[i + 1].substring(1)) == 1)
				{
					rankHandler.add(Integer.parseInt(cards[i].substring(1)));
					counter++;
					if (counter == 4)
					{
						rankHandler.add(Integer.parseInt(cards[i + 1].substring(1)));
						return true;
					}
				}
				else
				{
					rankHandler.clear();
					counter = 0;
					continue;
				}
			}
			//check if this array contains Ace and that lowest value in it is lesser than 6 (cause if is bigger
			// or equal to 6 then it dont contain 5)
			if (cards[0].substring(1).equals("14") && Integer.parseInt(cards[cards.length - 1].substring(1)) < 6)
			{
				//check if it contain 5
				boolean contain5 = false;
				for (int i = 1; i < cards.length; i++)
				{
					if (cards[i].substring(1).equals("5"))
					{
						contain5 = true;
					}
				}
				//if it dont contain 5 then return false
				if (!contain5)
					return false;
				else
				{
					//else replace the 1st card (that is the Ace) with 1 with same suit
					cards[0] = cards[0].charAt(0) + "1";
					
					//arrange the array again
					cards = arrangeMe(cards);
					rankHandler.clear();
					//now same check for straight again
					int counter2 = 0;
					for (int i = 0; i < cards.length - 1; i++)
					{
						if (Integer.parseInt(cards[i].substring(1)) - Integer.parseInt(cards[i + 1].substring(1)) == 1)
						{
							rankHandler.add(Integer.parseInt(cards[i].substring(1)));
							counter2++;
							if (counter2 == 4)
							{
								rankHandler.add(Integer.parseInt(cards[i + 1].substring(1)));
								return true;
							}
						}
						else
						{
							rankHandler.clear();
							counter2 = 0;
							continue;
						}
					}
				}
			}
			
			return false;
		}
		
		private boolean isRoyalFlush(String[] _cards)
		{
			if (_cards.length < 5)
				return false;
			
			char suit = '0';
			for (int i = 0; i < 3; i++)
			{
				//check if Flush and gets the Flush suit
				int counter = 1;
				for (int x = i + 1; x < _cards.length; x++)
				{
					
					if (_cards[i].charAt(0) == _cards[x].charAt(0))
					{
						counter++;
						if (counter == 5)
							suit = _cards[i].charAt(0);
					}
				}
			}
			//now if there is a flush copy all the flush cards to an array 
			if (suit != '0')
			{
				ArrayList<Integer> values = new ArrayList<Integer>();
				
				//copy all the Rank of flush cards to an array 
				for (int i = 0; i < _cards.length; i++)
				{
					if (_cards[i].startsWith(String.valueOf(suit)))
					{
						values.add(Integer.parseInt(_cards[i].substring(1)));
					}
				}
				//check if this Array contains Ace(14) , King (13) , Queen (12) , Jack (11) and 10
				if (values.contains(14) && values.contains(13) && values.contains(12) && values.contains(11) && values.contains(10))
					return true;
			}
			
			return false;
		}
		
		private boolean isFourOfAKind(String[] _cards)
		{
			if (_cards.length < 4)
				return false;
			String[] cards = new String[_cards.length];
			cards = arrangeMe(_cards);
			
			int fourKindCardsVlaue = 0;
			int counter = 1;
			for (int i = 0; i < cards.length; i++)
			{
				if (fourKindCardsVlaue != 0)
					break;
				for (int x = i + 1; x < cards.length; x++)
				{
					if (cards[i].substring(1).equals(cards[x].substring(1)))
					{
						counter++;
						if (counter == 4)
						{
							fourKindCardsVlaue = Integer.parseInt(cards[i].substring(1));
							break;
						}
					}
				}
				counter = 1;
			}
			
			if (fourKindCardsVlaue == 0)
				return false;
			
			rankHandler.clear();
			rankHandler.add(fourKindCardsVlaue);
			for (int i = 0; i < cards.length; i++)
			{
				if (Integer.parseInt(cards[i].substring(1)) != fourKindCardsVlaue)
				{
					rankHandler.add(Integer.parseInt(cards[i].substring(1)));
					if (rankHandler.size() == 2)
					{
						return true;
					}
				}
			}
			return false;
		}
		
		private boolean isThreeOfAKind(String[] _cards)
		{
			if (_cards.length < 3)
				return false;
			String[] cards = new String[_cards.length];
			cards = arrangeMe(_cards);
			
			int threeKindCardsVlaue = 0;
			int counter = 1;
			for (int i = 0; i < cards.length; i++)
			{
				if (threeKindCardsVlaue != 0)
					break;
				for (int x = i + 1; x < cards.length; x++)
				{
					if (cards[i].substring(1).equals(cards[x].substring(1)))
					{
						counter++;
						if (counter == 3)
						{
							threeKindCardsVlaue = Integer.parseInt(cards[i].substring(1));
							break;
						}
					}
				}
				counter = 1;
			}
			
			if (threeKindCardsVlaue == 0)
				return false;
			
			rankHandler.clear();
			rankHandler.add(threeKindCardsVlaue);
			for (int i = 0; i < cards.length; i++)
			{
				if (Integer.parseInt(cards[i].substring(1)) != threeKindCardsVlaue)
				{
					rankHandler.add(Integer.parseInt(cards[i].substring(1)));
					if (rankHandler.size() == 3)
					{
						return true;
					}
				}
			}
			return false;
		}
		
		private boolean isTwoPair(String[] _cards)
		{
			if (_cards.length < 4)
				return false;
			
			//arange cards from big to small 
			String[] cards = new String[_cards.length];
			cards = arrangeMe(_cards);
			
			//now lets check if its one pair
			int counter = 1;
			int onePairHighValue = 0;
			for (int i = 0; i < cards.length; i++)
			{
				if (onePairHighValue != 0)
					break;
				for (int x = i + 1; x < cards.length; x++)
				{
					if (cards[i].substring(1).equals(cards[x].substring(1)))
					{
						counter++;
						if (counter == 2)
						{
							onePairHighValue = Integer.parseInt(cards[i].substring(1));
							break;
						}
					}
				}
				counter = 1;
			}
			//check if this 7 cards not contain one pair
			if (onePairHighValue == 0)
				return false;
			
			//now lets check if there is another pair with lower value
			counter = 1;
			int secondPairLowValue = 0;
			for (int i = 0; i < cards.length; i++)
			{
				if (secondPairLowValue != 0)
					break;
				for (int x = i + 1; x < cards.length; x++)
				{
					if (cards[i].substring(1).equals(cards[x].substring(1)))
					{
						counter++;
						if (counter == 2 && Integer.parseInt(cards[i].substring(1)) != onePairHighValue)
						{
							secondPairLowValue = Integer.parseInt(cards[i].substring(1));
						}
					}
				}
				counter = 1;
			}
			if (secondPairLowValue == 0)
				return false;
			for (int i = 0; i < cards.length; i++)
			{
				if (Integer.parseInt(cards[i].substring(1)) != onePairHighValue && Integer.parseInt(cards[i].substring(1)) != secondPairLowValue)
				{
					rankHandler.clear();
					rankHandler.add(onePairHighValue);
					rankHandler.add(secondPairLowValue);
					rankHandler.add(Integer.parseInt(cards[i].substring(1)));
					return true;
				}
			}
			return false;
		}
		
		private boolean isPair(String[] _cards)
		{
			//arange cards from big to small 
			String[] cards = new String[_cards.length];
			cards = arrangeMe(_cards);
			
			//now lets check if its one pair
			int counter = 1;
			int pairValue = 0;
			for (int i = 0; i < cards.length; i++)
			{
				for (int x = i + 1; x < cards.length; x++)
				{
					if (cards[i].substring(1).equals(cards[x].substring(1)))
					{
						counter++;
						if (counter == 2)
						{
							pairValue = Integer.parseInt(cards[i].substring(1));
							rankHandler.clear();
							rankHandler.add(pairValue);
							for (int y = 0; y < cards.length; y++)
							{
								if (Integer.parseInt((cards[y].substring(1))) != pairValue)
								{
									rankHandler.add(Integer.parseInt((cards[y].substring(1))));
									if (rankHandler.size() == 4)
										return true;
								}
							}
						}
					}
				}
				counter = 1;
			}
			return false;
		}
		
		private boolean isHighCard(String[] _cards)
		{
			String[] cards = new String[_cards.length];
			cards = arrangeMe(_cards);
			rankHandler.clear();
			for (int i = 0; i < 5; i++)
			{
				rankHandler.add(Integer.parseInt(cards[i].substring(1)));
			}
			return true;
		}
		
		/**
		 * Arrange a cards Array from heighest to lowest Rank.
		 * @param _cards
		 * @return Arranged Cards Array
		 */
		private String[] arrangeMe(String[] _cards)
		{
			for (int i = 0; i < _cards.length; i++)
			{
				for (int x = i + 1; x < _cards.length; x++)
				{
					if (Integer.parseInt(String.valueOf(_cards[i]).substring(1)) < Integer.parseInt(String.valueOf(_cards[x]).substring(1)))
					{
						String temp3 = _cards[i];
						_cards[i] = _cards[x];
						_cards[x] = temp3;
					}
				}
			}
			return _cards;
		}
		
		/**
		 * @param fastMap <String playersNames, fastmap<card index, String card = "suit" (1<Spades> or 2(Hearts) or 3(Diamonds) or 4(Clubs)) + "value"> 
		 * @return fastmap <String playersNames, String[] playerHand+onTableCards arranged starting from heighest and ends with lowest Rank>
		 */
		
	}
	
	private class RankManager
	{
		FastMap<String, ArrayList<String>> playersRanks = new FastMap<String, ArrayList<String>>();
		FastMap<String, ArrayList<Integer>> rankedCards = new FastMap<String, ArrayList<Integer>>();
		PokerGame Game;
		CardManager HandManager;
		String eventHandler;
		
		private RankManager(PokerGame _Game, CardManager _HandManager)
		{
			Game = _Game;
			HandManager = _HandManager;
			eventHandler = "";
			rankPlayers();
			
		}
		
		private void rankPlayers()
		{
			
			for (Iterator<L2PcInstance> it = Game.getActivePlayers().iterator(); it.hasNext();)
			{
				L2PcInstance plr = it.next();
				
				if (Game.playersAction.get(plr).equals("Fold"))
					continue;
				
				String rank = "";
				rank = HandManager.getHandValue(plr.getName(), 7);
				
				if (!playersRanks.containsKey(rank))
					playersRanks.put(rank, new ArrayList<String>());
				
				playersRanks.get(rank).add(plr.getName());
				ArrayList<Integer> rankCards = new ArrayList<Integer>();
				rankCards.addAll(HandManager.getRankHandler());
				rankedCards.put(plr.getName(), rankCards);
			}
			_log.info("rankedCards are " + rankedCards.toString());
		}
		
		private ArrayList<String> getPossibleRanks()
		{
			_log.info("getPssibleRanks playersRanks = " + playersRanks.toString());
			if (playersRanks.containsKey("Royal Flush") && !playersRanks.get("Royal Flush").isEmpty())
			{
				eventHandler = "Royal Flush";
				return playersRanks.get("Royal Flush");
			}
			else if (playersRanks.containsKey("Straight Flush") && !playersRanks.get("Straight Flush").isEmpty())
			{
				eventHandler = "Straight Flush";
				return playersRanks.get("Straight Flush");
			}
			else if (playersRanks.containsKey("Four of a Kind") && !playersRanks.get("Four of a Kind").isEmpty())
			{
				eventHandler = "Four of a Kind";
				return playersRanks.get("Four of a Kind");
			}
			else if (playersRanks.containsKey("Full House") && !playersRanks.get("Full House").isEmpty())
			{
				eventHandler = "Full House";
				return playersRanks.get("Full House");
			}
			else if (playersRanks.containsKey("Flush") && !playersRanks.get("Flush").isEmpty())
			{
				eventHandler = "Flush";
				return playersRanks.get("Flush");
			}
			else if (playersRanks.containsKey("Straight") && !playersRanks.get("Straight").isEmpty())
			{
				eventHandler = "Straight";
				return playersRanks.get("Straight");
			}
			else if (playersRanks.containsKey("Three of a Kind") && !playersRanks.get("Three of a Kind").isEmpty())
			{
				eventHandler = "Three of a Kind";
				return playersRanks.get("Three of a Kind");
			}
			else if (playersRanks.containsKey("Two Pair") && !playersRanks.get("Two Pair").isEmpty())
			{
				eventHandler = "Two Pair";
				return playersRanks.get("Two Pair");
			}
			else if (playersRanks.containsKey("Pair") && !playersRanks.get("Pair").isEmpty())
			{
				eventHandler = "Pair";
				return playersRanks.get("Pair");
			}
			else if (playersRanks.containsKey("High Card") && !playersRanks.get("High Card").isEmpty())
			{
				eventHandler = "High Card";
				return playersRanks.get("High Card");
			}
			return null;
		}
		
		private FastList<String> getWinners(String event, ArrayList<String> thisEventPlayers)
		{
			_log.info("getWinners event = " + event + ", list members are" + thisEventPlayers.toString());
			FastList<String> winners = new FastList<String>();
			
			if (event.equals("Royal Flush"))
			{
				for (String name : thisEventPlayers)
				{
					winners.add(name);
				}
			}
			else if (event.equals("Straight Flush"))
			{
				int maxValue = 0;
				//get the max card value
				for (String name : thisEventPlayers)
				{
					int flushTo = rankedCards.get(name).get(0); //get max card rank in the straight flush
					if (maxValue == 0)
					{
						maxValue = flushTo;
					}
					else if (maxValue < flushTo)
					{
						maxValue = flushTo;
					}
				}
				
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0).equals(maxValue))
					{
						winners.add(name);
					}
				}
			}
			else if (event.equals("Four of a Kind"))
			{
				int maxFour = 0;
				int maxKicker = 0;
				//get the max card value
				for (String name : thisEventPlayers)
				{
					int fourOf = rankedCards.get(name).get(0); //get max card rank in the straight flush
					if (maxFour == 0)
					{
						maxFour = fourOf;
					}
					else if (maxFour < fourOf)
					{
						maxFour = fourOf;
					}
				}
				
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxFour)
						continue;
					
					int kicker = rankedCards.get(name).get(1); //get max card rank in the straight flush
					if (maxKicker == 0)
					{
						maxKicker = kicker;
					}
					else if (maxKicker < kicker)
					{
						maxKicker = kicker;
					}
				}
				
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxFour || rankedCards.get(name).get(1) != maxKicker)
						continue;
					winners.add(name);
					
				}
			}
			else if (event.equals("Full House"))
			{
				int maxThree = 0;
				int maxTwo = 0;
				//get the max card value
				for (String name : thisEventPlayers)
				{
					int threeOf = rankedCards.get(name).get(0); //get max card rank in the straight flush
					if (maxThree == 0)
					{
						maxThree = threeOf;
					}
					else if (maxThree < threeOf)
					{
						maxThree = threeOf;
					}
				}
				
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxThree)
						continue;
					
					int pair = rankedCards.get(name).get(1); //get max card rank in the straight flush
					if (maxTwo == 0)
					{
						maxTwo = pair;
					}
					else if (maxTwo < pair)
					{
						maxTwo = pair;
					}
				}
				
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxThree || rankedCards.get(name).get(1) != maxTwo)
						continue;
					winners.add(name);
					
				}
			}
			else if (event.equals("Flush"))
			{
				int maxFlush = 0;
				int maxKicker1 = 0;
				int maxKicker2 = 0;
				int maxKicker3 = 0;
				int maxKicker4 = 0;
				
				for (String name : thisEventPlayers)
				{
					int flushTo = rankedCards.get(name).get(0);
					if (maxFlush == 0)
					{
						maxFlush = flushTo;
					}
					else if (maxFlush < flushTo)
					{
						maxFlush = flushTo;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxFlush)
						continue;
					int kicker1 = rankedCards.get(name).get(1);
					if (maxKicker1 == 0)
					{
						maxKicker1 = kicker1;
					}
					else if (maxKicker1 < kicker1)
					{
						maxKicker1 = kicker1;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxFlush || rankedCards.get(name).get(1) != maxKicker1)
						continue;
					int kicker2 = rankedCards.get(name).get(2);
					if (maxKicker2 == 0)
					{
						maxKicker2 = kicker2;
					}
					else if (maxKicker2 < kicker2)
					{
						maxKicker2 = kicker2;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxFlush || rankedCards.get(name).get(1) != maxKicker1 || rankedCards.get(name).get(2) != maxKicker2)
						continue;
					int kicker3 = rankedCards.get(name).get(3);
					if (maxKicker3 == 0)
					{
						maxKicker3 = kicker3;
					}
					else if (maxKicker3 < kicker3)
					{
						maxKicker3 = kicker3;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxFlush || rankedCards.get(name).get(1) != maxKicker1 || rankedCards.get(name).get(2) != maxKicker2 || rankedCards.get(name).get(3) != maxKicker3)
						continue;
					int kicker4 = rankedCards.get(name).get(4);
					if (maxFlush == 0)
					{
						maxKicker4 = kicker4;
					}
					else if (maxKicker4 < kicker4)
					{
						maxKicker4 = kicker4;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxFlush || rankedCards.get(name).get(1) != maxKicker1 || rankedCards.get(name).get(2) != maxKicker2 || rankedCards.get(name).get(3) != maxKicker3 || rankedCards.get(name).get(4) != maxKicker4)
						continue;
					winners.add(name);
				}
				
			}
			else if (event.equals("Straight"))
			{
				int maxValue = 0;
				//get the max card value
				for (String name : thisEventPlayers)
				{
					int straightTo = rankedCards.get(name).get(0); //get max card rank in the straight
					if (maxValue == 0)
					{
						maxValue = straightTo;
					}
					else if (maxValue < straightTo)
					{
						maxValue = straightTo;
					}
				}
				
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) == maxValue)
					{
						winners.add(name);
					}
				}
			}
			else if (event.equals("Three of a Kind"))
			{
				int maxThree = 0;
				int maxKicker1 = 0;
				int maxKicker2 = 0;
				
				for (String name : thisEventPlayers)
				{
					int threeOf = rankedCards.get(name).get(0);
					if (maxThree == 0)
					{
						maxThree = threeOf;
					}
					else if (maxThree < threeOf)
					{
						maxThree = threeOf;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxThree)
						continue;
					int kicker1 = rankedCards.get(name).get(1);
					if (maxKicker1 == 0)
					{
						maxKicker1 = kicker1;
					}
					else if (maxKicker1 < kicker1)
					{
						maxKicker1 = kicker1;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxThree || rankedCards.get(name).get(1) != maxKicker1)
						continue;
					int kicker2 = rankedCards.get(name).get(2);
					if (maxKicker2 == 0)
					{
						maxKicker2 = kicker2;
					}
					else if (maxKicker2 < kicker2)
					{
						maxKicker2 = kicker2;
					}
				}
				
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxThree || rankedCards.get(name).get(1) != maxKicker1 || rankedCards.get(name).get(2) != maxKicker2)
						continue;
					winners.add(name);
				}
				
			}
			else if (event.equals("Two Pair"))
			{
				int max1stPair = 0;
				int max2ndPair = 0;
				int maxKicker = 0;
				
				for (String name : thisEventPlayers)
				{
					int firstPair = rankedCards.get(name).get(0);
					if (max1stPair == 0)
					{
						max1stPair = firstPair;
					}
					else if (max1stPair < firstPair)
					{
						max1stPair = firstPair;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != max1stPair)
						continue;
					int seconPair = rankedCards.get(name).get(1);
					if (max2ndPair == 0)
					{
						max2ndPair = seconPair;
					}
					else if (max2ndPair < seconPair)
					{
						max2ndPair = seconPair;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != max1stPair || rankedCards.get(name).get(1) != max2ndPair)
						continue;
					int kicker = rankedCards.get(name).get(2);
					if (maxKicker == 0)
					{
						maxKicker = kicker;
					}
					else if (maxKicker < kicker)
					{
						maxKicker = kicker;
					}
				}
				
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != max1stPair || rankedCards.get(name).get(1) != max2ndPair || rankedCards.get(name).get(2) != maxKicker)
						continue;
					winners.add(name);
				}
				
			}
			else if (event.equals("Pair"))
			{
				int maxPair = 0;
				int maxKicker1 = 0;
				int maxKicker2 = 0;
				int maxKicker3 = 0;
				for (String name : thisEventPlayers)
				{
					int pair = rankedCards.get(name).get(0);
					if (maxPair == 0)
					{
						maxPair = pair;
					}
					else if (maxPair < pair)
					{
						maxPair = pair;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxPair)
						continue;
					int kicker1 = rankedCards.get(name).get(1);
					if (maxKicker1 == 0)
					{
						maxKicker1 = kicker1;
					}
					else if (maxKicker1 < kicker1)
					{
						maxKicker1 = kicker1;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxPair || rankedCards.get(name).get(1) != maxKicker1)
						continue;
					int kicker2 = rankedCards.get(name).get(2);
					if (maxKicker2 == 0)
					{
						maxKicker2 = kicker2;
					}
					else if (maxKicker2 < kicker2)
					{
						maxKicker2 = kicker2;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxPair || rankedCards.get(name).get(1) != maxKicker1 || rankedCards.get(name).get(2) != maxKicker2)
						continue;
					int kicker3 = rankedCards.get(name).get(3);
					if (maxKicker3 == 0)
					{
						maxKicker3 = kicker3;
					}
					else if (maxKicker3 < kicker3)
					{
						maxKicker3 = kicker3;
					}
				}
				
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxPair || rankedCards.get(name).get(1) != maxKicker1 || rankedCards.get(name).get(2) != maxKicker2 || rankedCards.get(name).get(3) != maxKicker3)
						continue;
					winners.add(name);
				}
				
			}
			else if (event.equals("High Card"))
			{
				int maxCard = 0;
				int maxKicker1 = 0;
				int maxKicker2 = 0;
				int maxKicker3 = 0;
				int maxKicker4 = 0;
				
				for (String name : thisEventPlayers)
				{
					int cardValue = rankedCards.get(name).get(0);
					if (maxCard == 0)
					{
						maxCard = cardValue;
					}
					else if (maxCard < cardValue)
					{
						maxCard = cardValue;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxCard)
						continue;
					int kicker1 = rankedCards.get(name).get(1);
					if (maxKicker1 == 0)
					{
						maxKicker1 = kicker1;
					}
					else if (maxKicker1 < kicker1)
					{
						maxKicker1 = kicker1;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxCard || rankedCards.get(name).get(1) != maxKicker1)
						continue;
					int kicker2 = rankedCards.get(name).get(2);
					if (maxKicker2 == 0)
					{
						maxKicker2 = kicker2;
					}
					else if (maxKicker2 < kicker2)
					{
						maxKicker2 = kicker2;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxCard || rankedCards.get(name).get(1) != maxKicker1 || rankedCards.get(name).get(2) != maxKicker2)
						continue;
					int kicker3 = rankedCards.get(name).get(3);
					if (maxKicker3 == 0)
					{
						maxKicker3 = kicker3;
					}
					else if (maxKicker3 < kicker3)
					{
						maxKicker3 = kicker3;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxCard || rankedCards.get(name).get(1) != maxKicker1 || rankedCards.get(name).get(2) != maxKicker2 || rankedCards.get(name).get(3) != maxKicker3)
						continue;
					int kicker4 = rankedCards.get(name).get(4);
					if (maxCard == 0)
					{
						maxKicker4 = kicker4;
					}
					else if (maxKicker4 < kicker4)
					{
						maxKicker4 = kicker4;
					}
				}
				for (String name : thisEventPlayers)
				{
					if (rankedCards.get(name).get(0) != maxCard || rankedCards.get(name).get(1) != maxKicker1 || rankedCards.get(name).get(2) != maxKicker2 || rankedCards.get(name).get(3) != maxKicker3 || rankedCards.get(name).get(4) != maxKicker4)
						continue;
					winners.add(name);
				}
			}
			//remove this player from ranks list so he dont get rewarded again
			if (!winners.isEmpty())
				for (String name : winners)
					playersRanks.get(event).remove(playersRanks.get(event).indexOf(name));
			return winners;
		}
	}
	
	private class PotManager
	{
		ArrayList<L2PcInstance> potPlayers = new ArrayList<L2PcInstance>();
		int initValue;
		int value;
		
		private PotManager(int minPotValue)
		{
			initValue = minPotValue;
		}
		
		private PotManager(int _value, ArrayList<L2PcInstance> players)
		{
			initValue = 0;
			value = _value;
			potPlayers.addAll(players);
		}
		
		private ArrayList<L2PcInstance> getPotPlayers()
		{
			return potPlayers;
		}
		
		private int getValue()
		{
			return value;
		}
		
		private void addPlayer(L2PcInstance player)
		{
			potPlayers.add(player);
		}
		
		private void addValue()
		{
			value = value + initValue;
		}
	}
	
}
