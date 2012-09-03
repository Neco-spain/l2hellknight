package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.MyTargetSelected;
import l2rt.gameserver.network.serverpackets.ExShowScreenMessage;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Location;
import l2rt.util.GArray;
import l2rt.util.Rnd;

public class BufferNoobs extends DefaultAI
{


	public BufferNoobs(L2Character actor)
	{
		super(actor);
		this.AI_TASK_DELAY = 1000;
		this.AI_TASK_ACTIVE_DELAY = 1000;
	}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return true;
		L2Skill skill = null;
		
		for(L2Player player : L2World.getAroundPlayers(actor, 200, 200))
		{
			if (player != null && checkBuff(player)) {
				for (int a = 4322; a < 4328; a++) {
					skill = SkillTable.getInstance().getInfo(a, 1);
					GArray<L2Character> target = new GArray<L2Character>();
					target.add(player);
					actor.broadcastPacket(new MagicSkillUse(actor, player, a, 1, 0, 0));
					actor.callSkill(skill, target, true);
				}
				player.sendPacket(new ExShowScreenMessage("Помощник Новичков наделил Вас вспомогательной магией, "+player.getName(), 800));
			}
		}
		return true;
	}
	
	private boolean checkBuff (L2Player player)
	{
		if (player != null && player.getEffectList().getEffectsBySkillId(4322) == null &&
		player.getEffectList().getEffectsBySkillId(4323) == null &&
		player.getEffectList().getEffectsBySkillId(4324) == null &&
		player.getEffectList().getEffectsBySkillId(4325) == null &&
		player.getEffectList().getEffectsBySkillId(4326) == null &&
		player.getEffectList().getEffectsBySkillId(4327) == null &&
		player.getEffectList().getEffectsBySkillId(4328) == null)
			return true;
		else 
			return false;
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}