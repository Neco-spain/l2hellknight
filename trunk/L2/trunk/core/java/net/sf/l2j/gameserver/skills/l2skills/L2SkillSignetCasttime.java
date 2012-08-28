package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.templates.StatsSet;

public final class L2SkillSignetCasttime extends L2Skill
{
	public int _effectNpcId;
	public int effectId;

    public L2SkillSignetCasttime(StatsSet set)
    {
        super(set);
        _effectNpcId = set.getInteger("effectNpcId", -1);
        effectId = set.getInteger("effectId", -1);
    }
    
    @Override
	public void useSkill(L2Character caster, L2Object[] targets)
    {
        if (caster.isAlikeDead())
            return;
        if(caster.isInsideZone(2))
        {
            return;
        }
		else
        {
            getEffectsSelf(caster);
            return;
        }
    }
}
