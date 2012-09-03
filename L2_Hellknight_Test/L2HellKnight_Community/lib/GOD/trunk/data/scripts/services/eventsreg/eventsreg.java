package services.eventsreg;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.util.Files;

public class eventsreg extends Functions implements ScriptFile
{
	public String DialogAppend_50011(Integer val)
	{
		return Files.read("data/scripts/services/eventsreg/index.htm");
	}
	
	@Override
	public void onLoad() {
		
	}


	@Override
	public void onReload() {

	}

	@Override
	public void onShutdown() {

	}
	
	
	
}