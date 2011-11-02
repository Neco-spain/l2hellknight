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
package com.l2js.loginserver;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2js.L2DatabaseFactory;

/**
 * @author L0ngh0rn
 */
public class BanList
{
	public enum BanStatus
	{
		BAN_UNSUCCESSFUL, BAN_SUCCESSFUL, ALREADY_BANNED, UNBAN_SUCCESSFUL, UNBAN_UNSUCCESSFUL
	}

	protected Logger _log = Logger.getLogger(BanList.class.getName());
	private static BanList _instance;
	private Map<String, BanInfo> _banlist = new FastMap<String, BanInfo>();

	private BanList()
	{
		load();
	}

	public static BanList getInstance()
	{
		if (_instance == null)
			_instance = new BanList();

		return _instance;
	}

	private void load()
	{
		Connection con = null;
		PreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM banlist");
			ResultSet rset = statement.executeQuery();

			String address;
			long expiration;
			BanInfo info;

			while (rset.next())
			{
				address = rset.getString("address");
				expiration = rset.getLong("expiration");
				info = new BanInfo(address, expiration);

				_banlist.put(address, info);

				_log.info("Banlist: Ban loaded for " + address + " (Expiration: " + expiration + ")");
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Banlist: Failed to initialize ban list");
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	public void reload()
	{
		_banlist.clear();
		load();
	}

	public boolean isAddressBanned(InetAddress address)
	{
		BanInfo ban = _banlist.get(address.getHostAddress());
		String[] parts = address.getHostAddress().split("\\.");

		if (ban == null)
			ban = _banlist.get(parts[0] + "." + parts[1] + "." + parts[2] + ".0");

		if (ban == null)
			ban = _banlist.get(parts[0] + "." + parts[1] + ".0.0");

		if (ban == null)
			ban = _banlist.get(parts[0] + ".0.0.0");

		if (ban != null)
		{
			if (ban.hasExpired())
			{
				_banlist.remove(ban.getAddress());
				ban.deleteMe();

				_log.warning("Banlist: IP " + ban.getAddress() + " has been removed from banlist, expired");
			}
			else
				return true;
		}

		return false;
	}

	public boolean addAddressToBanlist(InetAddress address)
	{
		return addAddressToBanlist(address, 0);
	}

	public boolean addAddressToBanlist(InetAddress address, long expiration)
	{
		BanInfo ban = new BanInfo(address, expiration);

		_banlist.put(address.getHostAddress(), ban);
		_log.warning("Banlist: IP " + ban.getAddress() + " has been banned for " + ban.getExpiration());

		return ban.saveMe();
	}

	public boolean removeAddressFromBanlist(InetAddress address)
	{
		if (!isAddressBanned(address))
			return false;

		BanInfo ban = _banlist.get(address.getHostAddress());
		String[] parts = address.getHostAddress().split("\\.");

		if (ban == null)
			ban = _banlist.get(parts[0] + "." + parts[1] + "." + parts[2] + ".0");

		if (ban == null)
			ban = _banlist.get(parts[0] + "." + parts[1] + ".0.0");

		if (ban == null)
			ban = _banlist.get(parts[0] + ".0.0.0");

		if (ban != null)
		{
			_banlist.remove(ban.getAddress());
			ban.deleteMe();

			_log.warning("Banlist: IP " + ban.getAddress() + " has been removed from banlist");

			return true;
		}

		return false;
	}

	public Map<String, BanInfo> getBannedAddresses()
	{
		return _banlist;
	}

	public class BanInfo
	{
		private long _expiration;
		private String _address;

		public BanInfo(InetAddress address, long expiration)
		{
			_address = address.getHostAddress();
			_expiration = expiration;
		}

		public BanInfo(String address, long expiration)
		{
			_address = address;
			_expiration = expiration;
		}

		public String getAddress()
		{
			return _address;
		}

		public long getExpiration()
		{
			return _expiration;
		}

		public boolean hasExpired()
		{
			return System.currentTimeMillis() > _expiration && _expiration > 0;
		}

		public boolean saveMe()
		{
			Connection con = null;
			PreparedStatement statement = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("INSERT INTO banlist VALUES (?, ?, ?)");
				statement.setString(1, _address);
				statement.setLong(2, _expiration);
				statement.setString(3, "");
				statement.execute();
				statement.close();

				return true;
			}
			catch (Exception e)
			{
				_log.warning("Banlist: Error during saving ban for IP " + _address + " (Expiration: " + _expiration
						+ ")");
			}
			finally
			{
				try
				{
					con.close();
				}
				catch (Exception e)
				{

				}
			}

			return false;
		}

		public boolean deleteMe()
		{
			Connection con = null;
			PreparedStatement statement = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM banlist WHERE address = ?");
				statement.setString(1, _address);
				statement.execute();
				statement.close();

				return true;
			}
			catch (Exception e)
			{
				_log.warning("Banlist: Error during deleting ban for IP " + _address);
			}
			finally
			{
				try
				{
					con.close();
				}
				catch (Exception e)
				{

				}
			}

			return false;
		}
	}
}
