package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2NpcWalkerNode;

public class NpcWalkerRoutesTable 
{
	private final static Logger _log = Logger.getLogger(SpawnTable.class.getName());

	private static NpcWalkerRoutesTable  _instance;

	private FastList<L2NpcWalkerNode> _routes;

	public static NpcWalkerRoutesTable getInstance()
	{
		if(_instance == null)
		{
				_instance = new NpcWalkerRoutesTable();
				_log.info("Initializing Walkers Routes Table.");
		}
		
		return _instance;
	}

	private NpcWalkerRoutesTable()
	{
	}
	//FIXME: NPE while loading. :S
	public void load()
	{
		 _routes = new FastList<L2NpcWalkerNode>();
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT route_id, npc_id, move_point, chatText, move_x, move_y, move_z, delay, running FROM walker_routes");
			ResultSet rset = statement.executeQuery();
			L2NpcWalkerNode  route;
			while (rset.next())
			{
				route = new L2NpcWalkerNode();
				route.setRouteId(rset.getInt("route_id"));
				route.setNpcId(rset.getInt("npc_id"));
				route.setMovePoint(rset.getString("move_point"));
				route.setChatText(rset.getString("chatText"));
				
				route.setMoveX(rset.getInt("move_x"));
				route.setMoveY(rset.getInt("move_y"));
				route.setMoveZ(rset.getInt("move_z"));
				route.setDelay(rset.getInt("delay"));
				route.setRunning(rset.getBoolean("running"));

			
				_routes.add(route);
			}

			rset.close();
			statement.close();

			_log.info("WalkerRoutesTable: Loaded "+_routes.size()+" Npc Walker Routes.");
			rset.close();
			statement.close();
		}
		catch (Exception e) 
		{
			_log.severe("WalkerRoutesTable: Error while loading Npc Walkers Routes: "+e.getMessage());
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e) {}
		}
	}
	
	public FastList<L2NpcWalkerNode> getRouteForNpc(int id)
	{
		FastList<L2NpcWalkerNode> _return = new FastList<L2NpcWalkerNode>();
		
		 for (FastList.Node<L2NpcWalkerNode> n = _routes.head(), end = _routes.tail(); (n = n.getNext()) != end;) {
	         if(n.getValue().getNpcId() == id)
	         {
	        	 _return.add(n.getValue());
	         }
	     }
		return _return;
		
		
	}
}
