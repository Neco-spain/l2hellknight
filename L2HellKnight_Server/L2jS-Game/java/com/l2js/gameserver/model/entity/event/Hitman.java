/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js.gameserver.model.entity.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2js.Config;
import com.l2js.L2DatabaseFactory;
import com.l2js.gameserver.Announcements;
import com.l2js.gameserver.ThreadPoolManager;
import com.l2js.gameserver.datatables.CharNameTable;
import com.l2js.gameserver.datatables.ItemTable;
import com.l2js.gameserver.model.L2World;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.model.itemcontainer.PcInventory;
import com.l2js.gameserver.network.SystemMessageId;
import com.l2js.gameserver.network.clientpackets.Say2;
import com.l2js.gameserver.network.serverpackets.SystemMessage;

public class Hitman
{
	protected Logger								_log				= Logger.getLogger(Hitman.class.getName());

	private static Hitman							_instance;
	private FastMap<Integer, PlayerToAssasinate>	_targets;
	private FastMap<String, Integer>				_currency;
	private final DecimalFormat						f					= new DecimalFormat(",##0,000");

	private static String							SQL_SELECT			= "SELECT targetId, clientId, target_name, itemId, bounty, pending_delete FROM hitman_list";
	private static String							SQL_DELETE			= "DELETE FROM hitman_list WHERE targetId = ?";
	private static String							SQL_SAVING			= "REPLACE INTO hitman_list VALUES (?, ?, ?, ?, ?, ?)";
	private static String[]							SQL_OFFLINE			= {
			"SELECT charId, char_name FROM characters WHERE char_name = ?",
			"SELECT charId, char_name FROM characters WHERE charId = ?"
																		};

	private int										MIN_MAX_CLEAN_RATE	= Config.HITMAN_SAVE_TARGET * 60000;

	public static boolean start()
	{
		if (Config.HITMAN_ENABLE_EVENT)
			getInstance();

		return _instance != null;
	}

	public static Hitman getInstance()
	{
		if (_instance == null)
			_instance = new Hitman();

		return _instance;
	}

