package l2p.gameserver.clientpackets;

import java.util.List;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.EnchantSkillLearn;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExEnchantSkillInfoDetail;
import l2p.gameserver.tables.SkillTreeTable;

public final class RequestExEnchantSkillInfoDetail extends L2GameClientPacket
{
  private static final int TYPE_NORMAL_ENCHANT = 0;
  private static final int TYPE_SAFE_ENCHANT = 1;
  private static final int TYPE_UNTRAIN_ENCHANT = 2;
  private static final int TYPE_CHANGE_ENCHANT = 3;
  private int _type;
  private int _skillId;
  private int _skillLvl;

  protected void readImpl()
  {
    _type = readD();
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

    int bookId = 0;
    int sp = 0;
    int adenaCount = 0;
    double spMult = 1.0D;

    EnchantSkillLearn esd = null;

    switch (_type)
    {
    case 0:
      if (_skillLvl % 100 == 1)
        bookId = 6622;
      esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
      break;
    case 1:
      bookId = 9627;
      esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
      spMult = 5.0D;
      break;
    case 2:
      bookId = 9625;
      esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl + 1);
      break;
    case 3:
      bookId = 9626;
      esd = (EnchantSkillLearn)SkillTreeTable.getEnchantsForChange(_skillId, _skillLvl).get(0);
      spMult = 0.2000000029802322D;
    }

    if (esd == null) {
      return;
    }
    spMult *= esd.getCostMult();
    int[] cost = esd.getCost();

    sp = (int)(cost[1] * spMult);

    if (_type != 2) {
      adenaCount = (int)(cost[0] * spMult);
    }

    activeChar.sendPacket(new ExEnchantSkillInfoDetail(_skillId, _skillLvl, sp, esd.getRate(activeChar), bookId, adenaCount));
  }
}