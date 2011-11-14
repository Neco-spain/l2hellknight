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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2js.Config;
import com.l2js.L2DatabaseFactory;
import com.l2js.gameserver.datatables.ModsBufferSkillTable.BufferSkill;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author L0ngh0rn
 */
public class ModsBufferSchemeTable
{
	private static Logger							_log				= Logger.getLogger(ModsBufferSchemeTable.class
																				.getName());

	private static String							SQL_LIST_SCHEME		= "SELECT mbs.scheme_id, mbs.player_id, mbs.scheme_name, mbs.scheme_type FROM mods_buffer_scheme AS mbs WHERE mbs.player_id = ? ORDER BY mbs.player_id ASC, mbs.scheme_type ASC, mbs.scheme_name ASC";
	private static String							SQL_LIST_CONTENT	= "SELECT DISTINCT mbc.scheme_id, mbc.skill_id, mbc.skill_level FROM mods_buffer_content AS mbc WHERE mbc.scheme_id = ?";
	private static String							SQL_INSERT_SCHEME	= "INSERT INTO mods_buffer_scheme(player_id, scheme_name, scheme_type) VALUES (?, ?, ?)";
	private static String							SQL_INSERT_CONTENT	= "INSERT INTO mods_buffer_content(skill_level, skill_id, scheme_id) VALUES (?, ?, ?)";
	private static String							SQL_GET_ADD_SCHEME	= "SELECT mbs.scheme_id FROM mods_buffer_scheme AS mbs WHERE mbs.player_id = ? AND mbs.scheme_name = ? AND mbs.scheme_type = ?";
	private static String							SQL_DELETE_SKILL	= "DELETE FROM mods_buffer_content WHERE scheme_id = ? AND skill_id = ? and skill_level = ?";

	private List<Integer>							_scheme;
	private FastMap<Integer, List<SchemePlayer>>	_schemePlayer;
	private FastMap<Integer, List<SchemeContent>>	_schemeContent;

