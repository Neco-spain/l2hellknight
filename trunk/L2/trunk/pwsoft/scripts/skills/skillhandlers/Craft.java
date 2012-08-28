package scripts.skills.skillhandlers;

import javolution.util.FastList;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.skills.ISkillHandler;

public class Craft
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.COMMON_CRAFT, L2Skill.SkillType.DWARVEN_CRAFT };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if ((activeChar == null) || (!activeChar.isPlayer())) return;

    L2PcInstance player = (L2PcInstance)activeChar;

    if (player.getPrivateStoreType() != 0)
    {
      player.sendPacket(Static.CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING);
      return;
    }
    RecipeController.getInstance().requestBookOpen(player, skill.getSkillType() == L2Skill.SkillType.DWARVEN_CRAFT);
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}