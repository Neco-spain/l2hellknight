package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2EffectPointInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Point3D;

public final class L2SkillSignet extends L2Skill
{
  private int _effectNpcId;
  public int effectId;

  public L2SkillSignet(StatsSet set)
  {
    super(set);
    _effectNpcId = set.getInteger("effectNpcId", -1);
    effectId = set.getInteger("effectId", -1);
  }

  public void useSkill(L2Character caster, L2Object[] targets)
  {
    if (caster.isAlikeDead()) {
      return;
    }
    L2NpcTemplate template = NpcTable.getInstance().getTemplate(_effectNpcId);
    L2EffectPointInstance effectPoint = new L2EffectPointInstance(IdFactory.getInstance().getNextId(), template, caster);
    effectPoint.setCurrentHp(effectPoint.getMaxHp());
    effectPoint.setCurrentMp(effectPoint.getMaxMp());
    L2World.getInstance().storeObject(effectPoint);

    int x = caster.getX();
    int y = caster.getY();
    int z = caster.getZ();

    if (((caster instanceof L2PcInstance)) && (getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND))
    {
      Point3D wordPosition = ((L2PcInstance)caster).getCurrentSkillWorldPosition();

      if (wordPosition != null)
      {
        x = wordPosition.getX();
        y = wordPosition.getY();
        z = wordPosition.getZ();
      }
    }
    getEffects(caster, effectPoint);

    effectPoint.setIsInvul(true);
    effectPoint.spawnMe(x, y, z);
  }
}