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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2js.L2DatabaseFactory;

import javolution.util.FastMap;

/**
 * @author L0ngh0rn
 *
 */
public class ClassBalanceTable
{
	private static Logger _log = Logger.getLogger(ClassBalanceTable.class.getName());
	private FastMap<Integer, ClassBalance> _balance;
	
	private static String QRY_SELECT = "SELECT c.class_id, c.FxH, c.FxL, c.FxR, c.MxH, c.MxL, c.MxR FROM class_balance AS c ORDER BY c.class_id ASC";
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ClassBalanceTable _instance = new ClassBalanceTable();
	}
	
	public static ClassBalanceTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public ClassBalanceTable()
	{
		loadClassBalance();
	}
	
	public void loadClassBalance()
	{
		_balance = new FastMap<Integer, ClassBalance>();
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(QRY_SELECT);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				int class_id = rs.getInt("class_id");
				double fxh = rs.getDouble("FxH");
				double fxl = rs.getDouble("FxL");
				double fxr = rs.getDouble("FxR");
				double mxh = rs.getDouble("MxH");
				double mxl = rs.getDouble("MxL");
				double mxr = rs.getDouble("MxR");
				_balance.put(class_id, new ClassBalance(class_id, new ArmorBalance(fxh, fxl, fxr), new ArmorBalance(mxh, mxl, mxr)));
			}
			ps.close();
			rs.close();
			
			_log.log(Level.INFO, "ClassBalanceTable - (Re)Load: " + _balance.size() +" class.");
		}
		catch (Exception e)
		{
			_log.warning("ClassBalanceTable: Could not load class_balance.");
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public ClassBalance getBalance(int class_id)
	{
		if (_balance.containsKey(class_id))
			return _balance.get(class_id);
		return new ClassBalance();
	}
	
	public double getBalanceValue(TypeBalance type, int class_id)
	{
		switch (type)
		{
			case FxH: return getBalance(class_id).getFight().getHeavy();
			case FxL: return getBalance(class_id).getFight().getLight();
			case FxR: return getBalance(class_id).getFight().getRobe();
			case MxH: return getBalance(class_id).getMage().getHeavy();
			case MxL: return getBalance(class_id).getMage().getLight();
			case MxR: return getBalance(class_id).getMage().getRobe();
			default: return 1D;
		}
	}
	
	public enum TypeBalance
	{
		FxH, FxL, FxR, MxH, MxL, MxR;
	}
	
	public class ClassBalance
	{
		private int class_id;
		private ArmorBalance fight;
		private ArmorBalance mage;
		
		public ClassBalance(int class_id, ArmorBalance fight, ArmorBalance mage)
		{
			this.class_id = class_id;
			this.fight = fight;
			this.mage = mage;
		}
		
		public ClassBalance()
		{
			this.class_id = -1;
			this.fight = new ArmorBalance();
			this.mage = new ArmorBalance();
		}
		
		public int getClass_id()
		{
			return class_id;
		}

		public void setClass_id(int class_id)
		{
			this.class_id = class_id;
		}

		public ArmorBalance getFight()
		{
			return fight;
		}

		public void setFight(ArmorBalance fight)
		{
			this.fight = fight;
		}

		public ArmorBalance getMage()
		{
			return mage;
		}

		public void setMage(ArmorBalance mage)
		{
			this.mage = mage;
		}
	}
	
	public class ArmorBalance
	{
		private double heavy;
		private double light;
		private double robe;
		
		public ArmorBalance(double heavy, double light, double robe)
		{
			this.heavy = heavy;
			this.light = light;
			this.robe = robe;
		}
		
		public ArmorBalance()
		{
			this.heavy = 1.0;
			this.light = 1.0;
			this.robe = 1.0;
		}
		
		public double getHeavy()
		{
			return heavy;
		}

		public void setHeavy(double heavy)
		{
			this.heavy = heavy;
		}

		public double getLight()
		{
			return light;
		}

		public void setLight(double light)
		{
			this.light = light;
		}

		public double getRobe()
		{
			return robe;
		}

		public void setRobe(double robe)
		{
			this.robe = robe;
		}
	}
}
