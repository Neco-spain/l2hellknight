package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Craft
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.COMMON_CRAFT, L2Skill.SkillType.DWARVEN_CRAFT };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    if ((activeChar == null) || (!(activeChar instanceof L2PcInstance))) return;

    L2PcInstance player = (L2PcInstance)activeChar;

    if (player.getPrivateStoreType() != 0)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING));
      return;
    }
    RecipeController.getInstance().requestBookOpen(player, skill.getSkillType() == L2Skill.SkillType.DWARVEN_CRAFT);
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}