package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.model.L2EnchantSkillLearn;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.stat.PcStat;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutRegister;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.util.Rnd;

public final class RequestExEnchantSkill extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestAquireSkill.class.getName());
  private int _skillId;
  private int _skillLvl;

  protected void readImpl()
  {
    _skillId = readD();
    _skillLvl = readD();
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

    if (((trainer == null) || (!player.isInsideRadius(trainer, 150, false, false))) && (!player.isGM())) {
      return;
    }
    if (player.getSkillLevel(_skillId) >= _skillLvl) {
      return;
    }
    if (player.getClassId().getId() < 88) {
      return;
    }
    if (player.getLevel() < 76) return;

    L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);

    int counts = 0;
    int _requiredSp = 10000000;
    int _requiredExp = 100000;
    byte _rate = 0;
    int _baseLvl = 1;

    L2EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(player);

    for (L2EnchantSkillLearn s : skills)
    {
      L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
      if ((sk == null) || (sk != skill) || (!sk.getCanLearn(player.getClassId())) || (!sk.canTeachBy(npcid)))
        continue;
      counts++;
      _requiredSp = s.getSpCost();
      _requiredExp = s.getExp();
      _rate = s.getRate(player, (Config.PREMIUM_ENABLE) && (player.isPremium()));
      _baseLvl = s.getBaseLevel();
    }

    if ((counts == 0) && (!Config.ALT_GAME_SKILL_LEARN))
    {
      player.sendPacket(Static.CANT_LEARN_SKILL);

      return;
    }

    if (player.getSp() >= _requiredSp)
    {
      if (player.getExp() >= _requiredExp)
      {
        if ((Config.ES_SP_BOOK_NEEDED) && ((_skillLvl == 101) || (_skillLvl == 141)))
        {
          int spbId = 6622;

          L2ItemInstance spb = player.getInventory().getItemByItemId(spbId);

          if (spb == null)
          {
            player.sendPacket(Static.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
            return;
          }

          player.destroyItem("Consume", spb, trainer, true);
        }
      }
      else
      {
        player.sendPacket(Static.YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL);
        return;
      }
    }
    else
    {
      player.sendPacket(Static.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
      return;
    }
    if (Rnd.get(100) <= _rate)
    {
      player.addSkill(skill, true);
      player.getStat().removeExpAndSp(_requiredExp, _requiredSp);

      StatusUpdate su = new StatusUpdate(player.getObjectId());
      su.addAttribute(13, player.getSp());
      player.sendPacket(su);

      sendPacket(SystemMessage.id(SystemMessageId.EXP_DECREASED_BY_S1).addNumber(_requiredExp));
      sendPacket(SystemMessage.id(SystemMessageId.SP_DECREASED_S1).addNumber(_requiredSp));
      player.sendPacket(SystemMessage.id(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1).addSkillName(_skillId));
    }
    else
    {
      if (skill.getLevel() > 100)
      {
        _skillLvl = _baseLvl;
        player.addSkill(SkillTable.getInstance().getInfo(_skillId, _skillLvl), true);
        player.sendSkillList();
      }
      player.sendPacket(SystemMessage.id(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1).addSkillName(_skillId));
    }
    trainer.showEnchantSkillList(player, player.getClassId());

    FastTable allShortCuts = new FastTable();
    allShortCuts.addAll(player.getAllShortCuts());
    int i = 0; for (int n = allShortCuts.size(); i < n; i++)
    {
      L2ShortCut sc = (L2ShortCut)allShortCuts.get(i);
      if (sc == null) {
        continue;
      }
      if ((sc.getId() != _skillId) || (sc.getType() != 2))
        continue;
      L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _skillLvl, 1);
      player.sendPacket(new ShortCutRegister(newsc));
      player.registerShortCut(newsc);
    }

    allShortCuts.clear();
    allShortCuts = null;
  }
}