package l2rt.gameserver.model.entity.vehicle;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.util.Location;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.StringTokenizer;

public class L2VehicleManager
{
	private static final Log _log = LogFactory.getLog(L2VehicleManager.class.getName());

	private static L2VehicleManager _instance;

	public static L2VehicleManager getInstance()
	{
		if(_instance == null)
		{
			_log.info("Initializing L2VehicleManager");
			_instance = new L2VehicleManager();
			_instance.loadShips();
		}
		return _instance;
	}

	private FastMap<Integer, L2Vehicle> _staticItems = new FastMap<Integer, L2Vehicle>().setShared(true);

	public void loadShips()
	{
		if(!Config.ALLOW_BOAT)
			return;

		LineNumberReader lnr = null;
		try
		{
			File vehicleData = new File(Config.DATAPACK_ROOT, "data/vehicle.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(vehicleData)));

			String line = null;
			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0 || line.startsWith("#"))
					continue;
				parseLine(line);
			}

			_log.info("L2VehicleManager: Loaded " + _staticItems.size() + " vehicles.");
		}
		catch(FileNotFoundException e)
		{
			_log.warn("vehicle.csv is missing in data folder");
		}
		catch(Exception e)
		{
			_log.warn("error while creating vehicle table " + e, e);
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception e1)
			{}
		}
	}

	private void parseLine(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		String name = st.nextToken();
		int id = Integer.parseInt(st.nextToken());
		int timer = Integer.parseInt(st.nextToken());

		L2Vehicle ship = name.startsWith("AirShip") ? new L2AirShip(null, name, id) : new L2Ship(name, id);

		Location loc = new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		ship.setHeading(Integer.parseInt(st.nextToken())); // Heading нужно выставлять до локации
		ship.setXYZInvisible(loc);

		int idWaypoint = Integer.parseInt(st.nextToken());
		int idWTicket = Integer.parseInt(st.nextToken());
		Location ret_loc = new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		String[] msgs = new String[] { st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(),
				st.nextToken() };

		ship.SetTrajet1(idWaypoint, idWTicket, ret_loc, msgs);

		idWaypoint = Integer.parseInt(st.nextToken());
		idWTicket = Integer.parseInt(st.nextToken());
		ret_loc = new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		msgs = new String[] { st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(),
				st.nextToken() };

		ship.SetTrajet2(idWaypoint, idWTicket, ret_loc, msgs);

		_staticItems.put(ship.getObjectId(), ship);

		ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(ship), timer * 1000);
	}

	private class Spawn implements Runnable
	{
		L2Vehicle ship;

		public Spawn(L2Vehicle ship)
		{
			this.ship = ship;
		}

		public void run()
		{
			ship.spawn();
		}
	}

	public FastMap<Integer, L2Vehicle> getBoats()
	{
		return _staticItems;
	}

	public L2Vehicle getBoat(int boatObjectId)
	{
		return _staticItems.get(boatObjectId);
	}

	public void addStaticItem(L2Vehicle boat)
	{
		_staticItems.put(boat.getObjectId(), boat);
	}
}