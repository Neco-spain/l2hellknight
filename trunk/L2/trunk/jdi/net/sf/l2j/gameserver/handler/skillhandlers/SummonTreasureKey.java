package net.sf.l2j.gameserver.handler.skillhandlers;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.Rnd;

public class SummonTreasureKey
  implements ISkillHandler
{
  static Logger _log = Logger.getLogger(SummonTreasureKey.class.getName());
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.SUMMON_TREASURE_KEY };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    if ((activeChar == null) || (!(activeChar instanceof L2PcInstance))) return;

    L2PcInstance player = (L2PcInstance)activeChar;
    try
    {
      int item_id = 0;

      switch (skill.getLevel())
      {
      case 1:
        item_id = Rnd.get(6667, 6669);
        break;
      case 2:
        item_id = Rnd.get(6668, 6670);
        break;
      case 3:
        item_id = Rnd.get(6669, 6671);
        break;
      case 4:
        item_id = Rnd.get(6670, 6672);
      }

      player.addItem("Skill", item_id, Rnd.get(2, 3), player, false);
    }
    catch (Exception e)
    {
      _log.warning("Error using skill summon Treasure Key:" + e);
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}