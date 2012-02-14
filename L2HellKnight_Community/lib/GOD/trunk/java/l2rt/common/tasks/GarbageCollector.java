package l2rt.common.tasks;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import java.util.logging.Logger;

public class GarbageCollector
{
	private static Logger _log = Logger.getLogger(GarbageCollector.class.getName());

    static
    {
        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new GarbageCollectorTask(), Config.GARBAGE_COLLECTOR_INTERVAL, Config.GARBAGE_COLLECTOR_INTERVAL);
    }

    static class GarbageCollectorTask implements Runnable
    {
        @Override
        public void run()
        {
            _log.info("GarbageCollector: start");

            System.gc();
            System.runFinalization();

            _log.info("GarbageCollector: finish");
        }
    }
}
