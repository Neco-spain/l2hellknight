package ai.talkingisland;

import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.scripts.Functions;

public class Apprentice extends DefaultAI
{
	public Apprentice(NpcInstance actor)
	{
		super(actor);
		AI_TASK_ACTIVE_DELAY = 20000;
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor != null)
		{
			NpcString ns = NpcString.RIDING_KOOKARU;
			Functions.npcSay(actor, ns);
		}
		return false;
	}
}