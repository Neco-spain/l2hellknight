package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillSpellbookTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PledgeSkillLearn;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2DonateInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FishermanInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2VillageMasterInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutRegister;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

public class RequestAquireSkill extends L2GameClientPacket
{
  private static final String _C__6C_REQUESTAQUIRESKILL = "[C] 6C RequestAquireSkill";
  private static Logger _log = Logger.getLogger(RequestAquireSkill.class.getName());
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
    L2FolkInstance trainer = player.getLastFolkNPC();
    if (trainer == null) {
      return;
    }
    int npcid = trainer.getNpcId();

    if ((!player.isInsideRadius(trainer, 150, false, false)) && (!player.isGM()))
    {
      return;
    }
    if (!Config.ALT_GAME_SKILL_LEARN) player.setSkillLearningClassId(player.getClassId());

    if (player.getSkillLevel(_id) >= _level)
    {
      return;
    }

    L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);

    if (skill.noAquire())
    {
      player.sendMessage("You cannot learn the skill");
      return;
    }

    int counts = 0;
    int _requiredSp = 10000000;

    if (_skillType == 0)
    {
      L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getSkillLearningClassId());

      for (L2SkillLearn s : skills)
      {
        L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

        if ((sk == null) || (sk != skill) || (!sk.getCanLearn(player.getSkillLearningClassId())) || (!sk.canTeachBy(npcid)))
        {
          continue;
        }
        counts++;
        _requiredSp = SkillTreeTable.getInstance().getSkillCost(player, skill);
      }

      if ((counts == 0) && (!Config.ALT_GAME_SKILL_LEARN))
      {
        player.sendMessage("You are trying to learn skill that u can't..");

        Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", 2);

        return;
      }

      if (player.getSp() >= _requiredSp)
      {
        if (Config.SP_BOOK_NEEDED)
        {
          int spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill);

          if ((skill.getLevel() == 1) && (spbId > -1))
          {
            L2ItemInstance spb = player.getInventory().getItemByItemId(spbId);

            if (spb == null)
            {
              player.sendPacket(new SystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));

              return;
            }

            player.destroyItem("Consume", spb.getObjectId(), 1, trainer, false);
          }
        }
      }
      else {
        SystemMessage sm = new SystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);

        player.sendPacket(sm);
        sm = null;

        return;
      }
    } else if (_skillType == 1)
    {
      int costid = 0;
      int costcount = 0;

      if ((player.getTarget() instanceof L2DonateInstance))
      {
        if (!Config.ENABLE_MY_SKILL_LEARN) return;

        int iter = 0;
        for (L2SkillLearn s : Config.MY_L2SKILL_LEARN)
        {
          int class_id = ((Integer)Config.MY_L2SKILL_CLASS_ID.get(iter)).intValue();

          iter++;

          L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

          if ((sk == null) || (sk != skill)) {
            continue;
          }
          boolean Cant = false;

          for (L2Skill skill2 : player.getAllSkills()) {
            if ((skill2.getId() != sk.getId()) || (sk.getLevel() > skill2.getLevel()))
              continue;
            Cant = true;
            break;
          }
          if (Cant) {
            return;
          }
          if ((class_id != -1) && 
            (class_id != player.getClassId().getId())) {
            return;
          }
          counts++;
          costid = s.getIdCost();
          costcount = s.getCostCount();
          _requiredSp = s.getSpCost();
        }

        if (counts == 0)
        {
          player.sendMessage("You are trying to learn skill that u can't..");
          Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", 2);

          return;
        }
        if (player.getSp() >= _requiredSp)
        {
          if (!player.destroyItemByItemId("Consume", costid, costcount, trainer, false))
          {
            player.sendPacket(new SystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));

            return;
          }

          SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);

          sm.addNumber(costcount);
          sm.addItemName(costid);
          sendPacket(sm);
          sm = null;
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);

          player.sendPacket(sm);
          sm = null;
          return;
        }

      }
      else
      {
        L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSkills(player);

        for (L2SkillLearn s : skillsc)
        {
          L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

          if ((sk == null) || (sk != skill)) {
            continue;
          }
          counts++;
          costid = s.getIdCost();
          costcount = s.getCostCount();
          _requiredSp = s.getSpCost();
        }

        if (counts == 0)
        {
          player.sendMessage("You are trying to learn skill that u can't..");
          Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", 2);

          return;
        }

        if (player.getSp() >= _requiredSp)
        {
          if (!player.destroyItemByItemId("Consume", costid, costcount, trainer, false))
          {
            player.sendPacket(new SystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));

            return;
          }

          SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);

          sm.addNumber(costcount);
          sm.addItemName(costid);
          sendPacket(sm);
          sm = null;
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);

          player.sendPacket(sm);
          sm = null;
          return;
        }
      }
    } else {
      if (_skillType == 2)
      {
        if (!player.isClanLeader())
        {
          player.sendMessage("This feature is available only for the clan leader");
          return;
        }

        int itemId = 0;
        int repCost = 100000000;

        L2PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(player);

        for (L2PledgeSkillLearn s : skills)
        {
          L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

          if ((sk == null) || (sk != skill)) {
            continue;
          }
          counts++;
          itemId = s.getItemId();
          repCost = s.getRepCost();
        }

        if (counts == 0)
        {
          player.sendMessage("You are trying to learn skill that u can't..");
          Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", 2);

          return;
        }

        if (player.getClan().getReputationScore() >= repCost)
        {
          if (Config.LIFE_CRYSTAL_NEEDED)
          {
            if (!player.destroyItemByItemId("Consume", itemId, 1, trainer, false))
            {
              player.sendPacket(new SystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
              return;
            }

            SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
            sm.addItemName(itemId);
            sm.addNumber(1);
            sendPacket(sm);
            sm = null;
          }
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
          player.sendPacket(sm);

          return;
        }
        player.getClan().setReputationScore(player.getClan().getReputationScore() - repCost, true);
        player.getClan().addNewSkill(skill);

        if (Config.DEBUG) {
          _log.fine("Learned pledge skill " + _id + " for " + _requiredSp + " SP.");
        }
        SystemMessage cr = new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
        cr.addNumber(repCost);
        player.sendPacket(cr);
        SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
        sm.addSkillName(_id);
        player.sendPacket(sm);
        sm = null;

        player.getClan().broadcastToOnlineMembers(new PledgeSkillList(player.getClan()));

        for (L2PcInstance member : player.getClan().getOnlineMembers(""))
        {
          member.sendSkillList();
        }
        ((L2VillageMasterInstance)trainer).showPledgeSkillList(player);

        return;
      }

      _log.warning("Recived Wrong Packet Data in Aquired Skill - unk1:" + _skillType);

      return;
    }

    if (_requiredSp == 10000000) {
      Util.handleIllegalPlayerAction(player, "Warning!! " + player.getName() + ": skill " + _id + " for " + _requiredSp + " SP.", Config.DEFAULT_PUNISH);
      player.logout();
      return;
    }

    player.addSkill(skill, true);

    if (Config.DEBUG) {
      _log.fine("Learned skill " + _id + " for " + _requiredSp + " SP.");
    }
    player.setSp(player.getSp() - _requiredSp);

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(13, player.getSp());
    player.sendPacket(su);

    SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
    sp.addNumber(_requiredSp);
    sendPacket(sp);

    SystemMessage sm = new SystemMessage(SystemMessageId.LEARNED_SKILL_S1);
    sm.addSkillName(_id);
    player.sendPacket(sm);
    sm = null;

    if (_level > 1)
    {
      L2ShortCut[] allShortCuts = player.getAllShortCuts();

      for (L2ShortCut sc : allShortCuts)
      {
        if ((sc.getId() != _id) || (sc.getType() != 2))
          continue;
        L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _level, 1);

        player.sendPacket(new ShortCutRegister(newsc));
        player.registerShortCut(newsc);
      }

    }

    if ((trainer instanceof L2FishermanInstance))
      ((L2FishermanInstance)trainer).showSkillList(player);
    else if ((trainer instanceof L2DonateInstance))
      ((L2DonateInstance)trainer).MyLearnSkill(player);
    else {
      trainer.showSkillList(player, player.getSkillLearningClassId());
    }
    if ((_id >= 1368) && (_id <= 1372))
    {
      ExStorageMaxCount esmc = new ExStorageMaxCount(player);
      player.sendPacket(esmc);
    }
  }

  public String getType()
  {
    return "[C] 6C RequestAquireSkill";
  }
}