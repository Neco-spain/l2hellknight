package l2rt.gameserver.tables;

import gnu.trove.map.hash.TIntObjectHashMap;

import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.model.L2TeleportLocation;

import java.sql.ResultSet;
import java.util.logging.Logger;

/**
 * @author ALF
 */
public class TeleportLocationTable
{
	private static Logger _log = Logger.getLogger(TeleportLocationTable.class.getName());

	private static TeleportLocationTable _instance;

	private TIntObjectHashMap<L2TeleportLocation> _teleports;

	public static TeleportLocationTable getInstance()
	{
		if(_instance == null)
			_instance = new TeleportLocationTable();
		return _instance;
	}

	private TeleportLocationTable()
	{
		_teleports = new TIntObjectHashMap<L2TeleportLocation>();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id, loc_x, loc_y, loc_z, price FROM teleport");
			rset = statement.executeQuery();
			L2TeleportLocation teleport;

			while(rset.next())
			{
				teleport = new L2TeleportLocation();
				
				teleport.setTeleId(rset.getInt("id"));
				teleport.setLocX(rset.getInt("loc_x"));
				teleport.setLocY(rset.getInt("loc_y"));
				teleport.setLocZ(rset.getInt("loc_z"));
				teleport.setPrice(rset.getInt("price"));
				
				_teleports.put(teleport.getTeleId(), teleport);
			}

			_log.config("TeleportLocationTable: Loaded " + _teleports.size() + " Teleport Location Templates.");
		}
		catch(Exception e)
		{
			_log.warning("error while creating TeleportLocationTable " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public L2TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}
	
}
