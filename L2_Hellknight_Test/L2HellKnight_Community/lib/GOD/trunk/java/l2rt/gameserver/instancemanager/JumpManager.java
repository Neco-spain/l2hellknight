package l2rt.gameserver.instancemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javolution.util.FastMap;

import l2rt.Config;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ExFlyMove;
import l2rt.util.GArray;

/**
 * JumpManager
 * @author  ALF
 */
 
public class JumpManager
{
	private static final Logger _log = Logger.getLogger(JumpManager.class.getName());
	private FastMap<Integer, GArray<JumpNode>> _routes;
	
	private JumpManager()
	{
	}
	
	public void load()
	{
		_log.info(getClass().getSimpleName()+": Initializing");
		
		_routes = new FastMap<Integer, GArray<JumpNode>>().setShared(true);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		File file = new File(Config.DATAPACK_ROOT, "data/JumpTrack.xml");
		Document doc = null;
		
		if (file.exists())
		{
			try
			{
				doc = factory.newDocumentBuilder().parse(file);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not parse JumpTrack.xml file: " + e.getMessage(), e);
			}

			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equals("track"))
				{
					GArray<JumpNode> list = new GArray<JumpNode>();
					int trackId = Integer.parseInt(d.getAttributes().getNamedItem("trackId").getNodeValue());
					int maxNum = Integer.parseInt(d.getAttributes().getNamedItem("maxNum").getNodeValue());
					for (Node r = d.getFirstChild(); r != null; r = r.getNextSibling())
					{
						if (r.getNodeName().equals("jumpLoc"))
						{
							NamedNodeMap attrs = r.getAttributes();
							int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							int x = Integer.parseInt(attrs.getNamedItem("x").getNodeValue());
							int y = Integer.parseInt(attrs.getNamedItem("y").getNodeValue());
							int z = Integer.parseInt(attrs.getNamedItem("z").getNodeValue());
							list.add(new JumpNode(id, maxNum, x, y, z));
						}
					}
					_routes.put(trackId, list);
				}
			}
		}
		_log.info(getClass().getSimpleName()+": Loaded " + _routes.size() + " Jump Routes.");
	}
	
	public void StartJump(L2Player player)
	{
		if(!player.isInZone(ZoneType.Jump))
			return;
			
		int _tracId = 1;
		
		int town = TownManager.getInstance().getClosestTownNumber(player);
		switch(town)
		{
			case 1: //Near Talking Island Village
				_tracId = 1; //Talking Island
				break;
		}
		JumpNode n = getRouteForId(_tracId).get(1);
		player.sendPacket(new ExFlyMove(player.getObjectId(), n.getMaxNum(),n.getMoveX(),n.getMoveY(),n.getMoveZ(),n.getRouteId()));
		player.setXYZ(n.getMoveX(), n.getMoveY(), n.getMoveZ());
	}

	
	public void NextJump(L2Player player, int nextId)
	{
		if (nextId < 0) return;
		int _tracId = 1;
		
		int town = TownManager.getInstance().getClosestTownNumber(player);
		switch(town)
		{
			case 1: //Near Talking Island Village
				_tracId = 1; //Talking Island
				break;
		}
		if (getRouteForId(_tracId).get(nextId) == null)
			return;
		JumpNode n = getRouteForId(_tracId).get(nextId);
		player.sendPacket(new ExFlyMove(player.getObjectId(), n.getMaxNum(),n.getMoveX(),n.getMoveY(),n.getMoveZ(),n.getRouteId()));
		player.setXYZ(n.getMoveX(), n.getMoveY(), n.getMoveZ());
	}
	
	public GArray<JumpNode> getRouteForId(int id)
	{
		return _routes.get(id);
	}
	
	public class JumpNode
	{
		private int _id;
		private int _maxNum;
		private int _moveX;
		private int _moveY;
		private int _moveZ;
		
		public JumpNode(int id, int maxNum, int moveX, int moveY, int moveZ)
		{
			super();
			this._id = id;
			this._maxNum = maxNum;
			this._moveX = moveX;
			this._moveY = moveY;
			this._moveZ = moveZ;
		}
		
		public int getRouteId()
		{
			return _id;
		}
		
		public int getMoveX()
		{
			return _moveX;
		}
		
		public int getMoveY()
		{
			return _moveY;
		}
		
		public int getMoveZ()
		{
			return _moveZ;
		}	

		public int getMaxNum()
		{
			return _maxNum;
		}		
	}

	
	public static final JumpManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final JumpManager _instance = new JumpManager();
	}
}