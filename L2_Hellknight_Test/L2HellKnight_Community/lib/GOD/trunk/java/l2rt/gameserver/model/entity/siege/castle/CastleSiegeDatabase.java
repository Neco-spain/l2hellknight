package l2rt.gameserver.model.entity.siege.castle;

import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.entity.siege.SiegeDatabase;

public class CastleSiegeDatabase extends SiegeDatabase
{
	public CastleSiegeDatabase(Siege siege)
	{
		super(siege);
	}

	@Override
	public void saveSiegeDate()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE castle SET siegeDate = ? WHERE id = ?");
			statement.setLong(1, _siege.getSiegeDate().getTimeInMillis() / 1000);
			statement.setInt(2, _siege.getSiegeUnit().getId());
			statement.execute();
		}
		catch(Exception e)
		{
			System.out.println("Exception: saveSiegeDate(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}
}