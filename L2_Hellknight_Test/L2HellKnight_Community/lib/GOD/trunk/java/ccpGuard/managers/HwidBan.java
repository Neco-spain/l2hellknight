package ccpGuard.managers;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import ccpGuard.ProtectInfo;
import l2rt.database.ThreadConnection;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.DatabaseUtils;
import l2rt.gameserver.network.L2GameClient;


public class HwidBan
{
	private static HwidBan _instance;
	private static Map<Integer, L2HwidBan> _lists;

	public static HwidBan getInstance()
	{
		if(_instance == null)
			_instance = new HwidBan();
		return _instance;
	}

	public static void reload()
	{
		_instance = new HwidBan();
	}

	public HwidBan()
	{
		_lists = new HashMap<Integer, L2HwidBan>();
		load();
	}

	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		String HWID = "";
		int counterHwidBan = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM hwid_bans");
			rset = statement.executeQuery();
			while(rset.next())
			{

				HWID = rset.getString("HWID");
				L2HwidBan hb = new L2HwidBan(counterHwidBan);
				hb.setHwidBan(HWID);
				_lists.put(counterHwidBan, hb);
				counterHwidBan++;
			}
		}
		catch(Exception E)
		{}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public static boolean checkFullHWIDBanned(ProtectInfo pi)
	{
		if(_lists.size() == 0)
			return false;
		for(int i = 0; i < _lists.size(); i++)
		{
			if(_lists.get(i).getHwid().equals(pi.getHWID()))
				return true;
		}
		return false;
	}

	public static int getCountHwidBan()
	{
		return _lists.size();
	}

	public static void addHwidBan(L2GameClient client)
	{
		String hwid = client._prot_info.getHWID();
		int counterHwidBan = _lists.size();
		L2HwidBan hb = new L2HwidBan(counterHwidBan);
		hb.setHwidBan(hwid);
		_lists.put(counterHwidBan, hb);
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO hwid_bans SET HWID=?");
			statement.setString(1, hwid);
			statement.execute();
		}
		catch(Exception e)
		{}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

}