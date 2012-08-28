package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillSpellbookTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.model.L2PledgeSkillLearn;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.AquireSkillInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class RequestAquireSkillInfo extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestAquireSkillInfo.class.getName());
  private int _id;
  private int _level;
  private int _skillType;

  protected void readImpl()
  {
    _id = readD();
    _level = readD();
    _skillType = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPAP() < 300L) {
      return;
    }
    player.sCPAP();

    L2FolkInstance trainer = player.getLastFolkNPC();

    if (((trainer == null) || (!player.isInsideRadius(trainer, 150, false, false))) && (!player.isGM())) {
      return;
    }
    L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);

    boolean canteach = false;

    if (skill == null)
    {
      return;
    }

    if (_skillType == 0)
    {
      if (!trainer.getTemplate().canTeach(player.getSkillLearningClassId())) {
        return;
      }
      L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getSkillLearningClassId());

      for (L2SkillLearn s : skills)
      {
        if ((s.getId() != _id) || (s.getLevel() != _level))
          continue;
        canteach = true;
        break;
      }

      if (!canteach) {
        return;
      }
      player.setAquFlag(skill.getId());
      int requiredSp = SkillTreeTable.getInstance().getSkillCost(player, skill);
      AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), requiredSp, 0);

      if (Config.SP_BOOK_NEEDED)
      {
        int spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill);

        if ((skill.getLevel() == 1) && (spbId > -1)) {
          asi.addRequirement(99, spbId, 1, 50);
        }
      }
      sendPacket(asi);
    }
    else if (_skillType == 2)
    {
      int requiredRep = 0;
      int itemId = 0;
      L2PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(player);

      for (L2PledgeSkillLearn s : skills)
      {
        if ((s.getId() != _id) || (s.getLevel() != _level))
          continue;
        canteach = true;
        requiredRep = s.getRepCost();
        itemId = s.getItemId();
        break;
      }

      if (!canteach) {
        return;
      }

      player.setAquFlag(skill.getId());
      AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), requiredRep, 2);

      if (Config.LIFE_CRYSTAL_NEEDED)
      {
        asi.addRequirement(1, itemId, 1, 0);
      }

      sendPacket(asi);
    }
    else
    {
      int costid = 0;
      int costcount = 0;
      int spcost = 0;

      L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSkills(player);

      for (L2SkillLearn s : skillsc)
      {
        L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

        if ((sk == null) || (sk != skill)) {
          continue;
        }
        canteach = true;
        costid = s.getIdCost();
        costcount = s.getCostCount();
        spcost = s.getSpCost();
      }

      player.setAquFlag(skill.getId());
      AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), spcost, 1);
      asi.addRequirement(4, costid, costcount, 0);
      sendPacket(asi);
    }
  }
}