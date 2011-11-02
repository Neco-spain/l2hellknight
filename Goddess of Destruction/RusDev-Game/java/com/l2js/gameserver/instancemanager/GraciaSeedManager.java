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
package com.l2js.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.logging.Logger;

import com.l2js.Config;
import com.l2js.L2DatabaseFactory;

/**
 * @author L0ngh0rn
 */
public class GraciaSeedManager
{
	private static final Logger _log = Logger.getLogger(GraciaSeedManager.class.getName());
	
	private static final String LOAD_VAR = "SELECT var,value FROM gracia_seeds_data";
	private static final String SAVE_VAR = "INSERT INTO gracia_seeds_data (var,value) VALUES (?,?) " + "ON DUPLICATE KEY UPDATE value=?";
	
	private static int _SoDTiatKilled = 0;
	private static int _SoDState = 1;
	private static Calendar _SoDLastStateChangeDate;
	
	private GraciaSeedManager()
	{
		_SoDLastStateChangeDate = Calendar.getInstance();
		loadData();
		handleSodStages();
	}
	
	public void saveData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SAVE_VAR);
			
			statement.setString(1, "SoDTiatKilled");
			statement.setInt(2, _SoDTiatKilled);
			statement.setInt(3, _SoDTiatKilled);
			statement.execute();
			
			statement.setString(1, "SoDState");
			statement.setInt(2, _SoDState);
			statement.setInt(3, _SoDState);
			statement.execute();
			
			statement.setString(1, "SoDLastStateChangeDate");
			statement.setLong(2, _SoDLastStateChangeDate.getTimeInMillis());
			statement.setLong(3, _SoDLastStateChangeDate.getTimeInMillis());
			statement.execute();
		}
		catch (Exception e)
		{
			_log.warning("GraciaSeedManager: problem while saving variables: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void loadData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset;
		String var, value;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(LOAD_VAR);
			
			rset = statement.executeQuery();
			while (rset.next())
			{
				var = rset.getString(1);
				value = rset.getString(2);
				
				if ("SoDTiatKilled".equalsIgnoreCase(var))
					_SoDTiatKilled = Integer.parseInt(value);
				
				else if ("SoDState".equalsIgnoreCase(var))
					_SoDState = Integer.parseInt(value);
				
				else if ("SoDLastStateChangeDate".equalsIgnoreCase(var))
					_SoDLastStateChangeDate.setTimeInMillis(Long.parseLong(value));
			}
		}
		catch (Exception e)
		{
			_log.warning("GraciaSeedManager: problem while loading variables: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	private void handleSodStages()
	{
		switch (_SoDState)
		{
			case 1:
				break;
			case 2:
				long timePast = System.currentTimeMillis() - _SoDLastStateChangeDate.getTimeInMillis();
				if (timePast >= Config.SOD_STAGE_2_LENGTH)
					setSoDState(3, true);
				break;
			case 3:
				break;
			default:
				_log.warning("GraciaSeedManager: Unknown Seed of Destruction state(" + _SoDState + ")! ");
		}
	}
	
	public void increaseSoDTiatKilled()
	{
		if (_SoDState == 1)
		{
			_SoDTiatKilled++;
			if (_SoDTiatKilled >= Config.SOD_TIAT_KILL_COUNT)
				setSoDState(2, false);
			saveData();
		}
	}
	
	public int getSoDTiatKilled()
	{
		return _SoDTiatKilled;
	}
	
	public void setSoDState(int value, boolean doSave)
	{
		_SoDLastStateChangeDate.setTimeInMillis(System.currentTimeMillis());
		_SoDState = value;
		
		if (_SoDState == 1)
			_SoDTiatKilled = 0;
		if (doSave)
			saveData();
	}
	
	public long getSoDTimeForNextStateChange()
	{
		switch (_SoDState)
		{
			case 1:
				return -1;
			case 2:
				return (_SoDLastStateChangeDate.getTimeInMillis() + Config.SOD_STAGE_2_LENGTH - System.currentTimeMillis());
			case 3:
				return -1;
			default:
				return -1;
		}
	}
	
	public Calendar getSoDLastStateChangeDate()
	{
		return _SoDLastStateChangeDate;
	}
	
	public int getSoDState()
	{
		return _SoDState;
	}
	
	public static final GraciaSeedManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final GraciaSeedManager _instance = new GraciaSeedManager();
	}
}
