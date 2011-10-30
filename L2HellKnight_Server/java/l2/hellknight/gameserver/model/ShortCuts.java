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
package l2.hellknight.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.ExAutoSoulShot;
import l2.hellknight.gameserver.network.serverpackets.ShortCutInit;
import l2.hellknight.gameserver.network.serverpackets.ShortCutRegister;
import l2.hellknight.gameserver.templates.item.L2EtcItemType;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:33 $
 */
public class ShortCuts
{
	private static Logger _log = Logger.getLogger(ShortCuts.class.getName());
	private static final int MAX_SHORTCUTS_PER_BAR = 12;
	private final L2PcInstance _owner;
	private final Map<Integer, L2ShortCut> _shortCuts = new TreeMap<Integer, L2ShortCut>();
	
	public ShortCuts(L2PcInstance owner)
	{
		_owner = owner;
	}
	
	public L2ShortCut[] getAllShortCuts()
	{
		return _shortCuts.values().toArray(new L2ShortCut[_shortCuts.values().size()]);
	}
	
	public L2ShortCut getShortCut(int slot, int page)
	{
		L2ShortCut sc = _shortCuts.get(slot + (page * MAX_SHORTCUTS_PER_BAR));
		// Verify shortcut
		if ((sc != null) && (sc.getType() == L2ShortCut.TYPE_ITEM))
		{
			if (_owner.getInventory().getItemByObjectId(sc.getId()) == null)
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
				sc = null;
			}
		}
		return sc;
	}
	
	public synchronized void registerShortCut(L2ShortCut shortcut)
	{
		// Verify shortcut
		if (shortcut.getType() == L2ShortCut.TYPE_ITEM)
		{
			final L2ItemInstance item = _owner.getInventory().getItemByObjectId(shortcut.getId());
			if (item == null)
			{
				return;
			}
			if (item.isEtcItem())
			{
				shortcut.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
			}
		}
		final L2ShortCut oldShortCut = _shortCuts.put(shortcut.getSlot() + (shortcut.getPage() * MAX_SHORTCUTS_PER_BAR), shortcut);
		registerShortCutInDb(shortcut, oldShortCut);
	}
	
	private void registerShortCutInDb(L2ShortCut shortcut, L2ShortCut oldShortCut)
	{
		if (oldShortCut != null)
		{
			deleteShortCutFromDb(oldShortCut);
		}
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("REPLACE INTO character_shortcuts (charId,slot,page,type,shortcut_id,level,class_index) values(?,?,?,?,?,?,?)");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, shortcut.getType());
			statement.setInt(5, shortcut.getId());
			statement.setInt(6, shortcut.getLevel());
			statement.setInt(7, _owner.getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store character shortcut: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * @param slot
	 * @param page
	 */
	public synchronized void deleteShortCut(int slot, int page)
	{
		final L2ShortCut old = _shortCuts.remove(slot + (page * MAX_SHORTCUTS_PER_BAR));
		if ((old == null) || (_owner == null))
		{
			return;
		}
		deleteShortCutFromDb(old);
		if (old.getType() == L2ShortCut.TYPE_ITEM)
		{
			L2ItemInstance item = _owner.getInventory().getItemByObjectId(old.getId());
			
			if ((item != null) && (item.getItemType() == L2EtcItemType.SHOT))
			{
				if (_owner.removeAutoSoulShot(item.getItemId()))
				{
					_owner.sendPacket(new ExAutoSoulShot(item.getItemId(), 0));
				}
			}
		}
		
		_owner.sendPacket(new ShortCutInit(_owner));
		
		for (int shotId : _owner.getAutoSoulShot())
		{
			_owner.sendPacket(new ExAutoSoulShot(shotId, 1));
		}
	}
	
	public synchronized void deleteShortCutByObjectId(int objectId)
	{
		for (L2ShortCut shortcut : _shortCuts.values())
		{
			if ((shortcut.getType() == L2ShortCut.TYPE_ITEM) && (shortcut.getId() == objectId))
			{
				deleteShortCut(shortcut.getSlot(), shortcut.getPage());
				break;
			}
		}
	}
	
	/**
	 * @param shortcut
	 */
	private void deleteShortCutFromDb(L2ShortCut shortcut)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=? AND slot=? AND page=? AND class_index=?");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, _owner.getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not delete character shortcut: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void restore()
	{
		_shortCuts.clear();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT charId, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE charId=? AND class_index=?");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, _owner.getClassIndex());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int slot = rset.getInt("slot");
				int page = rset.getInt("page");
				int type = rset.getInt("type");
				int id = rset.getInt("shortcut_id");
				int level = rset.getInt("level");
				
				L2ShortCut sc = new L2ShortCut(slot, page, type, id, level, 1);
				_shortCuts.put(slot + (page * MAX_SHORTCUTS_PER_BAR), sc);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore character shortcuts: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		// Verify shortcuts
		for (L2ShortCut sc : getAllShortCuts())
		{
			if (sc.getType() == L2ShortCut.TYPE_ITEM)
			{
				L2ItemInstance item = _owner.getInventory().getItemByObjectId(sc.getId());
				if (item == null)
				{
					deleteShortCut(sc.getSlot(), sc.getPage());
				}
				else if (item.isEtcItem())
				{
					sc.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
				}
			}
		}
	}
	
	/**
	 * Updates the shortcut bars with the new skill.
	 * @param skillId the skill Id to search and update.
	 * @param skillLevel the skill level to update.
	 */
	public synchronized void updateShortCuts(int skillId, int skillLevel)
	{
		// Update all the shortcuts for this skill
		for (L2ShortCut sc : _shortCuts.values())
		{
			if ((sc.getId() == skillId) && (sc.getType() == L2ShortCut.TYPE_SKILL))
			{
				L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, 1);
				_owner.sendPacket(new ShortCutRegister(newsc));
				_owner.registerShortCut(newsc);
			}
		}
	}
}
