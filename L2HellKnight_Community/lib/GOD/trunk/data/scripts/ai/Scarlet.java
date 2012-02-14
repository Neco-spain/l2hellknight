package ai;

import javolution.util.FastMap;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;
import l2rt.util.Rnd;
import bosses.FrintezzaManager;

/**
 * @author Diamond
 */
public class Scarlet extends DefaultAI
{
	// Базовый удар. 1-3 форма.
	private final L2Skill[] DaemonAttack = { SkillTable.getInstance().getInfo(5014, 1),
			SkillTable.getInstance().getInfo(5014, 2), SkillTable.getInstance().getInfo(5014, 3) };

	// Массовый удар + телепорт к цели. 1-3 форма.
	private final L2Skill[] DaemonCharge = { SkillTable.getInstance().getInfo(5015, 1),
			SkillTable.getInstance().getInfo(5015, 2), SkillTable.getInstance().getInfo(5015, 3),
			SkillTable.getInstance().getInfo(5015, 4), SkillTable.getInstance().getInfo(5015, 5),
			SkillTable.getInstance().getInfo(5015, 6) };

	// Массовый удар. 2-3 форма.
	private final L2Skill[] DaemonField = { SkillTable.getInstance().getInfo(5018, 1),
			SkillTable.getInstance().getInfo(5018, 2) };

	// Массовый паралич с эффектом "плавающего" рута. 3 форма.
	private final L2Skill YokeOfScarlet = SkillTable.getInstance().getInfo(5016, 1);

	// Массовый физический Drain 90%. 3 форма.
	private final L2Skill DaemonDrain = SkillTable.getInstance().getInfo(5019, 1);

	private static final int _strongScarletId = 29047;
	private static final int _frintezzasSwordId = 7903;

	public Scarlet(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected boolean createNewTask()
	{
		clearTasks();
		L2Character target;
		if((target = prepareTarget()) == null)
			return false;

		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return false;

		if(!FrintezzaManager.getZone().checkIfInZone(actor))
		{
			teleportHome(true);
			return false;
		}

		int stage = 0;
		if(actor.getNpcId() == _strongScarletId)
			stage = 2;
		else if(actor.getRightHandItem() == _frintezzasSwordId)
			stage = 1;

		double distance = actor.getDistance(target);
		int rnd_per = Rnd.get(100);

		//if(rnd_per < 10)
		//	return chooseTaskAndTargets(null, target, distance);

		if(rnd_per < 50)
			return chooseTaskAndTargets(DaemonAttack[stage], target, distance);

		boolean PowerEncore = false;
		GArray<L2Effect> effects = actor.getEffectList().getEffectsBySkillId(5008);
		if(effects != null && effects.get(0).getSkill().getLevel() == 3)
			PowerEncore = true;

		FastMap<L2Skill, Integer> d_skill = new FastMap<L2Skill, Integer>();

		switch(stage)
		{
			case 0:
				if(distance > 200)
					addDesiredSkill(d_skill, target, distance, DaemonCharge[PowerEncore ? 3 : 0]);
				break;
			case 1:
				if(distance > 200)
					addDesiredSkill(d_skill, target, distance, DaemonCharge[PowerEncore ? 4 : 1]);
				addDesiredSkill(d_skill, target, distance, DaemonField[0]);
				break;
			case 2:
				if(distance > 200)
					addDesiredSkill(d_skill, target, distance, DaemonCharge[PowerEncore ? 5 : 2]);
				addDesiredSkill(d_skill, target, distance, DaemonField[1]);
				addDesiredSkill(d_skill, target, distance, YokeOfScarlet);
				addDesiredSkill(d_skill, target, distance, DaemonDrain);
				break;
		}

		L2Skill r_skill = selectTopSkill(d_skill);
		if(r_skill != null && !r_skill.isOffensive())
			target = actor;

		return chooseTaskAndTargets(r_skill, target, distance);
	}

	@Override
	protected boolean maybeMoveToHome()
	{
		L2NpcInstance actor = getActor();
		if(actor != null && !FrintezzaManager.getZone().checkIfInZone(actor))
			teleportHome(true);
		return false;
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}