package l2rt.gameserver.skills;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.items.L2ItemInstance;

/**
 *
 * An Env object is just a class to pass parameters to a calculator such as L2Player,
 * L2ItemInstance, Initial value.
 *
 */
public final class Env
{
	public static final String FirstEffectSuccess = "FirstEffectSuccess";
	public static final String SkillMastery = "SkillMastery";

	public L2Character character;
	public L2Character target;
	public L2ItemInstance item;
	public L2Skill skill;
	public double value;
	public int[] arraymap;

	public Env()
	{}

	public Env(L2Character cha, L2Character tar, L2Skill sk)
	{
		character = cha;
		target = tar;
		skill = sk;
	}
}
