package npc.model;

import java.util.StringTokenizer;

import l2r.gameserver.instancemanager.HellboundManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.templates.npc.NpcTemplate;


/**
 * Данный инстанс используется в городе-инстансе на Hellbound
 * @author SYS
 */
public final class NativePrisonerInstance extends NpcInstance
{
	public NativePrisonerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected void onSpawn()
	{
		startAbnormalEffect(AbnormalEffect.HOLD_2);
		super.onSpawn();
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this) || isBusy())
			return;

		StringTokenizer st = new StringTokenizer(command);
		if(st.nextToken().equals("rescue"))
		{
			stopAbnormalEffect(AbnormalEffect.HOLD_2);
			Functions.npcSay(this, "Thank you for saving me! Guards are coming, run!");
			HellboundManager.addConfidence(15);
			deleteMe();
		}
		else
			super.onBypassFeedback(player, command);
	}
}