package l2p.gameserver.clientpackets;

import l2p.commons.util.Rnd;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.EnchantSkillLearn;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.ExEnchantSkillInfo;
import l2p.gameserver.serverpackets.ExEnchantSkillResult;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTreeTable;
import l2p.gameserver.utils.Log;

public final class RequestExEnchantSkillRouteChange extends L2GameClientPacket
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
    int slevel = activeChar.getSkillDisplayLevel(Integer.valueOf(_skillId));
    if (slevel == -1) {
      return;
    }
    if ((slevel <= sl.getBaseLevel()) || (slevel % 100 != _skillLvl % 100)) {
      return;
    }
    int[] cost = sl.getCost();
    int requiredSp = cost[1] * sl.getCostMult() / 5;
    int requiredAdena = cost[0] * sl.getCostMult() / 5;

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

    if (Functions.getItemCount(activeChar, 9626) == 0L)
    {
      activeChar.sendPacket(Msg.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
      return;
    }

    Functions.removeItem(activeChar, 9626, 1L);
    Functions.removeItem(activeChar, 57, requiredAdena);
    activeChar.addExpAndSp(0L, -1 * requiredSp);

    int levelPenalty = Rnd.get(Math.min(4, _skillLvl % 100));

    _skillLvl -= levelPenalty;
    if (_skillLvl % 100 == 0) {
      _skillLvl = sl.getBaseLevel();
    }
    Skill skill = SkillTable.getInstance().getInfo(_skillId, SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel()));

    if (skill != null) {
      activeChar.addSkill(skill, true);
    }
    if (levelPenalty == 0)
    {
      SystemMessage sm = new SystemMessage(2073);
      sm.addSkillName(_skillId, _skillLvl);
      activeChar.sendPacket(sm);
    }
    else
    {
      SystemMessage sm = new SystemMessage(2072);
      sm.addSkillName(_skillId, _skillLvl);
      sm.addNumber(levelPenalty);
      activeChar.sendPacket(sm);
    }

    Log.add(activeChar.getName() + "|Successfully changed route|" + _skillId + "|" + slevel + "|to+" + _skillLvl + "|" + levelPenalty, "enchant_skills");

    activeChar.sendPacket(new IStaticPacket[] { new ExEnchantSkillInfo(_skillId, activeChar.getSkillDisplayLevel(Integer.valueOf(_skillId))), new ExEnchantSkillResult(1) });
    RequestExEnchantSkill.updateSkillShortcuts(activeChar, _skillId, _skillLvl);
  }
}