package l2rt.gameserver.skills;

import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.tables.SkillTable;

public final class SkillHolder
{

    public SkillHolder(int skillId, int skillLvl)
    {
        _skillId = skillId;
        _skillLvl = skillLvl;
    }

    public SkillHolder(L2Skill skill)
    {
        _skillId = skill.getId();
        _skillLvl = skill.getLevel();
    }

    public final int getSkillId()
    {
        return _skillId;
    }

    public final int getSkillLvl()
    {
        return _skillLvl;
    }

    public final L2Skill getSkill()
    {
        return SkillTable.getInstance().getInfo(_skillId, _skillLvl);
    }

    private final int _skillId;
    private final int _skillLvl;
}
