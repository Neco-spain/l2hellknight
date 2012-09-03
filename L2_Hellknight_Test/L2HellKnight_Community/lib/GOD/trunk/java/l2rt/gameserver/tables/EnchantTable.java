package l2rt.gameserver.tables;

import javolution.util.FastMap;
import l2rt.gameserver.model.base.L2EnchantSkillLearn;
import l2rt.util.GArray;

public abstract class EnchantTable
{
	public static FastMap<Integer, GArray<L2EnchantSkillLearn>> _enchant = new FastMap<Integer, GArray<L2EnchantSkillLearn>>().setShared(true);
}