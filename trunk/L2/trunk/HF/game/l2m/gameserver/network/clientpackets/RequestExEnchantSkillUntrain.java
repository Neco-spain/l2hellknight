package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.base.EnchantSkillLearn;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.scripts.Functions;
import l2m.gameserver.network.serverpackets.ExEnchantSkillInfo;
import l2m.gameserver.network.serverpackets.ExEnchantSkillResult;
import l2m.gameserver.network.serverpackets.SkillList;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.data.tables.SkillTreeTable;
import l2m.gameserver.utils.Log;

public final class RequestExEnchantSkillUntrain extends L2GameClientPacket
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

    int oldSkillLevel = activeChar.getSkillDisplayLevel(Integer.valueOf(_skillId));
    if (oldSkillLevel == -1) {
      return;
    }
    if ((_skillLvl != oldSkillLevel - 1) || (_skillLvl / 100 != oldSkillLevel / 100)) {
      return;
    }
    EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, oldSkillLevel);
    if (sl == null)
      return;
    Skill newSkill;
    Skill newSkill;
    if (_skillLvl % 100 == 0)
    {
      _skillLvl = sl.getBaseLevel();
      newSkill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
    }
    else {
      newSkill = SkillTable.getInstance().getInfo(_skillId, SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel()));
    }
    if (newSkill == null) {
      return;
    }
    if (Functions.getItemCount(activeChar, 9625) == 0L)
    {
      activeChar.sendPacket(Msg.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
      return;
    }

    Functions.removeItem(activeChar, 9625, 1L);

    activeChar.addExpAndSp(0L, sl.getCost()[1] * sl.getCostMult());
    activeChar.addSkill(newSkill, true);

    if (_skillLvl > 100)
    {
      SystemMessage sm = new SystemMessage(2069);
      sm.addSkillName(_skillId, _skillLvl);
      activeChar.sendPacket(sm);
    }
    else
    {
      SystemMessage sm = new SystemMessage(2070);
      sm.addSkillName(_skillId, _skillLvl);
      activeChar.sendPacket(sm);
    }

    Log.add(activeChar.getName() + "|Successfully untranes|" + _skillId + "|to+" + _skillLvl + "|---", "enchant_skills");

    activeChar.sendPacket(new IStaticPacket[] { new ExEnchantSkillInfo(_skillId, newSkill.getDisplayLevel()), ExEnchantSkillResult.SUCCESS, new SkillList(activeChar) });
    RequestExEnchantSkill.updateSkillShortcuts(activeChar, _skillId, _skillLvl);
  }
}