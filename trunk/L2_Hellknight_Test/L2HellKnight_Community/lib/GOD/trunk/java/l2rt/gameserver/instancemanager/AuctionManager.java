package l2rt.gameserver.instancemanager;

import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.model.entity.Auction.Auction;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Logger;

public class AuctionManager
{
	protected static Logger _log = Logger.getLogger(AuctionManager.class.getName());

	private static AuctionManager _instance;

	public static AuctionManager getInstance()
	{
		if(_instance == null)
		{
			_log.info("Initializing AuctionManager");
			_instance = new AuctionManager();
			_instance.load();
		}
		return _instance;
	}

	private ArrayList<Auction> _auctions;

	public void reload()
	{
		getAuctions().clear();
		load();
	}

	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id FROM auction ORDER BY id");
			rs = statement.executeQuery();
			while(rs.next())
				getAuctions().add(new Auction(rs.getInt("id")));
			_log.info("Loaded: " + getAuctions().size() + " active auction(s)");
		}
		catch(Exception e)
		{
			System.out.println("Exception: AuctionManager.load(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	public final Auction getAuction(int id)
	{
		for(Auction auction : getAuctions())
			if(auction.getId() == id)
				return auction;
		return null;
	}

	public final ArrayList<Auction> getAuctions()
	{
		if(_auctions == null)
			_auctions = new ArrayList<Auction>();
		return _auctions;
	}
}