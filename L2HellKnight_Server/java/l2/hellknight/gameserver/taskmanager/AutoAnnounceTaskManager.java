/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package l2.hellknight.gameserver.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;

import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.util.Broadcast;

/**
 * 
 * @author nBd
 */
public class AutoAnnounceTaskManager
{
	protected static final Logger _log = Logger.getLogger(AutoAnnounceTaskManager.class.getName());
	
	protected List<AutoAnnouncement> _announces = new FastList<AutoAnnouncement>();
	
	private int _nextId = 1;
	
	public static AutoAnnounceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private AutoAnnounceTaskManager()
	{
		restore();
	}
	
	public List<AutoAnnouncement> getAutoAnnouncements()
	{
		return _announces;
	}
	
	public void restore()
	{
		if (!_announces.isEmpty())
		{
			for (AutoAnnouncement a : _announces)
				a.stopAnnounce();
			
			_announces.clear();
		}
		
		Connection conn = null;
		int count = 0;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = conn.prepareStatement("SELECT * FROM auto_announcements");
			ResultSet data = statement.executeQuery();
			while (data.next())
			{
				int id = data.getInt("id");
				long initial = data.getLong("initial");
				long delay = data.getLong("delay");
				int repeat = data.getInt("cycle");
				String memo = data.getString("memo");
				boolean isCritical = Boolean.parseBoolean(data.getString("isCritical"));
				String[] text = memo.split("/n");
				ThreadPoolManager.getInstance().scheduleGeneral(new AutoAnnouncement(id, delay, repeat, text, isCritical), initial);
				count++;
				if (_nextId <= id)
					_nextId = id + 1;
			}
			data.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "AutoAnnoucements: Failed to load announcements data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(conn);
		}
		_log.log(Level.INFO, "AutoAnnoucements: Loaded " + count + " Auto Annoucement Data.");
	}
	
	public void addAutoAnnounce(long initial, long delay, int repeat, String memo, boolean isCritical)
	{
		Connection conn = null;
		
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = conn.prepareStatement("INSERT INTO auto_announcements (id, initial, delay, cycle, memo, isCritical) VALUES (?,?,?,?,?,?)");
			statement.setInt(1, _nextId);
			statement.setLong(2, initial);
			statement.setLong(3, delay);
			statement.setInt(4, repeat);
			statement.setString(5, memo);
			statement.setString(6, String.valueOf(isCritical));
			statement.execute();
			
			statement.close();
			
			String[] text = memo.split("/n");
			ThreadPoolManager.getInstance().scheduleGeneral(new AutoAnnouncement(_nextId++, delay, repeat, text, isCritical), initial);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "AutoAnnoucements: Failed to add announcements data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(conn);
		}
	}
	
	public void deleteAutoAnnounce(int index)
	{
		Connection conn = null;
		
		try
		{
			AutoAnnouncement a = _announces.get(index);
			a.stopAnnounce();
			
			conn = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = conn.prepareStatement("DELETE FROM auto_announcements WHERE id = ?");
			statement.setInt(1, a._id);
			statement.execute();
			
			statement.close();
			
			_announces.remove(index);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "AutoAnnoucements: Failed to delete announcements data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(conn);
		}
	}
	
	public class AutoAnnouncement implements Runnable
	{
		private int _id;
		private long _delay;
		private int _repeat = -1;
		private String[] _memo;
		private boolean _stopped = false;
		private boolean _isCritical;
		
		public AutoAnnouncement(int id, long delay, int repeat, String[] memo, boolean isCritical)
		{
			_id = id;
			_delay = delay;
			_repeat = repeat;
			_memo = memo;
			_isCritical = isCritical;
			if (!_announces.contains(this))
				_announces.add(this);
		}
		
		public String[] getMemo()
		{
			return _memo;
		}
		
		public void stopAnnounce()
		{
			_stopped = true;
		}
		
		public boolean isCritical()
		{
			return _isCritical;
		}
		
		@Override
		public void run()
		{
			if (!_stopped && _repeat != 0)
			{
				for (String text : _memo)
				{
					announce(text, _isCritical);
				}
				
				if (_repeat > 0)
					_repeat--;
				ThreadPoolManager.getInstance().scheduleGeneral(this, _delay);
			}
			else
			{
				stopAnnounce();
			}
		}
	}
	
	public void announce(String text, boolean isCritical)
	{
		Broadcast.announceToOnlinePlayers(text, isCritical);
		_log.info("AutoAnnounce: " + text);
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AutoAnnounceTaskManager _instance = new AutoAnnounceTaskManager();
	}
}
