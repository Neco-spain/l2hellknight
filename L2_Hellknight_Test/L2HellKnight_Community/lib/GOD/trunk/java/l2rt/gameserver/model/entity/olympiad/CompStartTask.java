package l2rt.gameserver.model.entity.olympiad;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.cache.Msg;

class CompStartTask implements Runnable
{
	@Override
	public void run()
	{
		if(Olympiad.isOlympiadEnd())
			return;

		Olympiad._manager = new OlympiadManager();
		Olympiad._inCompPeriod = true;

		new Thread(Olympiad._manager).start();

		ThreadPoolManager.getInstance().scheduleGeneral(new CompEndTask(), Olympiad.getMillisToCompEnd());

		Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_STARTED);
		Olympiad._log.info("Olympiad System: Olympiad Game Started");
	}
}