package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
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
  private int _targetItemObjId;
  private int _refinerItemObjId;

  protected void readImpl()
  {
    _targetItemObjId = readD();
    _refinerItemObjId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    L2ItemInstance targetItem = (L2ItemInstance)L2World.getInstance().findObject(_targetItemObjId);
    L2ItemInstance refinerItem = (L2ItemInstance)L2World.getInstance().findObject(_refinerItemObjId);

    if ((targetItem == null) || (refinerItem == null)) return;

    int itemGrade = targetItem.getItem().getItemGrade();
    int refinerItemId = refinerItem.getItem().getItemId();

    if ((refinerItemId < 8723) || (refinerItemId > 8762))
    {
      player.sendPacket(Static.THIS_IS_NOT_A_SUITABLE_ITEM);
      return;
    }

    int gemstoneCount = 0;
    int gemstoneItemId = 0;
    int lifeStoneLevel = getLifeStoneLevel(refinerItemId);
    SystemMessage sm = SystemMessage.id(SystemMessageId.REQUIRES_S1_S2);
    switch (itemGrade)
    {
    case 2:
      gemstoneCount = 20;
      gemstoneItemId = 2130;
      sm.addNumber(gemstoneCount).addString("Gemstone D");
      break;
    case 3:
      if (lifeStoneLevel < 3)
      {
        player.sendPacket(Static.THIS_IS_NOT_A_SUITABLE_ITEM);
        return;
      }
      gemstoneCount = 30;
      gemstoneItemId = 2130;
      sm.addNumber(gemstoneCount).addString("Gemstone D");
      break;
    case 4:
      if (lifeStoneLevel < 6)
      {
        player.sendPacket(Static.THIS_IS_NOT_A_SUITABLE_ITEM);
        return;
      }
      gemstoneCount = 20;
      gemstoneItemId = 2131;
      sm.addNumber(gemstoneCount).addString("Gemstone C");
      break;
    case 5:
      if (lifeStoneLevel != 10)
      {
        player.sendPacket(Static.THIS_IS_NOT_A_SUITABLE_ITEM);
        return;
      }
      gemstoneCount = 25;
      gemstoneItemId = 2131;
      sm.addNumber(gemstoneCount).addString("Gemstone C");
    }

    player.sendPacket(new ExConfirmVariationRefiner(_refinerItemObjId, refinerItemId, gemstoneItemId, gemstoneCount));
    player.sendPacket(sm);
    sm = null;
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
}