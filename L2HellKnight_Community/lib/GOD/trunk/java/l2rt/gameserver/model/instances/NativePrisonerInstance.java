package l2rt.gameserver.model.instances;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.instancemanager.HellboundManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.taskmanager.DecayTaskManager;
import l2rt.gameserver.templates.L2NpcTemplate;

import java.util.StringTokenizer;

public final class NativePrisonerInstance extends L2NpcInstance
{
	public NativePrisonerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public void onSpawn()
	{
		//startAbnormalEffect(2048);
		super.onSpawn();
	}

	public void onBypassFeedback(L2Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command);
		if (st.nextToken().equals("rescue"))
		{
			//stopAbnormalEffect(2048);
			Functions.npcSay(this, "Thank you for saving me! Guards are coming, run!");
			HellboundManager.getInstance().addPoints(20);
			DecayTaskManager.getInstance().addDecayTask(this);
			setBusy(true);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}