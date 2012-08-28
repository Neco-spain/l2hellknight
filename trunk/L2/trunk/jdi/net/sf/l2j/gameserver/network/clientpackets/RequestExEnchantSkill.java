package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
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
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public final class RequestExEnchantSkill extends L2GameClientPacket
{
  private static final String _C__D0_07_REQUESTEXENCHANTSKILL = "[C] D0:07 RequestExEnchantSkill";
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
      _rate = s.getRate(player);
      _baseLvl = s.getBaseLevel();
    }

    if ((counts == 0) && (!Config.ALT_GAME_SKILL_LEARN))
    {
      player.sendMessage("You are trying to learn skill that u can't..");
      Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", 2);

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
            player.sendPacket(new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
            return;
          }

          player.destroyItem("Consume", spb, trainer, true);
        }
      }
      else
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL);
        player.sendPacket(sm);
        return;
      }
    }
    else
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
      player.sendPacket(sm);
      return;
    }
    if (Rnd.get(100) <= _rate)
    {
      if (_requiredSp == 10000000)
        return;
      player.addSkill(skill, true);

      if (Config.DEBUG) {
        _log.fine("Learned skill " + _skillId + " for " + _requiredSp + " SP.");
      }
      player.getStat().removeExpAndSp(_requiredExp, _requiredSp);

      StatusUpdate su = new StatusUpdate(player.getObjectId());
      su.addAttribute(13, player.getSp());
      player.sendPacket(su);

      SystemMessage ep = new SystemMessage(SystemMessageId.EXP_DECREASED_BY_S1);
      ep.addNumber(_requiredExp);
      sendPacket(ep);

      SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
      sp.addNumber(_requiredSp);
      sendPacket(sp);

      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1);
      sm.addSkillName(_skillId);
      player.sendPacket(sm);
    }
    else
    {
      if (skill.getLevel() > 100)
      {
        if (_requiredSp == 10000000)
          return;
        _skillLvl = _baseLvl;
        player.addSkill(SkillTable.getInstance().getInfo(_skillId, _skillLvl), true);
        player.sendSkillList();
      }
      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1);
      sm.addSkillName(_skillId);
      player.sendPacket(sm);
    }
    trainer.showEnchantSkillList(player, player.getClassId());

    L2ShortCut[] allShortCuts = player.getAllShortCuts();
    if (_requiredSp == 10000000) {
      return;
    }
    for (L2ShortCut sc : allShortCuts)
    {
      if ((sc.getId() != _skillId) || (sc.getType() != 2))
        continue;
      L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _skillLvl, 1);
      player.sendPacket(new ShortCutRegister(newsc));
      player.registerShortCut(newsc);
    }
  }

  public String getType()
  {
    return "[C] D0:07 RequestExEnchantSkill";
  }
}