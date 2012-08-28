package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExConfirmVariationGemstone;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;

public final class RequestConfirmGemStone extends L2GameClientPacket
{
  private static final String _C__D0_2B_REQUESTCONFIRMGEMSTONE = "[C] D0:2B RequestConfirmGemStone";
  private int _targetItemObjId;
  private int _refinerItemObjId;
  private int _gemstoneItemObjId;
  private int _gemstoneCount;

  protected void readImpl()
  {
    _targetItemObjId = readD();
    _refinerItemObjId = readD();
    _gemstoneItemObjId = readD();
    _gemstoneCount = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    L2ItemInstance targetItem = (L2ItemInstance)L2World.getInstance().findObject(_targetItemObjId);
    L2ItemInstance refinerItem = (L2ItemInstance)L2World.getInstance().findObject(_refinerItemObjId);
    L2ItemInstance gemstoneItem = (L2ItemInstance)L2World.getInstance().findObject(_gemstoneItemObjId);

    if ((targetItem == null) || (refinerItem == null) || (gemstoneItem == null)) return;

    int gemstoneItemId = gemstoneItem.getItem().getItemId();
    if ((gemstoneItemId != 2130) && (gemstoneItemId != 2131))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM));
      return;
    }

    int itemGrade = targetItem.getItem().getItemGrade();
    switch (itemGrade)
    {
    case 2:
      if ((_gemstoneCount == 20) && (gemstoneItemId == 2130))
        break;
      activeChar.sendPacket(new SystemMessage(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT));
      return;
    case 3:
      if ((_gemstoneCount == 30) && (gemstoneItemId == 2130))
        break;
      activeChar.sendPacket(new SystemMessage(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT));
      return;
    case 4:
      if ((_gemstoneCount == 20) && (gemstoneItemId == 2131))
        break;
      activeChar.sendPacket(new SystemMessage(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT));
      return;
    case 5:
      if ((_gemstoneCount == 25) && (gemstoneItemId == 2131))
        break;
      activeChar.sendPacket(new SystemMessage(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT));
      return;
    }

    activeChar.sendPacket(new ExConfirmVariationGemstone(_gemstoneItemObjId, _gemstoneCount));
    activeChar.sendPacket(new SystemMessage(SystemMessageId.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN));
  }

  public String getType()
  {
    return "[C] D0:2B RequestConfirmGemStone";
  }
}