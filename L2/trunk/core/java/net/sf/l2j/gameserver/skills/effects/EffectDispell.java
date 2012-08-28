package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.util.Rnd;

public class EffectDispell extends L2Effect
{
	public EffectDispell(Env env, EffectTemplate template)
    {
        super(env, template);
    }

    @Override
	public EffectType getEffectType()
    {
        return EffectType.DISPELL_YOU;
    }

    @Override
	public boolean onStart() 
	{
		double landrate = (int) getEffector().calcStat(Stats.CANCEL_VULN, 40, getEffected(), null);
		if(Rnd.get(100) < landrate)
		{
			L2Effect[] effects = getEffected().getAllEffects();
			int maxfive = 5;
			for (L2Effect e : effects)
			{
				switch (e.getEffectType())
				{
					case SIGNET_GROUND:
					case SIGNET_EFFECT:
						continue;
				}
				if (e.getSkill().getId() != 4082 && e.getSkill().getId() != 4215 &&
					e.getSkill().getId() != 4515 && e.getSkill().getId() != 110 && e.getSkill().getId() != 111 &&
					e.getSkill().getId() != 1323 && e.getSkill().getId() != 1325) // Cannot cancel skills 4082, 4215, 4515, 110, 111, 1323, 1325
				{
					if(e.getSkill().getSkillType() != SkillType.BUFF) //sleep, slow, surrenders etc
						e.exit();
					else
					{
						int rate = 100;
						int level = e.getLevel();
						if (level > 0) rate = Integer.valueOf(150/(1 + level));
						if (rate > 95) rate = 95;
						else if (rate < 5) rate = 5;
						if(Rnd.get(100) < rate)
						{
							e.exit();
							maxfive--;
							if(maxfive == 0) break;
						}
					}
				}
			}
		}
	return true;
    }

    @Override
	public void onExit()
	{
    }

    @Override
	public boolean onActionTime()
    {
        return false;
    }
}
