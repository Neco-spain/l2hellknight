package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.model.L2EnchantSkillLearn;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ExEnchantSkillInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class RequestExEnchantSkillInfo extends L2GameClientPacket
{
  private static final String _C__D0_06_REQUESTEXENCHANTSKILLINFO = "[C] D0:06 RequestExEnchantSkillInfo";
  private int _skillId;
  private int _skillLvl;

  protected void readImpl()
  {
    _skillId = readD();
    _skillLvl = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    if (activeChar.getLevel() < 76) {
      return;
    }
    L2FolkInstance trainer = activeChar.getLastFolkNPC();

    if (((trainer == null) || (!activeChar.isInsideRadius(trainer, 150, false, false))) && (!activeChar.isGM())) {
      return;
    }
    L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);

    boolean canteach = false;

    if ((skill == null) || (skill.getId() != _skillId))
    {
      activeChar.sendMessage("This skill doesn't yet have enchant info in Datapack");
      return;
    }

    if (!trainer.getTemplate().canTeach(activeChar.getClassId())) {
      return;
    }
    L2EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(activeChar);

    for (L2EnchantSkillLearn s : skills)
    {
      if ((s.getId() != _skillId) || (s.getLevel() != _skillLvl))
        continue;
      canteach = true;
      break;
    }

    if (!canteach) {
      return;
    }
    int requiredSp = SkillTreeTable.getInstance().getSkillSpCost(activeChar, skill);
    int requiredExp = SkillTreeTable.getInstance().getSkillExpCost(activeChar, skill);
    byte rate = SkillTreeTable.getInstance().getSkillRate(activeChar, skill);
    ExEnchantSkillInfo asi = new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), requiredSp, requiredExp, rate);

    if ((Config.ES_SP_BOOK_NEEDED) && ((skill.getLevel() == 101) || (skill.getLevel() == 141)))
    {
      int spbId = 6622;
      asi.addRequirement(4, spbId, 1, 0);
    }
    sendPacket(asi);
  }

  public String getType()
  {
    return "[C] D0:06 RequestExEnchantSkillInfo";
  }
}