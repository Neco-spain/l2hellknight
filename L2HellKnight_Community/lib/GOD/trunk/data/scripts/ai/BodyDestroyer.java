package ai;

import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.SkillTable;

public class BodyDestroyer extends DefaultAI
{
	private int _firstAttakerObjectId = 0;
	private static final int announceDeathSkill = 5256;
	
	public BodyDestroyer(L2Character actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{	
		L2NpcInstance actor = getActor();
		if (actor == null || actor.isDead())
			return;
		
		L2Player plr = attacker.getPlayer();
		if (_firstAttakerObjectId == 0 && plr != null)
		{
			_firstAttakerObjectId = attacker.getObjectId();
			attacker.addDamageHate(actor, 0, 9999);
			
			L2Skill skill = SkillTable.getInstance().getInfo(announceDeathSkill, 1);
			if (skill != null)
				skill.getEffects(actor, attacker, false, false);
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtDead(L2Character killer)
	{
		if (_firstAttakerObjectId > 0)
		{
			L2Player plr = L2ObjectsStorage.getPlayer(_firstAttakerObjectId);
			if (plr != null)
				plr.getEffectList().stopEffect(announceDeathSkill);
		
			_firstAttakerObjectId = 0;
		}
		
		super.onEvtDead(killer);
	}
}