	public Hitman()
	{
		_targets = load();
		_currency = loadCurrency();
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new AISystem(), MIN_MAX_CLEAN_RATE, MIN_MAX_CLEAN_RATE);
	}

	private FastMap<String, Integer> loadCurrency()
	{
		FastMap<String, Integer> currency = new FastMap<String, Integer>();
		try
		{
			for (Integer itemId : Config.HITMAN_CURRENCY)
				currency.put(getCurrencyName(itemId).trim().replaceAll(" ", "_"), itemId);
		}
		catch (Exception e)
		{
			return new FastMap<String, Integer>();
		}
		return currency;
	}

	public FastMap<String, Integer> getCurrencys()
	{
		return _currency;
	}

	public Integer getCurrencyId(String name)
	{
		return _currency.get(name);
	}

	public String getCurrencyName(Integer itemId)
	{
		return ItemTable.getInstance().getTemplate(itemId).getName();
	}

	private FastMap<Integer, PlayerToAssasinate> load()
	{
		FastMap<Integer, PlayerToAssasinate> map = new FastMap<Integer, PlayerToAssasinate>();
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(SQL_SELECT);
			ResultSet rs = st.executeQuery();

			while (rs.next())
			{
				int targetId = rs.getInt("targetId");
				int clientId = rs.getInt("clientId");
				String target_name = rs.getString("target_name");
				int itemId = rs.getInt("itemId");
				Long bounty = rs.getLong("bounty");
				boolean pending = rs.getInt("pending_delete") == 1;

				if (pending)
					removeTarget(targetId, false);
				else
					map.put(targetId, new PlayerToAssasinate(targetId, clientId, itemId, bounty, target_name));
			}
			_log.info("HitmanEngine[Hitman.load()]: Started - " + map.size() + " Assassination Target(s)");
			rs.close();
			st.close();
			con.close();
		}
		catch (Exception e)
		{
			_log.warning("Hitman: " + e.getCause());
			return new FastMap<Integer, PlayerToAssasinate>();
		}
		return map;
	}

	public void onDeath(L2PcInstance assassin, L2PcInstance target)
	{
		if (_targets.containsKey(target.getObjectId()))
		{
			int assassinClan = assassin.getClanId();
			int assassinAlly = assassin.getAllyId();

			if (!Config.HITMAN_SAME_TEAM)
				if ((assassinClan != 0 && assassinClan == target.getClanId())
						|| (assassinAlly != 0 && assassinAlly == target.getAllyId()))
				{
					assassin.sendChatMessage(assassin.getObjectId(), Say2.TELL, "Agent",
							"You is not considered a Hitman, do not accept betrayal.");
					assassin.sendChatMessage(assassin.getObjectId(), Say2.TELL, "Agent",
							"You never received the reward for it. (Same Clan/Ally)");
					return;
				}

			PlayerToAssasinate pta = _targets.get(target.getObjectId());
			String name = getOfflineData(null, pta.getClientId())[1];
			L2PcInstance client = L2World.getInstance().getPlayer(name);

			target.sendChatMessage(target.getObjectId(), Say2.TELL, assassin.getName(),
					"You were on the target list. I received a reward for murdering you.");

			if (client != null)
			{
				client.sendChatMessage(client.getObjectId(), Say2.TELL, assassin.getName(),
						"Your assassination request to kill " + target.getName() + " has been fulfilled.");
				client.setHitmanTarget(0);
			}

			assassin.sendChatMessage(assassin.getObjectId(), Say2.TELL, (client != null ? client.getName() : "Hitman"),
					"The murder of " + target.getName() + " you will receive your reward.");
			rewardAssassin(assassin, target, pta.getItemId(), pta.getBounty());
			removeTarget(pta.getObjectId(), true);
		}
	}

	private void rewardAssassin(L2PcInstance activeChar, L2PcInstance target, int itemId, Long bounty)
	{
		PcInventory inv = activeChar.getInventory();
		SystemMessage systemMessage;

		if (ItemTable.getInstance().createDummyItem(itemId).isStackable())
		{
			inv.addItem("Hitman", itemId, bounty, activeChar, target);
			if (bounty > 1)
			{
				systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				systemMessage.addItemName(itemId);
				systemMessage.addItemNumber(bounty);
			}
			else
			{
				systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				systemMessage.addItemName(itemId);
			}
			activeChar.sendPacket(systemMessage);
		}
		else
		{
			for (int i = 0; i < bounty; ++i)
			{
				inv.addItem("Hitman", itemId, 1, activeChar, target);
				systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				systemMessage.addItemName(itemId);
				activeChar.sendPacket(systemMessage);
			}
		}
	}

	public void onEnterWorld(L2PcInstance activeChar)
	{
		if (_targets.containsKey(activeChar.getObjectId()))
			activeChar.sendChatMessage(activeChar.getObjectId(), Say2.TELL, "Hitman Info", "Ask their murder. Beware!");

		if (activeChar.getHitmanTarget() > 0)
		{
			if (!_targets.containsKey(activeChar.getHitmanTarget()))
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), Say2.TELL, "Hitman Info",
						"Your target has been eliminated. Have a nice day.");
				activeChar.setHitmanTarget(0);
			}
			else
				activeChar.sendChatMessage(activeChar.getObjectId(), Say2.TELL, "Hitman Info",
						"Your target is still at large.");
		}
	}

	public void save()
	{
		try
		{
			for (PlayerToAssasinate pta : _targets.values())
			{
				Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement st = con.prepareStatement(SQL_SAVING);
				st.setInt(1, pta.getObjectId());
				st.setInt(2, pta.getClientId());
				st.setString(3, pta.getName());
				st.setInt(4, pta.getItemId());
				st.setLong(5, pta.getBounty());
				st.setInt(6, pta.isPendingDelete() ? 1 : 0);
				st.executeQuery();
				st.close();
				con.close();
			}
		}
		catch (Exception e)
		{
			_log.warning("Hitman: " + e);
		}
		System.out.println("Hitman System: Data saved!!");
	}

	public void putHitOn(L2PcInstance client, String playerName, Long bounty, Integer itemId)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(playerName);

		if (client.getHitmanTarget() > 0)
		{
			client.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent",
					"You are already a client here, you can place a request only for a single player.");
			return;
		}
		else if (client.getInventory().getInventoryItemCount(itemId, -1) < bounty)
		{
			client.sendMessage("You does not have this amount of " + ItemTable.getInstance().getTemplate(itemId).getName()
					+ " for the bounty.");
			return;
		}		
		else if ((player != null && client.getName() == playerName))
		{
			client.sendMessage("You cannot bounty yourself! Please choose another target.");
			return;
		}
		else if (player == null && CharNameTable.getInstance().doesCharNameExist(playerName))
		{
			Integer targetId = Integer.parseInt(getOfflineData(playerName, 0)[0]);

			if (_targets.containsKey(targetId))
			{
				client.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent",
						"There is already a bounty on that player.");
				return;
			}
			_targets.put(targetId, new PlayerToAssasinate(targetId, client.getObjectId(), itemId, bounty, playerName));
			client.destroyItemByItemId("Hitman", itemId, bounty, client, true);
			client.setHitmanTarget(targetId);
			if (Config.HITMAN_ANNOUNCE)
				Announcements.getInstance().announceToAll(
						client.getName() + " paid " + (bounty > 999 ? f.format(bounty) : bounty) + " "
								+ getCurrencyName(itemId) + " for those who kill " + playerName + ".");
		}
		else if (player != null && CharNameTable.getInstance().doesCharNameExist(playerName))
		{
			if (_targets.containsKey(player.getObjectId()))
			{
				client.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent",
						"There is already a bounty on that player.");
				return;
			}
			player.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent", "There is a bounty on you.");
			_targets.put(player.getObjectId(), new PlayerToAssasinate(player, client.getObjectId(), itemId, bounty));
			client.destroyItemByItemId("Hitman", itemId, bounty, client, true);
			client.setHitmanTarget(player.getObjectId());
			if (Config.HITMAN_ANNOUNCE)
				Announcements.getInstance().announceToAll(
						client.getName() + " paid " + (bounty > 999 ? f.format(bounty) : bounty) + " "
								+ getCurrencyName(itemId) + " for those who kill " + playerName + ".");
		}
		else
			client.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent",
					"Player name invalid. The user you added does not exist.");
	}

	public class AISystem implements Runnable
	{
		@Override
		public void run()
		{
			if (Config.DEBUG)
				_log.info("Cleaning sequance initiated.");
			for (PlayerToAssasinate target : _targets.values())
				if (target.isPendingDelete())
					removeTarget(target.getObjectId(), true);
			save();
		}
	}

	public void removeTarget(int obId, boolean live)
	{
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(SQL_DELETE);
			st.setInt(1, obId);
			st.execute();
			st.close();
			con.close();

			if (live)
				_targets.remove(obId);
		}
		catch (Exception e)
		{
			_log.warning("Hitman: " + e);
		}
	}

	public void cancelAssasination(String name, L2PcInstance client)
	{
		L2PcInstance target = L2World.getInstance().getPlayer(name);

		if (client.getHitmanTarget() <= 0)
		{
			client.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent", "You don't own a hit.");
			return;
		}
		else if (target == null && CharNameTable.getInstance().doesCharNameExist(name))
		{
			PlayerToAssasinate pta = _targets.get(client.getHitmanTarget());

			if (!_targets.containsKey(pta.getObjectId()))
				client.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent", "There is no hit on that player.");
			else if (pta.getClientId() == client.getObjectId())
			{
				removeTarget(pta.getObjectId(), true);
				client.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent", "The hit has been canceled.");
				client.setHitmanTarget(0);
			}
			else
				client.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent",
						"You are not the actual owner of that target!");
		}
		else if (target != null && CharNameTable.getInstance().doesCharNameExist(name))
		{
			PlayerToAssasinate pta = _targets.get(target.getObjectId());

			if (!_targets.containsKey(pta.getObjectId()))
				client.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent", "There is no hit on that player.");
			else if (pta.getClientId() == client.getObjectId())
			{
				removeTarget(pta.getObjectId(), true);
				client.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent", "The hit has been canceled.");
				target.sendChatMessage(target.getObjectId(), Say2.TELL, "Agent", "The hit on you has been canceled.");
				client.setHitmanTarget(0);
			}
			else
				client.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent",
						"You are not the actual owner of that target");
		}
		else
			client.sendChatMessage(client.getObjectId(), Say2.TELL, "Agent",
					"Player name invalid. The user u added dose not exist.");
	}

	public String[] getOfflineData(String name, int objId)
	{
		String[] set = new String[2];
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(objId > 0 ? SQL_OFFLINE[1] : SQL_OFFLINE[0]);

			if (objId > 0)
				st.setInt(1, objId);
			else
				st.setString(1, name);

			ResultSet rs = st.executeQuery();

			while (rs.next())
			{
				set[0] = String.valueOf(rs.getInt("charId"));
				set[1] = rs.getString("char_name");
			}

			rs.close();
			st.close();
			con.close();
		}
		catch (Exception e)
		{
			_log.warning("Hitman: " + e);
		}
		return set;
	}

	public boolean exists(int objId)
	{
		return _targets.containsKey(objId);
	}

	public PlayerToAssasinate getTarget(int objId)
	{
		return _targets.get(objId);
	}

	public FastMap<Integer, PlayerToAssasinate> getTargets()
	{
		return _targets;
	}

	public FastMap<Integer, PlayerToAssasinate> getTargetsOnline()
	{
		FastMap<Integer, PlayerToAssasinate> online = new FastMap<Integer, PlayerToAssasinate>();

		for (Integer objId : _targets.keySet())
		{
			PlayerToAssasinate pta = _targets.get(objId);
			if (pta.isOnline() && !pta.isPendingDelete())
				online.put(objId, pta);
		}
		return online;
	}

	public void set_targets(FastMap<Integer, PlayerToAssasinate> targets)
	{
		_targets = targets;
	}
}
