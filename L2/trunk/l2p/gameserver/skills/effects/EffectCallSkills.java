package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Skill;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.stats.Env;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.StatsSet;

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