package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2TeleportLocation;

public class TeleportLocationTable
{
	private static Logger _log = Logger.getLogger(TeleportLocationTable.class.getName());

	private static TeleportLocationTable _instance;

	private Map<Integer, L2TeleportLocation> _teleports;

	public static TeleportLocationTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new TeleportLocationTable();
		}
		return _instance;
	}

	private TeleportLocationTable()
	{
	    reloadAll();
	}
	public void reloadAll()
	{
		_teleports = new FastMap<Integer, L2TeleportLocation>();

		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM teleport");
			ResultSet rset = statement.executeQuery();
			L2TeleportLocation teleport;

			while (rset.next())
			{
				teleport = new L2TeleportLocation();

				teleport.setTeleId(rset.getInt("id"));
				teleport.setLocX(rset.getInt("loc_x"));
				teleport.setLocY(rset.getInt("loc_y"));
				teleport.setLocZ(rset.getInt("loc_z"));
				teleport.setPrice(rset.getInt("price"));
				teleport.setIsForNoble(rset.getInt("fornoble")==1);

				_teleports.put(teleport.getTeleId(), teleport);
			}

			rset.close();
			statement.close();

			_log.config("TeleportLocationTable: Loaded " + _teleports.size() + " Teleport Location Templates.");
		}
		catch (Exception e)
		{
			_log.warning("error while creating teleport table "+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}

	public L2TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}
}


/*package net.sf.l2j.gameserver.datatables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2TeleportLocation;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TeleportLocationTable
{
	private static Logger _log = Logger.getLogger(TeleportLocationTable.class.getName());

	private static TeleportLocationTable _instance;

	private Map<Integer, L2TeleportLocation> _teleports;

	public static TeleportLocationTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new TeleportLocationTable();
		}
		return _instance;
	}

	private TeleportLocationTable()
	{
	    reloadAll();
	}
	public void reloadAll()
	{
		_teleports = new FastMap<Integer, L2TeleportLocation>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/teleports.xml");
		if (!f.exists())
		{
			_log.severe("TeleportLocationTable: teleports.xml could not be loaded: file not found");
			return;
		}
		
		try
		{
			InputSource in = new InputSource(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			in.setEncoding("UTF-8");
			Document doc = factory.newDocumentBuilder().parse(in);
			L2TeleportLocation teleport;
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if (n.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if (d.getNodeName().equalsIgnoreCase("teleport"))
						{
							teleport = new L2TeleportLocation();
							int id = Integer.valueOf(d.getAttributes().getNamedItem("id").getNodeValue());
							int loc_x = Integer.valueOf(d.getAttributes().getNamedItem("loc_x").getNodeValue());
							int loc_y = Integer.valueOf(d.getAttributes().getNamedItem("loc_y").getNodeValue());
							int loc_z = Integer.valueOf(d.getAttributes().getNamedItem("loc_z").getNodeValue());
							int price = Integer.valueOf(d.getAttributes().getNamedItem("price").getNodeValue());
							int fornoble = Integer.valueOf(d.getAttributes().getNamedItem("fornoble").getNodeValue());
							
							teleport.setTeleId(id);
							teleport.setLocX(loc_x);
							teleport.setLocY(loc_y);
							teleport.setLocZ(loc_z);
							teleport.setPrice(price);
							teleport.setIsForNoble(fornoble == 1);
	
							_teleports.put(teleport.getTeleId(), teleport);
							teleport = null;
						}
					}
				}
			}
		}
		catch (SAXException e)
		{
			_log.severe("TeleportLocationTable: Error while creating table" + e);
		}
		catch (IOException e)
		{
			_log.severe("TeleportLocationTable: Error while creating table" + e);
		}
		catch (ParserConfigurationException e)
		{
			_log.severe("TeleportLocationTable: Error while creating table" + e);
		}
		
		_log.info("TeleportLocationTable: Loaded " + _teleports.size() + " templates.");
	}
	public L2TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}
}
*/