	public static ModsBufferSchemeTable getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ModsBufferSchemeTable	_instance	= new ModsBufferSchemeTable();
	}

	public ModsBufferSchemeTable()
	{
		_log.info("ModsBufferScheme System: Initializing...");

		_scheme = new ArrayList<Integer>();
		_schemePlayer = new FastMap<Integer, List<SchemePlayer>>();
		_schemeContent = new FastMap<Integer, List<SchemeContent>>();

		loadSchemePlayer(null);
		loadSchemeContent(null, getSchemePlayer(null));
	}

	public synchronized void loadSchemePlayer(L2PcInstance activeChar)
	{
		int objId = (activeChar != null ? activeChar.getObjectId() : 0);

		List<SchemePlayer> schemes = new ArrayList<SchemePlayer>();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SQL_LIST_SCHEME);
			ps.setInt(1, objId);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				_scheme.add(rs.getInt("scheme_id"));
				schemes.add(new SchemePlayer(rs.getInt("scheme_id"), objId, rs.getString("scheme_name"), rs
						.getShort("scheme_type")));
			}
		}
		catch (Exception e)
		{
			_log.warning("ModsBufferScheme: Could not load Player: ");
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		_schemePlayer.put(objId, schemes);

		if (objId == 0)
			_log.info("ModsBufferScheme: Loaded " + _scheme.size() + " Scheme(s).");
	}

	public synchronized void loadSchemeContent(L2PcInstance activeChar, List<SchemePlayer> schemePlayer)
	{
		int objId = (activeChar != null ? activeChar.getObjectId() : 0);
		boolean isDonator = (objId != 0 ? activeChar.isDonator() : true);
		boolean isGM = (objId != 0 ? activeChar.isGM() : true);
		int contentSize = 0;

		for (SchemePlayer sp : schemePlayer)
		{
			List<SchemeContent> contents = new ArrayList<SchemeContent>();
			int schemeId = sp.getId();
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(SQL_LIST_CONTENT);
				ps.setInt(1, schemeId);
				ResultSet rs = ps.executeQuery();
				while (rs.next())
				{
					int skillId = rs.getInt("skill_id");
					int skillLevel = rs.getInt("skill_level");

					contents.add(new SchemeContent(schemeId, skillId, skillLevel));

					BufferSkill bs = ModsBufferSkillTable.getInstance().getSkillInfo(skillId + "-" + skillLevel);
					if (!isGM && (bs.getDonator() == 2 || (bs.getDonator() == 1 && !isDonator)))
						delContent(schemeId, skillId, skillLevel);
				}
			}
			catch (Exception e)
			{
				_log.warning("ModsBufferScheme: Could not load Content: " + e.getMessage());
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
			contentSize += contents.size();
			_schemeContent.put(schemeId, contents);
		}
		if (objId == 0)
			_log.info("ModsBufferScheme: Loaded " + contentSize + " Content(s).");
	}

	public synchronized void loadMyScheme(L2PcInstance activeChar)
	{
		loadSchemePlayer(activeChar);
		loadSchemeContent(activeChar, getSchemePlayer(activeChar));
	}

	public synchronized void destroyMyScheme(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return;
		try
		{
			for (SchemePlayer sp : getSchemePlayer(activeChar))
			{
				_scheme.remove(_scheme.indexOf(sp.getId()));
				_schemeContent.remove(sp.getId());
			}
			_schemePlayer.remove(activeChar.getObjectId());
		}
		catch (Exception e)
		{
		}
	}

	public synchronized boolean hasScheme(L2PcInstance activeChar)
	{
		int objId = (activeChar == null ? 0 : activeChar.getObjectId());
		try
		{
			return _schemePlayer.get(objId).size() > 0;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public synchronized boolean hasSkillScheme(Integer schemeId)
	{
		return _schemeContent.get(schemeId).size() > 0;
	}

	public synchronized int getCountScheme(L2PcInstance activeChar)
	{
		int objId = (activeChar != null ? activeChar.getObjectId() : 0);
		return (hasScheme(activeChar) ? _schemePlayer.get(objId).size() : 0);
	}

	public synchronized List<SchemeContent> getSchemeContent(Integer schemeId)
	{
		return _schemeContent.get(schemeId);
	}

	public synchronized boolean validSchemeName(L2PcInstance activeChar, String name, short type)
	{
		int objId = (activeChar != null ? activeChar.getObjectId() : 0);
		if (!hasScheme(activeChar))
			return true;
		for (SchemePlayer sp : _schemePlayer.get(objId))
			if (sp.getName().equalsIgnoreCase(name) && sp.getType() == type)
				return false;
		return true;
	}

	public synchronized List<SchemePlayer> getSchemePlayer(L2PcInstance activeChar)
	{
		int objId = (activeChar != null ? activeChar.getObjectId() : 0);
		return _schemePlayer.get(objId);
	}

	public synchronized String getSchemeNamePlayer(L2PcInstance activeChar, int schemeId)
	{
		for (SchemePlayer sp : getSchemePlayer(activeChar))
			if (sp.getId() == schemeId)
				return sp.getName();
		return "";
	}

	public synchronized boolean verifySkillInScheme(L2PcInstance activeChar, int schemeId, int skillId, int skillLevel)
	{
		for (SchemeContent sc : getSchemeContent(schemeId))
			if (sc.getSkillId() == skillId && sc.getSkillLevel() == skillLevel)
				return true;
		return false;
	}

	public synchronized int[] getSchemeCountGroupPlayer(L2PcInstance activeChar, int schemeId)
	{
		int countDanceSong = 0;
		int countOther = 0;
		ModsBufferSkillTable mbsi = ModsBufferSkillTable.getInstance();
		for (SchemeContent sc : getSchemeContent(schemeId))
		{
			BufferSkill bs = mbsi.getSkillInfo(sc.getSkillId() + "-" + sc.getSkillLevel());
			if (bs.getGroup().equalsIgnoreCase("Song") || bs.getGroup().equalsIgnoreCase("Dance"))
				countDanceSong++;
			else
				countOther++;
		}
		return new int[] {
				countDanceSong, countOther
		};
	}

	public boolean payBuffFee(L2PcInstance activeChar, Integer itemId, Long itemNum)
	{
		if (itemId == 0 || itemNum == 0L)
			return true;
		if (activeChar.isGM())
			return true;
		if (activeChar.getInventory().getInventoryItemCount(itemId, -1) < itemNum)
			return false;
		return activeChar.destroyItemByItemId("Buff Manager Fee", itemId, itemNum, activeChar, true);
	}

	public int getAddSchemeId(int objId, String name, int type)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SQL_GET_ADD_SCHEME);
			ps.setInt(1, objId);
			ps.setString(2, name);
			ps.setInt(3, type);
			ResultSet rs = ps.executeQuery();
			rs.next();
			return rs.getInt("scheme_id");

		}
		catch (Exception e)
		{
			return 0;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public synchronized String addScheme(L2PcInstance activeChar, int type, String name)
	{
		if (getCountScheme(activeChar) >= Config.BUFFER_NPC_NUMBER_SCHEME)
			return "You are already using all the schemes available!";
		if (!validSchemeName(activeChar, name, (short) type))
			return "You already have a schema with that name and type!";
		if (!payBuffFee(activeChar, Config.BUFFER_NPC_FEE_SCHEME[0], Long.valueOf(Config.BUFFER_NPC_FEE_SCHEME[1])))
			return "You do not have the necessary item for the purchase of the Scheme!";

		int charId = activeChar.getObjectId();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement psIns = con.prepareStatement(SQL_INSERT_SCHEME);
			psIns.setInt(1, charId);
			psIns.setString(2, name);
			psIns.setInt(3, type);
			psIns.executeUpdate();
			psIns.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "ModsBufferScheme: Error while add " + charId + " as scheme to DB!", e);
			return "Failed to create schema. Please try again later or contact the Administrator!";
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		int schemeId = getAddSchemeId(charId, name, type);

		if (schemeId == 0)
			return "Failed to create schema. Please try again later or contact the Administrator!";

		_scheme.add(schemeId);
		if (!hasScheme(activeChar))
		{
			List<SchemePlayer> newSchemePlayer = new ArrayList<SchemePlayer>();
			newSchemePlayer.add(new SchemePlayer(schemeId, charId, name, (short) type));
			_schemePlayer.put(charId, newSchemePlayer);
		}
		else
			getSchemePlayer(activeChar).add(new SchemePlayer(schemeId, charId, name, (short) type));

		_schemeContent.put(schemeId, new ArrayList<SchemeContent>());

		return "";
	}

	public synchronized String delScheme(L2PcInstance activeChar, int schemeId, int index)
	{
		String[] query = new String[] {
				"DELETE FROM mods_buffer_content WHERE scheme_id = ?",
				"DELETE FROM mods_buffer_scheme WHERE scheme_id = ?"
		};

		for (String sql : query)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement psDel = con.prepareStatement(sql);
				psDel.setInt(1, schemeId);
				psDel.executeUpdate();
				psDel.close();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "ModsBufferScheme: Error while delete " + schemeId + " as " + sql + " to DB!", e);
				return "Failed to delete schema.";
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		if (_schemeContent.containsKey(schemeId))
			_schemeContent.remove(schemeId);
		if (getCountScheme(activeChar) == 1)
		{
			_schemePlayer.remove(activeChar.getObjectId());
			_scheme.remove(_scheme.indexOf(schemeId));
		}
		else
			_schemePlayer.get(activeChar.getObjectId()).remove(index);
		return "Scheme deleted successfully.";
	}

	public synchronized String addContent(int schemeId, int skillId, int skillLevel)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement psIns = con.prepareStatement(SQL_INSERT_CONTENT);
			psIns.setInt(1, skillLevel);
			psIns.setInt(2, skillId);
			psIns.setInt(3, schemeId);
			psIns.executeUpdate();
			psIns.close();

			if (_schemeContent.containsKey(schemeId))
				getSchemeContent(schemeId).add(new SchemeContent(schemeId, skillId, skillLevel));
			else
			{
				_schemeContent.put(schemeId, new ArrayList<SchemeContent>());
				getSchemeContent(schemeId).add(new SchemeContent(schemeId, skillId, skillLevel));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "There was an error adding!";
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return "Added successfully!";
	}

	public synchronized String delContent(int schemeId, int skillId, int skillLevel)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement psDel = con.prepareStatement(SQL_DELETE_SKILL);
			psDel.setInt(1, schemeId);
			psDel.setInt(2, skillId);
			psDel.setInt(3, skillLevel);
			psDel.executeUpdate();
			psDel.close();

			for (SchemeContent sc : getSchemeContent(schemeId))
				if (sc.getId() == schemeId && sc.getSkillId() == skillId && sc.getSkillLevel() == skillLevel)
				{
					getSchemeContent(schemeId).remove(sc);
					break;
				}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Failed to delete skill.", e);
			return "Failed to delete skill.";
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return "Delete successfully!";
	}

	public class SchemePlayer
	{
		private final int		_id;
		private final int		_playerId;
		private final String	_name;
		private final short		_type;

		public SchemePlayer(int id, int player_id, String name, short type)
		{
			_id = id;
			_playerId = player_id;
			_name = name;
			_type = type;
		}

		public int getId()
		{
			return _id;
		}

		public int getPlayerId()
		{
			return _playerId;
		}

		public String getName()
		{
			return _name;
		}

		public short getType()
		{
			return _type;
		}
	}

	public static class SchemeContent
	{
		private final int	_id;
		private final int	_skillId;
		private final int	_skillLevel;

		public SchemeContent(int id, int skillId, int skillLevel)
		{
			_id = id;
			_skillId = skillId;
			_skillLevel = skillLevel;
		}

		public int getId()
		{
			return _id;
		}

		public int getSkillId()
		{
			return _skillId;
		}

		public int getSkillLevel()
		{
			return _skillLevel;
		}
	}
}