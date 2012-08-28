package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.base.EnchantSkillLearn;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExEnchantSkillInfo;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.data.tables.SkillTreeTable;

public class RequestExEnchantSkillInfo extends L2GameClientPacket
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
    if (_skillLvl > 100)
    {
      EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
      if (sl == null)
      {
        activeChar.sendMessage("Not found enchant info for this skill");
        return;
      }

      Skill skill = SkillTable.getInstance().getInfo(_skillId, SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel()));

      if ((skill == null) || (skill.getId() != _skillId))
      {
        activeChar.sendMessage("This skill doesn't yet have enchant info in Datapack");
        return;
      }

      if (activeChar.getSkillLevel(Integer.valueOf(_skillId)) != skill.getLevel())
      {
        activeChar.sendMessage("Skill not found");
        return;
      }
    }
    else if (activeChar.getSkillLevel(Integer.valueOf(_skillId)) != _skillLvl)
    {
      activeChar.sendMessage("Skill not found");
      return;
    }

    sendPacket(new ExEnchantSkillInfo(_skillId, _skillLvl));
  }
}