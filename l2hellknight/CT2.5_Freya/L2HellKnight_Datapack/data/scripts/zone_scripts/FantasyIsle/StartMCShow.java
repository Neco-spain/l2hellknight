package zone_scripts.FantasyIsle;

import l2.hellknight.gameserver.instancemanager.QuestManager;

public class StartMCShow implements Runnable
{
	@Override
	public void run()
	{
		QuestManager.getInstance().getQuest("MC_Show").notifyEvent("Start", null, null);
	}
}
