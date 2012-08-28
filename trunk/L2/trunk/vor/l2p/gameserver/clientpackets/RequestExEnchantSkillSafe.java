package l2p.gameserver.clientpackets;

import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.EnchantSkillLearn;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.ExEnchantSkillInfo;
import l2p.gameserver.serverpackets.ExEnchantSkillResult;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTreeTable;
import l2p.gameserver.utils.Log;

public final class RequestExEnchantSkillSafe extends L2GameClientPacket
{
  private int _skillId;
  private int _skillLvl;

  protected void readImpl()
  {
    _skillId = readD();
    _skillLvl = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.getTransformation() != 0)
    {
      activeChar.sendMessage("You must leave transformation mode first.");
      return;
    }

    if ((activeChar.getLevel() < 76) || (activeChar.getClassId().getLevel() < 4))
    {
      activeChar.sendMessage("You must have 3rd class change quest completed.");
      return;
    }

    EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);

    if (sl == null) {
      return;
    }
    int slevel = activeChar.getSkillLevel(Integer.valueOf(_skillId));
    if (slevel == -1) {
      return;
    }
    int enchantLevel = SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel());

    if (slevel >= enchantLevel) {
      return;
    }

    if (slevel == sl.getBaseLevel() ? _skillLvl % 100 != 1 : slevel != enchantLevel - 1)
    {
      activeChar.sendMessage("Incorrect enchant level.");
      return;
    }

    Skill skill = SkillTable.getInstance().getInfo(_skillId, enchantLevel);
    if (skill == null) {
      return;
    }
    int[] cost = sl.getCost();
    int requiredSp = cost[1] * 5 * sl.getCostMult();
    int requiredAdena = cost[0] * 5 * sl.getCostMult();

    int rate = sl.getRate(activeChar);

    if (activeChar.getSp() < requiredSp)
    {
      sendPacket(Msg.SP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT);
      return;
    }

    if (activeChar.getAdena() < requiredAdena)
    {
      sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
      return;
    }

    if ((Functions.getItemCount(activeChar, 9627) == 0L) && (!Config.DISABLE_ENCHANT_BOOKS_ALL))
    {
      activeChar.sendPacket(Msg.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
      return;
    }

    if (!Config.DISABLE_ENCHANT_BOOKS_ALL) {
      Functions.removeItem(activeChar, 9627, 1L);
    }
    if (Rnd.chance(rate))
    {
      activeChar.addSkill(skill, true);
      activeChar.addExpAndSp(0L, -1 * requiredSp);
      Functions.removeItem(activeChar, 57, requiredAdena);
      activeChar.sendPacket(new IStaticPacket[] { new SystemMessage(538).addNumber(requiredSp), new SystemMessage(1440).addSkillName(_skillId, _skillLvl), new ExEnchantSkillResult(1) });
      activeChar.sendPacket(new SkillList(activeChar));
      RequestExEnchantSkill.updateSkillShortcuts(activeChar, _skillId, _skillLvl);
      Log.add(activeChar.getName() + "|Successfully safe enchanted|" + _skillId + "|to+" + _skillLvl + "|" + rate, "enchant_skills");
    }
    else
    {
      activeChar.sendPacket(new IStaticPacket[] { new SystemMessage(2074).addSkillName(_skillId, _skillLvl), new ExEnchantSkillResult(0) });
      Log.add(activeChar.getName() + "|Failed to safe enchant|" + _skillId + "|to+" + _skillLvl + "|" + rate, "enchant_skills");
    }

    activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, activeChar.getSkillDisplayLevel(Integer.valueOf(_skillId))));
  }
}