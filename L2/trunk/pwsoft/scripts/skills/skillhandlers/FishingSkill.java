package scripts.skills.skillhandlers;

import javolution.util.FastList;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Fishing;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.templates.L2Weapon;
import scripts.skills.ISkillHandler;

public class FishingSkill
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.PUMPING, L2Skill.SkillType.REELING };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if ((activeChar == null) || (!activeChar.isPlayer())) return;

    L2PcInstance player = (L2PcInstance)activeChar;

    L2Fishing fish = player.GetFishCombat();
    if (fish == null)
    {
      if (skill.getSkillType() == L2Skill.SkillType.PUMPING)
      {
        player.sendPacket(Static.CAN_USE_PUMPING_ONLY_WHILE_FISHING);
      }
      else if (skill.getSkillType() == L2Skill.SkillType.REELING)
      {
        player.sendPacket(Static.CAN_USE_REELING_ONLY_WHILE_FISHING);
      }
      player.sendPacket(new ActionFailed());
      return;
    }
    L2Weapon weaponItem = player.getActiveWeaponItem();
    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
    if ((weaponInst == null) || (weaponItem == null))
      return;
    int SS = 1;
    int pen = 0;
    if ((weaponInst != null) && (weaponInst.getChargedFishshot())) SS = 2;
    double gradebonus = 1.0D + weaponItem.getCrystalType() * 0.1D;
    int dmg = (int)(skill.getPower() * gradebonus * SS);
    if (player.getSkillLevel(1315) <= skill.getLevel() - 2)
    {
      player.sendPacket(Static.REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY);
      pen = 50;
      int penatlydmg = dmg - pen;
      if (player.isGM()) player.sendMessage("Dmg w/o penalty = " + dmg);
      dmg = penatlydmg;
    }
    if (SS > 1)
    {
      weaponInst.setChargedFishshot(false);
    }
    if (skill.getSkillType() == L2Skill.SkillType.REELING)
    {
      fish.useRealing(dmg, pen);
    }
    else
    {
      fish.usePomping(dmg, pen);
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}