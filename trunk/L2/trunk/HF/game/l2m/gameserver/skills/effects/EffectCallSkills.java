package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.MagicSkillUse;
import l2m.gameserver.skills.Env;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.templates.StatsSet;

public class EffectCallSkills extends Effect
{
  public EffectCallSkills(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    int[] skillIds = getTemplate().getParam().getIntegerArray("skillIds");
    int[] skillLevels = getTemplate().getParam().getIntegerArray("skillLevels");

    for (int i = 0; i < skillIds.length; i++)
    {
      Skill skill = SkillTable.getInstance().getInfo(skillIds[i], skillLevels[i]);
      for (Creature cha : skill.getTargets(getEffector(), getEffected(), false))
        getEffector().broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(getEffector(), cha, skillIds[i], skillLevels[i], 0, 0L) });
      getEffector().callSkill(skill, skill.getTargets(getEffector(), getEffected(), false), false);
    }
  }

  public boolean onActionTime()
  {
    return false;
  }
}