package l2rt.gameserver.tables;

import l2rt.Config;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.GArray;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

public class TeleportTable
{
	private static Logger _log = Logger.getLogger(TeleportTable.class.getName());
	private static TeleportTable _instance;

	private HashMap<Integer, HashMap<Integer, TeleportLocation[]>> _lists;

	public static TeleportTable getInstance()
	{
		if(_instance == null)
			_instance = new TeleportTable();
		return _instance;
	}

	public static void reload()
	{
		_instance = new TeleportTable();
	}

	private TeleportTable()
	{
		_lists = new HashMap<Integer, HashMap<Integer, TeleportLocation[]>>();

		try
		{
			File file = new File(Config.DATAPACK_ROOT + "/data/teleports.xml");
			DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
			factory1.setValidating(false);
			factory1.setIgnoringComments(true);
			Document doc1 = factory1.newDocumentBuilder().parse(file);

			int counter = 0;
			for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
				if("list".equalsIgnoreCase(n1.getNodeName()))
					for(Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling())
						if("npc".equalsIgnoreCase(d1.getNodeName()))
						{
							HashMap<Integer, TeleportLocation[]> lists = new HashMap<Integer, TeleportLocation[]>();
							for(Node s1 = d1.getFirstChild(); s1 != null; s1 = s1.getNextSibling())
								if("sublist".equalsIgnoreCase(s1.getNodeName()))
								{
									GArray<TeleportLocation> targets = new GArray<TeleportLocation>();
									for(Node t1 = s1.getFirstChild(); t1 != null; t1 = t1.getNextSibling())
										if("target".equalsIgnoreCase(t1.getNodeName()))
										{
											counter++;
											String target = t1.getAttributes().getNamedItem("loc").getNodeValue();
											String name = t1.getAttributes().getNamedItem("name").getNodeValue();
											int price = Integer.parseInt(t1.getAttributes().getNamedItem("price").getNodeValue());
											int item = t1.getAttributes().getNamedItem("item") == null ? 57 : Integer.parseInt(t1.getAttributes().getNamedItem("item").getNodeValue());
											TeleportLocation t = new TeleportLocation(target, item, price, name);
											targets.add(t);
										}
									if(!targets.isEmpty())
										lists.put(Integer.parseInt(s1.getAttributes().getNamedItem("id").getNodeValue()), targets.toArray(new TeleportLocation[targets.size()]));
								}
							if(!lists.isEmpty())
								_lists.put(Integer.parseInt(d1.getAttributes().getNamedItem("id").getNodeValue()), lists);
						}

			_log.config("TeleportController: Loaded " + counter + " locations.");
		}
		catch(Exception e)
		{
			_log.warning("TeleportTable: Lists could not be initialized.");
			e.printStackTrace();
		}
	}

	public TeleportLocation[] getTeleportLocationList(int npcId, int listId)
	{
		if(_lists.get(npcId) == null)
		{
			System.out.println("Not found teleport location for npcId: " + npcId + ", listId: " + listId);
			return null;
		}
		return _lists.get(npcId).get(listId);
	}

	public class TeleportLocation
	{
		public int _price;
		public L2Item _item;
		public String _name;
		public String _target;

		public TeleportLocation(String target, int item, int price, String name)
		{
			_target = target;
			_price = price;
			_name = name;
			_item = ItemTemplates.getInstance().getTemplate(item);
		}
	}
}