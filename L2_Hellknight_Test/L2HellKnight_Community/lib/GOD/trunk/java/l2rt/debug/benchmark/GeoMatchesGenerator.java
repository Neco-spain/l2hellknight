package l2rt.debug.benchmark;

import l2rt.Config;
import l2rt.gameserver.geodata.GeoEngine;

public class GeoMatchesGenerator
{
	public static void main(String[] args) throws Exception
	{
		common.init();
		Config.GEOFILES_PATTERN = "(\\d{2}_\\d{2})\\.l2j";
		Config.ALLOW_DOORS = false;
		Config.COMPACT_GEO = false;
		GeoEngine.loadGeo();
		common.log.info("Goedata loaded");
		common.GC();
		GeoEngine.genBlockMatches(0); //TODO
		if(common.YesNoPrompt("Do you want to delete temproary geo checksums files?"))
			GeoEngine.deleteChecksumFiles();
		common.PromptEnterToContinue();
	}
}