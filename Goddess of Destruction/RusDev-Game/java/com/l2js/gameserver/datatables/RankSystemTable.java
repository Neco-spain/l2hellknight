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
package com.l2js.gameserver.datatables;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2js.Config;
import com.l2js.L2DatabaseFactory;
import com.l2js.gameserver.ThreadPoolManager;

/**
 * @author L0ngh0rn
 *
 */
public class RankSystemTable
{
	private Long MIN_MAX_CLEAN_RATE = Config.RANK_NPC_RELOAD * 60000;
	private Long lastReload;
	
	private static Logger _log = Logger.getLogger(RankSystemTable.class.getName());
	
	public static final int ITEM = 1;
	public static final int OLYMPIAD = 2;
	public static final int PVP = 3;
	public static final int PK = 4;
	
	private String QRY_SELECT_ITEMS = "SELECT c.charId, c.char_name, Sum(i.count) AS total " +
			"FROM items AS i Inner Join characters AS c ON i.owner_id = c.charId " +
			"WHERE c.accesslevel = 0 AND i.item_id = ? " +
			"GROUP BY c.charId " +
			"ORDER BY total DESC LIMIT 0, ?";
	
	private String QRY_SELECT_OLYMPIAD = "SELECT c.charId, c.char_name, o.olympiad_points AS total " +
			"FROM olympiad_nobles AS o Inner Join characters AS c ON o.charId = c.charId " +
			"WHERE c.accesslevel = 0 AND o.class_id = ? AND o.competitions_done >= 1 " +
			"ORDER BY o.olympiad_points DESC LIMIT 0, ?";
	
	private String QRY_SELECT_PVPPK = "SELECT c.charId, c.char_name, c.%column% AS total " +
			"FROM characters AS c " +
			"WHERE c.accesslevel = 0 AND c.%column% > 0  " +
			"ORDER BY c.%column% DESC LIMIT 0, ?";
	
	private Map<Integer, Map<Integer, TreeSet<RankInformation>>> _rank;
	
