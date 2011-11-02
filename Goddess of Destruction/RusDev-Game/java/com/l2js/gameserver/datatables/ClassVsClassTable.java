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

import javolution.util.FastMap;

import com.l2js.L2DatabaseFactory;

/**
 * @author L0ngh0rn
 */
public class ClassVsClassTable
{
	protected static Logger _log = Logger.getLogger(ClassVsClassTable.class.getName());
	private static FastMap<Integer, FastMap<Integer, Double>> _classVsClass;

	private static String QRY_CLASS_A = "SELECT c.class_id_01 FROM class_vs_class AS c GROUP BY c.class_id_01 ORDER BY c.class_id_01 ASC";
	private static String QRY_CLASS_B = "SELECT c.class_id_02, c.power FROM class_vs_class AS c WHERE c.class_id_01 = ?";

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ClassVsClassTable _instance = new ClassVsClassTable();
	}

	public static ClassVsClassTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public ClassVsClassTable()
	{
		loadClassVsClass();
	}

	public void loadClassVsClass()
	{
		_classVsClass = new FastMap<Integer, FastMap<Integer, Double>>();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(QRY_CLASS_A);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				final Integer classIdA = rs.getInt("class_id_01");
				final FastMap<Integer, Double> mapClass = new FastMap<Integer, Double>();
				Connection con2 = null;
				try
				{
					con2 = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement ps2 = con.prepareStatement(QRY_CLASS_B);
					ps2.setInt(1, classIdA);
					ResultSet rs2 = ps2.executeQuery();
					while (rs2.next())
						mapClass.put(rs2.getInt("class_id_02"), rs2.getDouble("power"));
					ps2.close();
					rs2.close();
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "ClassVsClassTable: Could not load sql: " + QRY_CLASS_B, e);
				}
				finally
				{
					L2DatabaseFactory.close(con2);
				}

				_classVsClass.put(classIdA, mapClass);
			}
			ps.close();
			rs.close();

			_log.log(Level.INFO, "ClassVsClassTable - (Re)Load: " + _classVsClass.size() + " class.");
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "ClassVsClassTable: Could not load sql: " + QRY_CLASS_A, e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public FastMap<Integer, FastMap<Integer, Double>> getClassVsClass()
	{
		return _classVsClass;
	}

	public FastMap<Integer, Double> getClassValue(Integer classId)
	{
		if (_classVsClass.containsKey(classId))
			return _classVsClass.get(classId);
		return null;
	}

	public Double getPower(Integer classIdA, Integer classIdB)
	{
		if (_classVsClass.containsKey(classIdA))
			if (_classVsClass.get(classIdA).containsKey(classIdB))
				return _classVsClass.get(classIdA).get(classIdB);
		return 1D;
	}
}
