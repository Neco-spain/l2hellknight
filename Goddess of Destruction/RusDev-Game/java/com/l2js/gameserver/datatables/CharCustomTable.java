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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2js.Config;
import com.l2js.L2DatabaseFactory;
import com.l2js.gameserver.ThreadPoolManager;
import com.l2js.gameserver.model.L2World;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author L0ngh0rn
 *
 */
public class CharCustomTable
{
	public static enum CustomType
	{
		HERO, NOBLE, DONATOR, COLORNAME, COLORTITLE;
		
		public static CustomType getType(String name)
		{
			try
			{
				return CustomType.valueOf(name.toUpperCase());
			}
			catch (Exception e)
			{
				return null;
			}
		}
	}
	
	public static enum Action
	{
		DELETE, INSERT;
	}
	
	private static Logger _log = Logger.getLogger(CharCustomTable.class.getName());
	private FastMap<CustomType, FastMap<Integer, CharCustomContainer>> _custom;
	
	private int CLEANUP_LOADED = 20 * 60000;
	
	private static String QRY_SELECT = "SELECT cc.type, cc.charId, cc.value, cc.regTime, cc.`time`, (cc.regTime + cc.`time`) valid FROM character_custom AS cc ORDER BY cc.type ASC";
	private static String QRY_DELETE = "DELETE FROM character_custom WHERE type = ? AND charId = ?";
	private static String QRY_INSERT = "REPLACE INTO character_custom(type, charId, value, regTime, `time`) VALUES (?, ?, ?, ?, ?)";
	private static String QRY_LOAD = "SELECT cc.type, cc.charId, cc.value, cc.regTime, cc.`time`, (cc.regTime + cc.`time`) valid FROM character_custom AS cc WHERE cc.charId = ? ORDER BY cc.type ASC";
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CharCustomTable _instance = new CharCustomTable();
	}
	
	public static CharCustomTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public CharCustomTable()
	{
		_custom = new FastMap<CustomType, FastMap<Integer, CharCustomContainer>>();
		for (CustomType t : CustomType.values())
			_custom.put(t, new FastMap<Integer, CharCustomContainer>());
		
		Connection con = null;
		try
		{
			Vector<String> deleteCharIds = new Vector<String>();
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(QRY_SELECT);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				String type = rs.getString("type");
				Integer charId = rs.getInt("charId");
				long time = rs.getLong("time");
				long valid = rs.getLong("valid");
				
				if (!(time == 0) && !(valid > System.currentTimeMillis())) deleteCharIds.add(type + "," + charId);
			}
			ps.close();
			rs.close();
			
			for (String deleteCharId : deleteCharIds)
			{
				String[] value = deleteCharId.split(",");
				delete(value[0], Integer.valueOf(value[1]), false);
			}
			
			_log.log(Level.INFO, "CharCustomTable - Expired/Deleted: " + deleteCharIds.size());
			deleteCharIds.clear();
		}
		catch (Exception e)
		{
			_log.warning("CharCustomTable: Could not load char custom.");
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new AISystem(), CLEANUP_LOADED, CLEANUP_LOADED);
	}
	
	public void add(CustomType type, L2PcInstance activeChar, int value, long regTime, long time)
	{
		Integer charId = activeChar.getObjectId();
		CharCustomContainer container = _custom.get(type).get(charId);
		if (container != null) _custom.get(type).remove(charId);
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement psIns = con.prepareStatement(QRY_INSERT);
			psIns.setString(1, String.valueOf(type));
			psIns.setInt(2, charId);
			psIns.setInt(3, value);
			psIns.setLong(4, regTime);
			psIns.setLong(5, time);
			psIns.executeUpdate();
			psIns.close();
			process(type, Action.INSERT, charId, value, regTime, time);
		}
		catch (Exception e)
		{
			_log.warning("CharCustomTable: Error while add " + charId + " as " + String.valueOf(type) + " to DB!");
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void delete(String type, Integer objId, boolean online)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement psDel = con.prepareStatement(QRY_DELETE);
			psDel.setString(1, type);
			psDel.setInt(2, objId);
			psDel.executeUpdate();
			psDel.close();
			if (online) process(CustomType.getType(type), Action.DELETE, objId);
		}
		catch (Exception e)
		{
			_log.warning("CharCustomTable: Could not delete char custom.");
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	private void process(CustomType type, Action action, Integer objId)
	{
		process(type, action, objId, -1, 0L, 0L);
	}
	
	private void process(CustomType type, Action action, Integer objId, int value, long regTime, long time)
	{
		switch (action)
		{
			case INSERT:
				_custom.get(type).put(objId, new CharCustomContainer(value, regTime, time));
				setCustom(type, action, objId, value);
				break;
			case DELETE:
				_custom.get(type).remove(objId);
				if (type == CustomType.COLORNAME) value = Config.CHARACTER_COLOR_NAME;
				else if (type == CustomType.COLORTITLE) value = Config.CHARACTER_COLOR_TITLE;
				setCustom(type, action, objId, value);
				break;
			default:
				break;
		}
	}
	
	private void setCustom(CustomType type, Action action, Integer objId, int value)
	{
		L2PcInstance activeChar = L2World.getInstance().getPlayer(objId);
		if (activeChar == null) return;
		boolean active = true;
		if (action == Action.DELETE) active = false;
		switch (type)
		{
			case HERO:
				activeChar.setHero(active);
				break;
			case NOBLE:
				activeChar.setNoble(active);
				break;
			case DONATOR:
				activeChar.setDonator(active);
				break;
			case COLORNAME:
				activeChar.getAppearance().setNameColor(value);
				break;
			case COLORTITLE:
				activeChar.getAppearance().setTitleColor(value);
				break;
			default:
				break;
		}
		activeChar.broadcastUserInfo();
	}
	
	private void cleanUp()
	{
		for (CustomType t : CustomType.values())
			for (Integer objId : _custom.get(t).keySet())
			{
				L2PcInstance activeChar = L2World.getInstance().getPlayer(objId);
				if (activeChar == null) process(t, Action.DELETE, objId);
			}
		_log.info("CharCustomTable: CleanUp...");
	}
	
	public void loadMyCustom(L2PcInstance activeChar)
	{
		Integer objId = activeChar.getObjectId();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(QRY_LOAD);
			ps.setInt(1, objId);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				String type = rs.getString("type");
				CustomType ct = CustomType.getType(type);
				Integer charId = rs.getInt("charId");
				int value = rs.getInt("value");
				long regTime = rs.getLong("regTime");
				long time = rs.getLong("time");
				long valid = rs.getLong("valid");
				long currentTime = System.currentTimeMillis();
				if ((time == 0) || (valid > currentTime))
				{
					process(ct, Action.INSERT, charId, value, regTime, time);
					if (time != 0) activeChar.sendMessage("[" + type + "]: Will be removed in " + String.valueOf(((valid) - currentTime) / (86400 * 1000)) + " day(s)!");
				}
				else
				{
					process(ct, Action.DELETE, charId);
					activeChar.sendMessage("[" + type + "]: Was removed!");
				}
			}
			ps.close();
			rs.close();
		}
		catch (Exception e)
		{
			_log.warning("CharCustomTable: Could not load char " + objId + " custom.");
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void destroyMyCustom(L2PcInstance activeChar)
	{
		if (activeChar == null) return;
		Integer objId = activeChar.getObjectId();
		try
		{
			for (CustomType t : CustomType.values())
				if (_custom.containsKey(t))
					if (_custom.get(t).containsKey(objId))
						process(t, Action.DELETE, objId);
		}
		catch (Exception e) { }
	}
	
	public class AISystem implements Runnable
	{
		@Override
		public void run()
		{
			cleanUp();
		}
	}
}
