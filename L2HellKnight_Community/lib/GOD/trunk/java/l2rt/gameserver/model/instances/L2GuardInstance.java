package l2rt.gameserver.model.instances;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.templates.L2NpcTemplate;

public final class L2GuardInstance extends L2NpcInstance
{
	public L2GuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker.isMonster() || attacker.isPlayer() && attacker.getKarma() < 0;
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		return "data/html/guard/" + pom + ".htm";
	}

	@Override
	public boolean isInvul()
	{
		return false;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}
}