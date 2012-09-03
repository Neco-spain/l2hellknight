package bosses;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.network.serverpackets.ExStartScenePlayer;

/**
 * @ author: Drizzy
 * @ date: 29.01.2011
 * @ Manager for Lindvior. This manager run movie on Keucerus Base each some hours.
 */

public class LindviorManager extends Functions implements ScriptFile
{
	private static L2Zone _zone;

	public void init()
	{
        //Run Movie after start server (1 hour)
        ThreadPoolManager.getInstance().scheduleGeneral(new ShowMovie(), 3600000);
	}

    private class ShowMovie implements Runnable
    {
        public void run()
        {
            _zone = ZoneManager.getInstance().getZoneById(L2Zone.ZoneType.peace_zone, 4602, true);
            if(_zone != null)
                for(L2Player player : _zone.getInsidePlayers())
                    player.showQuestMovie(ExStartScenePlayer.SCENE_LINDVIOR);
            //Run Movie (6 hour)
            ThreadPoolManager.getInstance().scheduleGeneral(new ShowMovie(), 21600000);
        }
    }

	public void onLoad()
	{
		init();
		System.out.println("Lindvior Manager Load.");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}