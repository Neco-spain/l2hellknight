package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
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
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutRegister;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class RequestAquireSkill extends L2GameClientPacket
{
  private static final Logger _log = Logger.getLogger(RequestAquireSkill.class.getName());
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

    if (System.currentTimeMillis() - player.gCPAQ() < 300L) {
      return;
    }

    player.sCPAQ();

    if (player.getAquFlag() != _id) {
      return;
    }

    player.setAquFlag(0);

    L2FolkInstance trainer = player.getLastFolkNPC();
    if (trainer == null) {
      return;
    }

    int npcid = trainer.getNpcId();
    if ((!player.isInsideRadius(trainer, 150, false, false)) && (!player.isGM())) {
      return;
    }

    if (!Config.ALT_GAME_SKILL_LEARN) {
      player.setSkillLearningClassId(player.getClassId());
    }

    if (player.getSkillLevel(_id) >= _level) {
      return;
    }

    SkillTable st = SkillTable.getInstance();
    SkillTreeTable stt = SkillTreeTable.getInstance();

    int counts = 0;
    int _requiredSp = 10000000;
    L2Skill skill = st.getInfo(_id, _level);

    switch (_skillType) {
    case 0:
      for (L2SkillLearn s : stt.getAvailableSkills(player, player.getSkillLearningClassId())) {
        L2Skill sk = st.getInfo(s.getId(), s.getLevel());
        if ((sk == null) || (sk != skill) || (!sk.getCanLearn(player.getSkillLearningClassId())) || (!sk.canTeachBy(npcid))) {
          continue;
        }
        counts++;
        _requiredSp = stt.getSkillCost(player, skill);
      }

      if ((counts == 0) && (!Config.ALT_GAME_SKILL_LEARN))
      {
        return;
      }

      if (player.getSp() >= _requiredSp) {
        if (!Config.SP_BOOK_NEEDED) break;
        int spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill);

        if ((skill.getLevel() == 1) && (spbId > -1)) {
          L2ItemInstance spb = player.getInventory().getItemByItemId(spbId);

          if (spb == null)
          {
            player.sendPacket(Static.ITEM_MISSING_TO_LEARN_SKILL);
            return;
          }

          player.destroyItem("Consume", spb, trainer, true);
        }
      }
      else {
        player.sendPacket(Static.NOT_ENOUGH_SP_TO_LEARN_SKILL);
        return;
      }

    case 1:
      int costid = 0;
      int costcount = 0;

      L2Skill sk = null;
      for (L2SkillLearn s : stt.getAvailableSkills(player)) {
        sk = st.getInfo(s.getId(), s.getLevel());
        if ((sk == null) || (sk != skill))
        {
          continue;
        }
        counts++;
        costid = s.getIdCost();
        costcount = s.getCostCount();
        _requiredSp = s.getSpCost();
      }

      if (counts == 0) {
        player.sendPacket(Static.CANT_LEARN_SKILL);

        return;
      }

      if (player.getSp() >= _requiredSp) {
        if (!player.destroyItemByItemId("Consume", costid, costcount, trainer, false))
        {
          player.sendPacket(Static.ITEM_MISSING_TO_LEARN_SKILL);
          return;
        }

        sendPacket(SystemMessage.id(SystemMessageId.DISSAPEARED_ITEM).addNumber(costcount).addItemName(costid));
      } else {
        player.sendPacket(Static.NOT_ENOUGH_SP_TO_LEARN_SKILL);
        return;
      }

    case 2:
      if (!player.isClanLeader()) {
        player.sendPacket(Static.ONLY_FOR_CLANLEADER);
        return;
      }

      int itemId = 0;
      int repCost = 100000000;

      L2Skill skl = null;
      for (L2PledgeSkillLearn s : stt.getAvailablePledgeSkills(player)) {
        skl = st.getInfo(s.getId(), s.getLevel());
        if ((skl == null) || (skl != skill))
        {
          continue;
        }
        counts++;
        itemId = s.getItemId();
        repCost = s.getRepCost();
      }

      if (player.isPremium()) {
        repCost = (int)(repCost * Config.PREMIUM_AQURE_SKILL_MUL);
      }

      if (counts == 0)
      {
        return;
      }

      if (player.getClan().getReputationScore() >= repCost) {
        if (Config.LIFE_CRYSTAL_NEEDED) {
          if (!player.destroyItemByItemId("Consume", itemId, 1, trainer, false))
          {
            player.sendPacket(Static.ITEM_MISSING_TO_LEARN_SKILL);
            return;
          }

          sendPacket(SystemMessage.id(SystemMessageId.DISSAPEARED_ITEM).addItemName(itemId).addNumber(1));
        }
      } else {
        player.sendPacket(Static.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
        return;
      }
      player.getClan().setReputationScore(player.getClan().getReputationScore() - repCost, true);
      player.getClan().addNewSkill(skill);

      player.sendPacket(SystemMessage.id(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(repCost));
      player.sendPacket(SystemMessage.id(SystemMessageId.CLAN_SKILL_S1_ADDED).addSkillName(_id));

      player.getClan().broadcastToOnlineMembers(new PledgeSkillList(player.getClan()));

      for (L2PcInstance member : player.getClan().getOnlineMembers("")) {
        if (member == null)
        {
          continue;
        }
        member.sendSkillList();
      }

      trainer.showPledgeSkillList(player);
      return;
    default:
      _log.warning("Recived Wrong Packet Data in Aquired Skill - unk1:" + _skillType);
      return;
    }

    player.stopSkillEffects(_id);
    player.addSkill(skill, true);

    player.setSp(player.getSp() - _requiredSp);

    player.updateStats();
    player.sendChanges();

    sendPacket(SystemMessage.id(SystemMessageId.SP_DECREASED_S1).addNumber(_requiredSp));
    player.sendPacket(SystemMessage.id(SystemMessageId.LEARNED_SKILL_S1).addSkillName(_id));

    if (_level > 1) {
      FastTable allShortCuts = new FastTable();
      allShortCuts.addAll(player.getAllShortCuts());

      int i = 0; for (int n = allShortCuts.size(); i < n; i++) {
        L2ShortCut sc = (L2ShortCut)allShortCuts.get(i);
        if (sc == null)
        {
          continue;
        }
        if ((sc.getId() == _id) && (sc.getType() == 2)) {
          L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _level, 1);
          player.sendPacket(new ShortCutRegister(newsc));
          player.registerShortCut(newsc);
        }
      }
      allShortCuts.clear();
      allShortCuts = null;
    }

    if (trainer.isL2Fisherman())
      trainer.showSkillList(player);
    else {
      trainer.showSkillList(player, player.getSkillLearningClassId());
    }

    if ((_id >= 1368) && (_id <= 1372))
    {
      player.sendPacket(new ExStorageMaxCount(player));
    }
  }
}