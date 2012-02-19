package vehicles.KeucereusNorthController;

import l2.brick.Config;
import l2.brick.bflmpsvz.a.AirShipController;
import l2.brick.gameserver.model.Location;
import l2.brick.gameserver.model.VehiclePathPoint;

public class KeucereusNorthController extends AirShipController
{
	private static final int DOCK_ZONE = 50602;
	private static final int LOCATION = 100;
	private static final int CONTROLLER_ID = 32606;
	
	private static final VehiclePathPoint[] ARRIVAL =
	{
		new VehiclePathPoint(-183218, 239494, 2500, 280, 2000),
		new VehiclePathPoint(-183218, 239494, 1336, 280, 2000)
	};
	
	private static final VehiclePathPoint[] DEPART =
	{
		new VehiclePathPoint(-183218, 239494, 1700, 280, 2000),
		new VehiclePathPoint(-181974, 235358, 1700, 280, 2000)
	};
	
	private static final VehiclePathPoint[][] TELEPORTS =
	{
		{
			new VehiclePathPoint(-183218, 239494, 1700, 280, 2000),
			new VehiclePathPoint(-181974, 235358, 1700, 280, 2000),
			new VehiclePathPoint(-186373, 234000, 2500, 0, 0)
		},
		{
			new VehiclePathPoint(-183218, 239494, 1700, 280, 2000),
			new VehiclePathPoint(-181974, 235358, 1700, 280, 2000),
			new VehiclePathPoint(-206692, 220997, 3000, 0, 0)
		},
		{
			new VehiclePathPoint(-183218, 239494, 1700, 280, 2000),
			new VehiclePathPoint(-181974, 235358, 1700, 280, 2000),
			new VehiclePathPoint(-235693, 248843, 5100, 0, 0)
		}
	};
	
	private static final int[] FUEL =
	{
		0, 50, 100
	};
	
	public KeucereusNorthController(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(CONTROLLER_ID);
		addFirstTalkId(CONTROLLER_ID);
		addTalkId(CONTROLLER_ID);
		
		_dockZone = DOCK_ZONE;
		addEnterZoneId(DOCK_ZONE);
		addExitZoneId(DOCK_ZONE);
		
		_shipSpawnX = -184145;
		_shipSpawnY = 242373;
		_shipSpawnZ = 3000;
		
		_oustLoc = new Location(-183900, 239384, 1320);
		
		_locationId = LOCATION;
		_arrivalPath = ARRIVAL;
		_departPath = DEPART;
		_teleportsTable = TELEPORTS;
		_fuelTable = FUEL;
		
		_movieId = 1001;
		
		validityCheck();
	}
	
	public static void main(String[] args)
	{
		new KeucereusNorthController(-1, KeucereusNorthController.class.getSimpleName(), "vehicles");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded AirShip: Keucereus North Controller");
	}
}