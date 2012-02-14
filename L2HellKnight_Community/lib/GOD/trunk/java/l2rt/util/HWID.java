package l2rt.util;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.database.*;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;

import java.sql.ResultSet;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class HWID
{
	private static final Logger _log = Logger.getLogger(HWID.class.getName());
	private static final GArray<HardwareID> banned_hwids = new GArray<HardwareID>();
	private static final GSArray<Entry<HardwareID, FastMap<String, Integer>>> bonus_hwids = new GSArray<Entry<HardwareID, FastMap<String, Integer>>>();

	public static final HWIDComparator DefaultComparator = new HWIDComparator();
	public static final HWIDComparator BAN_Comparator = new HWIDComparator(false);

	public static void reloadBannedHWIDs()
	{
		synchronized (banned_hwids)
		{
			banned_hwids.clear();
		}

		ThreadConnection con = null;
		FiltredPreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("SELECT HWID FROM hwid_bans");
			rs = st.executeQuery();
			synchronized (banned_hwids)
			{
				while(rs.next())
					banned_hwids.add(new HardwareID(rs.getString("HWID")));
			}

			_log.info("[Protection] Loaded " + banned_hwids.size() + " banned HWIDs");
		}
		catch(Exception e)
		{
			_log.info("[Protection] Failed to load banned HWIDs");
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, st, rs);
		}
	}

	public static void reloadBonusHWIDs()
	{
		long now = System.currentTimeMillis() / 1000;
		mysql.set("DELETE FROM `hwid_bonus` WHERE `type`='window' AND UNIX_TIMESTAMP(`time`) < " + (now - 60 * 60 * 24 * Config.SERVICES_WINDOW_DAYS));
		mysql.set("DELETE FROM `hwid_bonus` WHERE `type`='tradeBan' AND `value` > -1 AND `value` < " + now);
		mysql.set("DELETE FROM `hwid_bonus` WHERE `type` LIKE 'newbie%' AND UNIX_TIMESTAMP(`time`) < " + (now - 60 * 60 * 24 * 7));
		synchronized (bonus_hwids)
		{
			bonus_hwids.clear();

			ThreadConnection con = null;
			FiltredPreparedStatement st = null;
			ResultSet rs = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				st = con.prepareStatement("SELECT * FROM hwid_bonus");
				rs = st.executeQuery();
				while(rs.next())
				{
					HardwareID hwid = new HardwareID(rs.getString("HWID"));
					String type = rs.getString("type");
					int value = rs.getInt("value");

					FastMap<String, Integer> oldMap = null;
					for(Entry<HardwareID, FastMap<String, Integer>> entry : bonus_hwids)
						if(entry.getKey().equals(hwid))
						{
							oldMap = entry.getValue();
							break;
						}

					if(oldMap != null)
					{
						Integer oldValue = oldMap.get(type);
						if(oldValue == null || oldValue < value) // XXX: а зачем эта проверка?
							oldMap.put(type, value);
					}
					else
					{
						FastMap<String, Integer> bonus = new FastMap<String, Integer>();
						bonus.put(type, value);
						bonus_hwids.add(new Entry<HardwareID, FastMap<String, Integer>>(hwid, bonus));
					}
				}

				_log.info("[Protection] Loaded " + bonus_hwids.size() + " bonus HWIDs");
			}
			catch(Exception e)
			{
				_log.info("[Protection] Failed to load bonus HWIDs");
				e.printStackTrace();
			}
			finally
			{
				DatabaseUtils.closeDatabaseCSR(con, st, rs);
			}
		}
	}

	public static boolean checkHWIDBanned(HardwareID hwid)
	{
		synchronized (banned_hwids)
		{
			return hwid != null && BAN_Comparator.contains(hwid, banned_hwids);
		}
	}

	public static String handleBanHWID(String[] argv)
	{
		if(!Config.PROTECT_ENABLE || !Config.PROTECT_GS_ENABLE_HWID_BANS)
			return "HWID bans feature disabled";

		if(argv == null || argv.length < 2)
			return "USAGE: banhwid char_name|hwid [kick:true|false] [reason]";

		String hwid = argv[1]; // либо HWID, либо имя чара
		if(hwid.length() != 32)
		{
			L2Player plyr = L2World.getPlayer(hwid);
			if(plyr == null)
				return "Player " + hwid + " not found in world";
			if(!plyr.hasHWID())
				return "Player " + hwid + " not connected (offline trade)";
			hwid = plyr.getHWID().Full;
		}
		boolean kick = argv.length > 2 ? Boolean.parseBoolean(argv[2]) : true;
		String reason = argv.length > 3 ? argv[3] : "";
		BanHWID(hwid, reason, kick);
		return "HWID " + hwid + " banned";
	}

	public static boolean BanHWID(String hwid, String comment)
	{
		return BanHWID(hwid, comment, false);
	}

	public static boolean BanHWID(String hwid, String comment, boolean kick)
	{
		if(!Config.PROTECT_ENABLE || !Config.PROTECT_GS_ENABLE_HWID_BANS || hwid == null || hwid.isEmpty())
			return false;
		return BanHWID(new HardwareID(hwid), comment, kick);
	}

	public static boolean BanHWID(HardwareID hwid, String comment, boolean kick)
	{
		if(!Config.PROTECT_ENABLE || !Config.PROTECT_GS_ENABLE_HWID_BANS || hwid == null)
			return false;

		if(checkHWIDBanned(hwid))
		{
			_log.info("[Protection] HWID: " + hwid + " already banned");
			return true;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement st = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("REPLACE INTO hwid_bans (HWID,comment) VALUES (?,?)");
			st.setString(1, hwid.Full);
			st.setString(2, comment);
			st.execute();

			synchronized (banned_hwids)
			{
				banned_hwids.add(hwid);
			}
			Log.add("Banned HWID: " + hwid, "protect");

			if(kick)
				for(L2Player cha : getPlayersByHWID(hwid))
					cha.logout(false, false, true, true);
		}
		catch(Exception e)
		{
			_log.info("[Protection] Failed to ban HWID: " + hwid);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, st);
		}

		return checkHWIDBanned(hwid);
	}

	public static boolean UnbanHWID(String hwid)
	{
		if(!Config.PROTECT_ENABLE || !Config.PROTECT_GS_ENABLE_HWID_BANS || hwid == null || hwid.isEmpty())
			return false;
		return UnbanHWID(new HardwareID(hwid));
	}

	public static boolean UnbanHWID(HardwareID hwid)
	{
		if(!Config.PROTECT_ENABLE || !Config.PROTECT_GS_ENABLE_HWID_BANS || hwid == null)
			return false;

		if(!checkHWIDBanned(hwid))
		{
			_log.info("[Protection] HWID: " + hwid + " already not banned");
			return true;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement st = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("DELETE FROM hwid_bans WHERE HWID=?");
			st.setString(1, hwid.Full);
			st.execute();

			synchronized (banned_hwids)
			{
				BAN_Comparator.remove(hwid, banned_hwids);
			}
			Log.add("Unbanned HWID: " + hwid, "protect");
		}
		catch(Exception e)
		{
			_log.info("[Protection] Failed to unban HWID: " + hwid);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, st);
		}
		return !checkHWIDBanned(hwid);
	}

	public static GArray<L2Player> getPlayersByHWID(final HardwareID hwid)
	{
		GArray<L2Player> result = new GArray<L2Player>();
		if(hwid != null)
			for(L2Player cha : L2ObjectsStorage.getAllPlayers())
				if(!cha.isInOfflineMode() && cha.getNetConnection() != null && cha.getNetConnection().protect_used && hwid.equals(cha.getHWID()))
					result.add(cha);
		return result;
	}

	public static void setBonus(HardwareID hwid, String type, int value)
	{
		setBonus(hwid, type, value, 0);
	}

	/**
	 * Устанавливает бонус для HWID.
	 */
	public static void setBonus(HardwareID hwid, String type, int value, long timestamp)
	{
		if(!Config.PROTECT_ENABLE || !Config.PROTECT_GS_ENABLE_HWID_BONUS || hwid == null)
			return;

		ThreadConnection con = null;
		FiltredStatement st = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			st = con.createStatement();

			st.executeUpdate("REPLACE INTO hwid_bonus (HWID,type,value,time) VALUES ('" + hwid.Full + "','" + type + "'," + value + "," + (timestamp == 0 ? "CURRENT_TIMESTAMP()" : "FROM_UNIXTIME(" + timestamp + ")") + ")");

			FastMap<String, Integer> oldMap = null;
			synchronized (bonus_hwids)
			{
				for(Entry<HardwareID, FastMap<String, Integer>> entry : bonus_hwids)
					if(entry.getKey().equals(hwid))
					{
						oldMap = entry.getValue();
						break;
					}

				if(oldMap != null)
				{
					Integer oldValue = oldMap.get(type);
					if(oldValue == null || oldValue < value) // XXX: а зачем эта проверка?
						oldMap.put(type, value);
				}
				else
				{
					FastMap<String, Integer> bonus = new FastMap<String, Integer>();
					bonus.put(type, value);
					bonus_hwids.add(new Entry<HardwareID, FastMap<String, Integer>>(hwid, bonus));
				}
			}
		}
		catch(Exception e)
		{
			_log.info("[Protection] Failed to add bonus HWID: " + hwid);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, st);
		}
	}

	/**
	 * Снимает бонус с HWID.
	 */
	public static void unsetBonus(HardwareID hwid, String type)
	{
		if(!Config.PROTECT_ENABLE || !Config.PROTECT_GS_ENABLE_HWID_BONUS || hwid == null)
			return;

		FastMap<String, Integer> bonus = null;
		synchronized (bonus_hwids)
		{
			for(Entry<HardwareID, FastMap<String, Integer>> entry : bonus_hwids)
				if(entry.getKey().equals(hwid))
				{
					bonus = entry.getValue();
					break;
				}
		}
		if(bonus == null)
			return;
		Integer isSet = bonus.get(type);
		if(isSet == null || isSet == 0)
			return;
		bonus.remove(type);

		ThreadConnection con = null;
		FiltredStatement st = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			st = con.createStatement();

			st.executeUpdate("DELETE FROM `hwid_bonus` WHERE `HWID`='" + hwid.Full + "' AND `type`='" + type + "'");
		}
		catch(Exception e)
		{
			_log.info("[Protection] Failed to remove bonus HWID: " + hwid);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, st);
		}
	}

	/**
	 * Возвращает максимальный бонус, имеющийся для всех HWID попадающих под условия стандартного компаратора.
	 */
	public static int getBonus(HardwareID hwid, String type)
	{
		return getBonus(hwid, type, DefaultComparator);
	}

	/**
	 * Возвращает максимальный бонус, имеющийся для всех HWID попадающих под условия компаратора.
	 */
	public static int getBonus(HardwareID hwid, String type, HWIDComparator comparator)
	{
		if(hwid == null)
			return 0;

		GArray<Entry<HardwareID, FastMap<String, Integer>>> possible = getAllMatches(hwid, comparator);
		if(possible.isEmpty())
			return 0;
		int max = 0;
		for(Entry<HardwareID, FastMap<String, Integer>> entry : possible)
			if(entry.getValue() != null && !entry.getValue().isEmpty())
			{
				FastMap<String, Integer> bonuses = entry.getValue();
				Integer value = bonuses.get(type);
				if(value != null)
					if(max == 0)
						max = value;
					else
						max = Math.max(value, max);
			}
		return max;
	}

	/**
	 * Возвращает список всех HWID, кторые попадают под условия компаратора.
	 */
	public static GArray<Entry<HardwareID, FastMap<String, Integer>>> getAllMatches(HardwareID hwid, HWIDComparator comparator)
	{
		GArray<Entry<HardwareID, FastMap<String, Integer>>> ret = new GArray<Entry<HardwareID, FastMap<String, Integer>>>();
		synchronized (bonus_hwids)
		{
			for(Entry<HardwareID, FastMap<String, Integer>> entry : bonus_hwids)
				if(comparator.compare(entry.getKey(), hwid) == HWIDComparator.EQUALS)
					ret.add(entry);
		}
		return ret;
	}

	public static int getBonus(L2Player player, String type, HWIDComparator comparator)
	{
		return player != null && player.hasHWID() ? getBonus(player.getHWID(), type, comparator) : 0;
	}

	public static int getBonus(L2Player player, String type)
	{
		return getBonus(player, type, DefaultComparator);
	}

	public static class HardwareID
	{
		public static final boolean COMPARE_CPU = true;
		public static final boolean COMPARE_BIOS = true;
		public static final boolean COMPARE_HDD = true;
		public static final boolean COMPARE_MAC = true;
		public static final boolean COMPARE_WLInternal = false;

		public final int CPU, WLInternal;
		public final long BIOS, HDD, MAC;
		public final String Full;

		public HardwareID(String s)
		{
			Full = s;
			CPU = Integer.decode("0x" + s.substring(0, 4));
			BIOS = Long.decode("0x" + s.substring(4, 12));
			HDD = Long.decode("0x" + s.substring(12, 20));
			MAC = Long.decode("0x" + s.substring(20, 28));
			WLInternal = Integer.decode("0x" + s.substring(28, 32));
		}

		@Override
		public int hashCode()
		{
			String hwidString = "";
			if(COMPARE_CPU)
				hwidString += Full.substring(0, 4);
			if(COMPARE_BIOS)
				hwidString += Full.substring(4, 12);
			if(COMPARE_HDD)
				hwidString += Full.substring(12, 20);
			if(COMPARE_MAC)
				hwidString += Full.substring(20, 28);
			if(COMPARE_WLInternal)
				hwidString += Full.substring(28, 32);
			return hwidString.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if(obj == null || !(obj instanceof HardwareID))
				return false;

			return DefaultComparator.compare(this, (HardwareID) obj) == HWIDComparator.EQUALS;
		}

		@Override
		public String toString()
		{
			return String.format("%s [CPU: %04x, BIOS: %08x, HDD: %08x, MAC: %08x, WLInternal: %04x]", Full, CPU, BIOS, HDD, MAC, WLInternal);
		}
	}

	public static class HWIDComparator implements Comparator<HardwareID>
	{
		public static final int EQUALS = 0;
		public static final int NOT_EQUALS = 1;

		public boolean COMPARE_CPU = HardwareID.COMPARE_CPU;
		public boolean COMPARE_BIOS = HardwareID.COMPARE_BIOS;
		public boolean COMPARE_HDD = HardwareID.COMPARE_HDD;
		public boolean COMPARE_MAC = HardwareID.COMPARE_MAC;
		public boolean COMPARE_WLInternal = HardwareID.COMPARE_WLInternal;

		public HWIDComparator()
		{}

		public HWIDComparator(boolean _MAC)
		{
			COMPARE_MAC = _MAC;
		}

		public HWIDComparator(boolean _MAC, boolean _CPU)
		{
			COMPARE_MAC = _MAC;
			COMPARE_CPU = _CPU;
		}

		public HWIDComparator(boolean _MAC, boolean _CPU, boolean _BIOS)
		{
			COMPARE_MAC = _MAC;
			COMPARE_CPU = _CPU;
			COMPARE_BIOS = _BIOS;
		}

		public HWIDComparator(boolean _MAC, boolean _CPU, boolean _BIOS, boolean _HDD)
		{
			COMPARE_MAC = _MAC;
			COMPARE_CPU = _CPU;
			COMPARE_HDD = _HDD;
			COMPARE_BIOS = _BIOS;
		}

		public HWIDComparator(boolean _MAC, boolean _CPU, boolean _BIOS, boolean _HDD, boolean _WLInternal)
		{
			COMPARE_MAC = _MAC;
			COMPARE_CPU = _CPU;
			COMPARE_HDD = _HDD;
			COMPARE_BIOS = _BIOS;
			COMPARE_WLInternal = _WLInternal;
		}

		@Override
		public int compare(HardwareID o1, HardwareID o2)
		{
			if(o1 == null || o2 == null)
				return o1 == o2 ? EQUALS : NOT_EQUALS;
			if(COMPARE_CPU && o1.CPU != o2.CPU)
				return NOT_EQUALS;
			if(COMPARE_BIOS && o1.BIOS != o2.BIOS)
				return NOT_EQUALS;
			if(COMPARE_HDD && o1.HDD != o2.HDD)
				return NOT_EQUALS;
			if(COMPARE_MAC && o1.MAC != o2.MAC)
				return NOT_EQUALS;
			if(COMPARE_WLInternal && o1.WLInternal != o2.WLInternal)
				return NOT_EQUALS;
			return EQUALS;
		}

		public int find(HardwareID hwid, List<HardwareID> in)
		{
			for(int i = 0; i < in.size(); i++)
				if(compare(hwid, in.get(i)) == EQUALS)
					return i;
			return -1;
		}

		public boolean contains(HardwareID hwid, List<HardwareID> in)
		{
			return find(hwid, in) != -1;
		}

		public boolean remove(HardwareID hwid, List<HardwareID> in)
		{
			int i = find(hwid, in);
			return i == -1 ? false : in.remove(i) != null;
		}

		public int find(HardwareID hwid, GArray<HardwareID> in)
		{
			for(int i = 0; i < in.size(); i++)
				if(compare(hwid, in.get(i)) == EQUALS)
					return i;
			return -1;
		}

		public boolean contains(HardwareID hwid, GArray<HardwareID> in)
		{
			return find(hwid, in) != -1;
		}

		public boolean remove(HardwareID hwid, GArray<HardwareID> in)
		{
			int i = find(hwid, in);
			return i == -1 ? false : in.remove(i) != null;
		}
	}

	private static final class Entry<K, V> implements Map.Entry<K, V>
	{
		private K _key;
		private V _value;

		public Entry(K key, V value)
		{
			_key = key;
			_value = value;
		}

		@Override
		public K getKey()
		{
			return _key;
		}

		@Override
		public V getValue()
		{
			return _value;
		}

		@Override
		public V setValue(V value)
		{
			return _value = value;
		}
	}
}