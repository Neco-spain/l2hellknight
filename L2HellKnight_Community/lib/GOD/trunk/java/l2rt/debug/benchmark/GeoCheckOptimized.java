package l2rt.debug.benchmark;

import l2rt.Config;
import l2rt.gameserver.geodata.GeoEngine;

public class GeoCheckOptimized
{
	public static void main(String[] args) throws Exception
	{
		common.init();
		Config.GEOFILES_PATTERN = "(\\d{2}_\\d{2})\\.l2j";
		Config.ALLOW_DOORS = false;
		Config.COMPACT_GEO = true;
		GeoEngine.loadGeo();
		common.PromptEnterToContinue();
	}
}