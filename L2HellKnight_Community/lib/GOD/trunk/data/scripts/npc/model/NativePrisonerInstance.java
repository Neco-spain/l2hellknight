package npc.model;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.instancemanager.HellboundManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.skills.AbnormalEffect;
import l2rt.gameserver.taskmanager.DecayTaskManager;
import l2rt.gameserver.templates.L2NpcTemplate;

import java.util.StringTokenizer;

/**
 * Данный инстанс используется в городе-инстансе на Hellbound
 * @author SYS
 */
public final class NativePrisonerInstance extends L2NpcInstance
{
	public NativePrisonerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onSpawn()
	{
		startAbnormalEffect(AbnormalEffect.HOLD_2);
		super.onSpawn();
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this) || isBusy())
			return;

		StringTokenizer st = new StringTokenizer(command);
		if(st.nextToken().equals("rescue"))
		{
			stopAbnormalEffect(AbnormalEffect.HOLD_2);
			Functions.npcSay(this, "Thank you for saving me! Guards are coming, run!");
			HellboundManager.getInstance().addPoints(20);
			DecayTaskManager.getInstance().addDecayTask(this);
			setBusy(true);
		}
		else
			super.onBypassFeedback(player, command);
	}
}