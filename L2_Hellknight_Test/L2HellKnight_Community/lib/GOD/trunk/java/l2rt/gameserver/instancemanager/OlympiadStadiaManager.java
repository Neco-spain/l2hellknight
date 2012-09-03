package l2rt.gameserver.instancemanager;

import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.entity.olympiad.OlympiadStadia;
import l2rt.util.GArray;

import java.util.logging.Logger;

public class OlympiadStadiaManager
{
	protected static Logger _log = Logger.getLogger(OlympiadStadiaManager.class.getName());

	private static OlympiadStadiaManager _instance;

	public static OlympiadStadiaManager getInstance()
	{
		if(_instance == null)
		{
			System.out.println("Initializing OlympiadStadiaManager");
			_instance = new OlympiadStadiaManager();
			_instance.load();
		}
		return _instance;
	}

	private GArray<OlympiadStadia> _olympiadStadias;

	public void reload()
	{
		getOlympiadStadias().clear();
		load();
	}

	private void load()
	{
		GArray<L2Zone> zones = ZoneManager.getInstance().getZoneByType(ZoneType.OlympiadStadia);
		if(zones.size() == 0)
			System.out.println("Not found zones for Olympiad!!!");
		else
			for(L2Zone zone : zones)
				getOlympiadStadias().add(new OlympiadStadia(zone.getId()));
	}

	public final GArray<OlympiadStadia> getOlympiadStadias()
	{
		if(_olympiadStadias == null)
			_olympiadStadias = new GArray<OlympiadStadia>();
		return _olympiadStadias;
	}
}
