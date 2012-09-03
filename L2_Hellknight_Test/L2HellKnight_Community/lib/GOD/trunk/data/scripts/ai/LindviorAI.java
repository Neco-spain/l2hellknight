package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.network.serverpackets.ExStartScenePlayer;


public class LindviorAI extends Functions implements ScriptFile
{
	private static L2Zone _zone;

	public void init()
	{
        		ThreadPoolManager.getInstance().scheduleGeneral(new ShowMovie(), 3600000); //1 hour after start server
	}

    private class ShowMovie implements Runnable
    {
    public void run()
    {
           		 _zone = ZoneManager.getInstance().getZoneById(L2Zone.ZoneType.peace_zone, 4602, true);
            	if(_zone != null)
                for(L2Player player : _zone.getInsidePlayers())
                    player.showQuestMovie(ExStartScenePlayer.SCENE_LINDVIOR);
            		ThreadPoolManager.getInstance().scheduleGeneral(new ShowMovie(), 21600000);  //6 hour after start server
    }
    	}

	public void onLoad()
	{
		init();
		System.out.println("LindviorShowMovie AI Loaded.");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}