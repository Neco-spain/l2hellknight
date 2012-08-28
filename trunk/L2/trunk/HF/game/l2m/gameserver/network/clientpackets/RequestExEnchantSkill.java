package l2m.gameserver.network.clientpackets;

import l2p.commons.util.Rnd;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.actor.instances.player.ShortCut;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.base.EnchantSkillLearn;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.scripts.Functions;
import l2m.gameserver.network.serverpackets.ExEnchantSkillInfo;
import l2m.gameserver.network.serverpackets.ExEnchantSkillResult;
import l2m.gameserver.network.serverpackets.ShortCutRegister;
import l2m.gameserver.network.serverpackets.SkillList;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.data.tables.SkillTreeTable;
import l2m.gameserver.utils.Log;

public class RequestExEnchantSkill extends L2GameClientPacket
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
    if (skill == null)
    {
      activeChar.sendMessage("Internal error: not found skill level");
      return;
    }

    int[] cost = sl.getCost();
    int requiredSp = cost[1] * 1 * sl.getCostMult();
    int requiredAdena = cost[0] * 1 * sl.getCostMult();
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

    if (_skillLvl % 100 == 1)
    {
      if (Functions.getItemCount(activeChar, 6622) == 0L)
      {
        activeChar.sendPacket(Msg.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
        return;
      }
      Functions.removeItem(activeChar, 6622, 1L);
    }

    if (Rnd.chance(rate))
    {
      activeChar.addExpAndSp(0L, -1 * requiredSp);
      Functions.removeItem(activeChar, 57, requiredAdena);
      activeChar.sendPacket(new IStaticPacket[] { new SystemMessage(538).addNumber(requiredSp), new SystemMessage(1440).addSkillName(_skillId, _skillLvl), new SkillList(activeChar), new ExEnchantSkillResult(1) });
      Log.add(activeChar.getName() + "|Successfully enchanted|" + _skillId + "|to+" + _skillLvl + "|" + rate, "enchant_skills");
    }
    else
    {
      skill = SkillTable.getInstance().getInfo(_skillId, sl.getBaseLevel());
      activeChar.sendPacket(new IStaticPacket[] { new SystemMessage(1441).addSkillName(_skillId, _skillLvl), new ExEnchantSkillResult(0) });
      Log.add(activeChar.getName() + "|Failed to enchant|" + _skillId + "|to+" + _skillLvl + "|" + rate, "enchant_skills");
    }
    activeChar.addSkill(skill, true);
    updateSkillShortcuts(activeChar, _skillId, _skillLvl);
    activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, activeChar.getSkillDisplayLevel(Integer.valueOf(_skillId))));
  }

  protected static void updateSkillShortcuts(Player player, int skillId, int skillLevel)
  {
    for (ShortCut sc : player.getAllShortCuts())
      if ((sc.getId() == skillId) && (sc.getType() == 2))
      {
        ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, 1);
        player.sendPacket(new ShortCutRegister(player, newsc));
        player.registerShortCut(newsc);
      }
  }
}