	public static RankSystemTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public RankSystemTable()
	{
		_log.info("Rank System: Initializing...");
		loagRankSystem();
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ReSync(), MIN_MAX_CLEAN_RATE, MIN_MAX_CLEAN_RATE);
	}
	
	public TreeSet<RankInformation> getRankType(int type, int id)
	{
		if (_rank.containsKey(type) && _rank.get(type).containsKey(id))
				return _rank.get(type).get(id);
		return new TreeSet<RankInformation>();
	}
	
	public TreeSet<RankInformation> getRankInformatioValues(int type, Integer value)
	{
		return getRankType(type, value);
	}
	
	public int getRankAmountValues(int type, Integer value)
	{
		return getRankInformatioValues(type, value).size();
	}
	
	private void loagRankSystem()
	{
		try
		{
			_rank = new FastMap<Integer, Map<Integer, TreeSet<RankInformation>>>();
			_rank.put(ITEM, createRankValues(ITEM, Config.RANK_NPC_LIST_ITEM));
			_rank.put(OLYMPIAD, createRankValues(OLYMPIAD, Config.RANK_NPC_LIST_CLASS));
			_rank.put(PVP, createRankValues(PVP, new int[]{0}));
			_rank.put(PK, createRankValues(PK, new int[]{1}));			
		}
		catch (Exception e) {
			_log.warning("Failed to load the Rank System: " + e.getMessage());
		}
		setLastReload(System.currentTimeMillis());
	}
	
	private void setLastReload(Long time)
	{
		lastReload = time;
	}
	
	public Long getLastReload()
	{
		return lastReload;
	}
	
	public Long getNextReload()
	{
		return lastReload + MIN_MAX_CLEAN_RATE;
	}
	
	public Long getNextUpdatingTime()
	{
		return getNextReload() - System.currentTimeMillis();
	}
	
	private Map<Integer, TreeSet<RankInformation>> createRankValues(int type, int[] rANK_NPC_LIST_ITEM)
	{
		int count = 0;
		Map<Integer, TreeSet<RankInformation>> valuesResult = new FastMap<Integer, TreeSet<RankInformation>>();
		for (Integer value : rANK_NPC_LIST_ITEM)
		{
			TreeSet<RankInformation> listValue;
			switch (type)
			{
				case ITEM:
					listValue = createRank(type, value, QRY_SELECT_ITEMS, Config.RANK_NPC_ITEMS_RECORDS);
					break;
				case OLYMPIAD:
					listValue = createRank(type, value, QRY_SELECT_OLYMPIAD, Config.RANK_NPC_OLY_RECORDS);
					break;
				case PVP:
					listValue = createRank(type, value, QRY_SELECT_PVPPK.replaceAll("%column%", "pvpkills"), Config.RANK_NPC_PVP_RECORDS);
					break;
				case PK:
					listValue = createRank(type, value, QRY_SELECT_PVPPK.replaceAll("%column%", "pkkills"), Config.RANK_NPC_PK_RECORDS);
					break;
				default:
					listValue = null;
					break;
			}
			
			if (listValue == null || listValue.isEmpty()) continue;
			
			valuesResult.put(value, listValue);
			count += listValue.size();
		}
		
		_log.info("Rank System (" + getTypeName(type) + ") Loaded: " + String.valueOf(valuesResult.size()) + "(" + String.valueOf(count)  + ")");
		return valuesResult;
	}
	
	private String getTypeName(int type)
	{
		switch (type)
		{
			case ITEM: return "Items";
			case OLYMPIAD: return "Olympiad";
			case PVP: return "PvP";
			case PK: return "PK";
			default: return "null";
		}
	}
	
	private TreeSet<RankInformation> createRank(int type, Integer value, String query, Integer records)
	{
		Connection con = null;
		TreeSet<RankInformation> list = new TreeSet<RankInformation>(new AmountComparator());
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query);
			ParameterMetaData p = statement.getParameterMetaData();
			if (p.getParameterCount() == 1)
				statement.setInt(1, records);
			else		
			{
				statement.setInt(1, value);
				statement.setInt(2, records);
			}
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
				list.add(new RankInformation(rset.getInt("charId"), rset.getString("char_name"), value, rset.getLong("total")));
		}
		catch (Exception e) {
			_log.warning("Could not load Rank System (" + getTypeName(type) + "): " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return list;
	}
	
	private static class AmountComparator implements Comparator<RankInformation>
	{
		@Override
		public int compare(RankInformation key1, RankInformation key2)
		{
			int c1 = key2.getAmount().compareTo(key1.getAmount());
			int c2 = key1.getPlayerName().compareTo(key2.getPlayerName());
			if (c1 == 0) return c2;	
			return c1;
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final RankSystemTable _instance = new RankSystemTable();
	}
	
	public class ReSync implements Runnable
	{
		@Override
		public void run()
		{
			if (Config.DEBUG) _log.info("Cleaning sequance initiated.");
			loagRankSystem();
		}
	}
	
	public class RankInformation
	{
		private Integer _objectId;
		private String _playerName;
		private Integer _typeId;
		private Long _amount;
		
		/**
		 * @param objectId
		 * @param playerName
		 * @param typeId
		 * @param amount
		 */
		public RankInformation(Integer objectId, String playerName, Integer typeId, Long amount)
		{
			this._objectId = objectId;
			this._playerName = playerName;
			this._typeId = typeId;
			this._amount = amount;
		}

		/**
		 * @return the _objectId
		 */
		public Integer getObjectId()
		{
			return _objectId;
		}

		/**
		 * @return the _playerName
		 */
		public String getPlayerName()
		{
			return _playerName;
		}

		/**
		 * @return the _typeId
		 */
		public Integer getTypeId()
		{
			return _typeId;
		}

		/**
		 * @return the _amount
		 */
		public Long getAmount()
		{
			return _amount;
		}	
	}
}
