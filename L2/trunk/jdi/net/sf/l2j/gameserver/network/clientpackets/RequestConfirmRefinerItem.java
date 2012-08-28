package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExConfirmVariationRefiner;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;

public class RequestConfirmRefinerItem extends L2GameClientPacket
{
  private static final String _C__D0_2A_REQUESTCONFIRMREFINERITEM = "[C] D0:2A RequestConfirmRefinerItem";
  private static final int GEMSTONE_D = 2130;
  private static final int GEMSTONE_C = 2131;
  private int _targetItemObjId;
  private int _refinerItemObjId;

  protected void readImpl()
  {
    _targetItemObjId = readD();
    _refinerItemObjId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    L2ItemInstance targetItem = (L2ItemInstance)L2World.getInstance().findObject(_targetItemObjId);
    L2ItemInstance refinerItem = (L2ItemInstance)L2World.getInstance().findObject(_refinerItemObjId);

    if ((targetItem == null) || (refinerItem == null)) return;

    int itemGrade = targetItem.getItem().getItemGrade();
    int refinerItemId = refinerItem.getItem().getItemId();

    if ((refinerItemId < 8723) || (refinerItemId > 8762))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM));
      return;
    }

    int gemstoneCount = 0;
    int gemstoneItemId = 0;
    int lifeStoneLevel = getLifeStoneLevel(refinerItemId);
    SystemMessage sm = new SystemMessage(SystemMessageId.REQUIRES_S1_S2);
    switch (itemGrade)
    {
    case 2:
      gemstoneCount = 20;
      gemstoneItemId = 2130;
      sm.addNumber(gemstoneCount);
      sm.addString("Gemstone D");
      break;
    case 3:
      if (lifeStoneLevel < 3)
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM));
        return;
      }
      gemstoneCount = 30;
      gemstoneItemId = 2130;
      sm.addNumber(gemstoneCount);
      sm.addString("Gemstone D");
      break;
    case 4:
      if (lifeStoneLevel < 6)
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM));
        return;
      }
      gemstoneCount = 20;
      gemstoneItemId = 2131;
      sm.addNumber(gemstoneCount);
      sm.addString("Gemstone C");
      break;
    case 5:
      if (lifeStoneLevel != 10)
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM));
        return;
      }
      gemstoneCount = 25;
      gemstoneItemId = 2131;
      sm.addNumber(gemstoneCount);
      sm.addString("Gemstone C");
    }

    activeChar.sendPacket(new ExConfirmVariationRefiner(_refinerItemObjId, refinerItemId, gemstoneItemId, gemstoneCount));

    activeChar.sendPacket(sm);
  }

  private int getLifeStoneGrade(int itemId)
  {
    itemId -= 8723;
    if (itemId < 10) return 0;
    if (itemId < 20) return 1;
    if (itemId < 30) return 2;
    return 3;
  }

  private int getLifeStoneLevel(int itemId)
  {
    itemId -= 10 * getLifeStoneGrade(itemId);
    itemId -= 8722;
    return itemId;
  }

  public String getType()
  {
    return "[C] D0:2A RequestConfirmRefinerItem";
  }
}