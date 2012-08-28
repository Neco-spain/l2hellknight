package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import scripts.skills.ISkillHandler;
import scripts.skills.SkillHandler;

public class Heal
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.HEAL, L2Skill.SkillType.HEAL_PERCENT, L2Skill.SkillType.HEAL_STATIC };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if ((activeChar == null) || (activeChar.isAlikeDead())) {
      return;
    }
    try
    {
      ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(L2Skill.SkillType.BUFF);

      if (handler != null)
        handler.useSkill(activeChar, skill, targets);
    }
    catch (Exception e)
    {
    }
    L2Character target = null;
    L2PcInstance player = null;
    L2Summon activeSummon = null;

    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

    if (activeChar.isPlayer())
      player = (L2PcInstance)activeChar;
    else if (activeChar.isL2Summon()) {
      activeSummon = (L2Summon)activeChar;
    }

    double hp_mul = 1.0D;
    boolean clearSpiritShot = false;
    if ((skill.getSkillType() != L2Skill.SkillType.HEAL_PERCENT) && (skill.getId() != 4051))
    {
      if ((weaponInst != null) && (weaponInst.getChargedSpiritshot() > 0)) {
        switch (weaponInst.getChargedSpiritshot()) {
        case 2:
          hp_mul = 1.5D;
          break;
        case 1:
          hp_mul = 1.3D;
        }

        clearSpiritShot = true;
      }
      else if ((activeSummon != null) && (activeSummon.getChargedSpiritShot() > 0)) {
        switch (activeSummon.getChargedSpiritShot()) {
        case 2:
          hp_mul = 1.5D;
          break;
        case 1:
          hp_mul = 1.3D;
        }

        clearSpiritShot = true;
      }
    }

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; ) {
      target = (L2Character)n.getValue();

      if ((target == null) || (target.isDead()) || (target.isL2Door()) || 
        ((target.isRaid()) && (Config.ALLOW_RAID_BOSS_HEAL) && (player != null)) || (
        (target != activeChar) && (
        ((target.isPlayer()) && (target.isCursedWeaponEquiped())) || (
        (player != null) && (player.isCursedWeaponEquiped())))))
      {
        continue;
      }

      double hp = target.getCurrentHp() == target.getMaxHp() ? 0.0D : skill.getPower();
      if (hp > 0.0D) {
        if (skill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT)
          hp = target.getMaxHp() * hp / 100.0D;
        else {
          hp *= hp_mul;
        }

        if (skill.getSkillType() == L2Skill.SkillType.HEAL_STATIC)
          hp = skill.getPower();
        else if (skill.getSkillType() != L2Skill.SkillType.HEAL_PERCENT) {
          hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0D, null, null) / 100.0D;
        }

        target.setCurrentHp(target.getCurrentHp() + hp);
        target.setLastHealAmount((int)hp);
      }

      if (target.isPlayer()) {
        if (skill.getId() == 4051) {
          target.sendPacket(Static.REJUVENATING_HP);
        }
        else
        {
          SystemMessage sm;
          if ((activeChar.isPlayer()) && (activeChar != target))
            sm = SystemMessage.id(SystemMessageId.S2_HP_RESTORED_BY_S1).addString(activeChar.getName());
          else {
            sm = SystemMessage.id(SystemMessageId.S1_HP_RESTORED);
          }

          sm.addNumber((int)hp);
          target.sendPacket(sm);
          SystemMessage sm = null;
        }
      }
    }

    if (clearSpiritShot) {
      if (activeSummon != null)
        activeSummon.setChargedSpiritShot(0);
      else if (weaponInst != null) {
        weaponInst.setChargedSpiritshot(0);
      }
    }

    player = null;
    target = null;
    activeSummon = null;
  }

  public L2Skill.SkillType[] getSkillIds() {
    return SKILL_IDS;
  }
}