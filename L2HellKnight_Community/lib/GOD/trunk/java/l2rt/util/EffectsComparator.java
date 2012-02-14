package l2rt.util;

import l2rt.gameserver.model.L2Effect;

import java.util.Comparator;

public class EffectsComparator implements Comparator<L2Effect>
{
	private static final EffectsComparator instance = new EffectsComparator();

	public static final EffectsComparator getInstance()
	{
		return instance;
	}

	public int compare(L2Effect o1, L2Effect o2)
	{
		if(o1 == null || o2 == null || o1.getSkill().isToggle() && o2.getSkill().isToggle())
			return 0;

		if(o1.getSkill().isToggle())
			return 1;

		if(o2.getSkill().isToggle())
			return -1;

		if(o1.getPeriodStartTime() > o2.getPeriodStartTime())
			return 1;

		if(o1.getPeriodStartTime() < o2.getPeriodStartTime())
			return -1;

		return 0;
	}
}