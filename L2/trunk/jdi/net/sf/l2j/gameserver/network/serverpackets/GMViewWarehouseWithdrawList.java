package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcWarehouse;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;

public class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
  private static final String _S__95_GMViewWarehouseWithdrawList = "[S] 95 GMViewWarehouseWithdrawList";
  private L2ItemInstance[] _items;
  private String _playerName;
  private L2PcInstance _activeChar;
  private int _money;

  public GMViewWarehouseWithdrawList(L2PcInstance cha)
  {
    _activeChar = cha;
    _items = _activeChar.getWarehouse().getItems();
    _playerName = _activeChar.getName();
    _money = _activeChar.getAdena();
  }

  protected final void writeImpl()
  {
    writeC(149);
    writeS(_playerName);
    writeD(_money);
    writeH(_items.length);

    for (L2ItemInstance item : _items)
    {
      writeH(item.getItem().getType1());

      writeD(item.getObjectId());
      writeD(item.getItemId());
      writeD(item.getCount());
      writeH(item.getItem().getType2());
      writeH(item.getCustomType1());

      switch (item.getItem().getType2())
      {
      case 0:
        writeD(item.getItem().getBodyPart());
        writeH(item.getEnchantLevel());
        writeH(((L2Weapon)item.getItem()).getSoulShotCount());
        writeH(((L2Weapon)item.getItem()).getSpiritShotCount());
        break;
      case 1:
      case 2:
      case 6:
      case 7:
      case 8:
      case 9:
        writeD(item.getItem().getBodyPart());
        writeH(item.getEnchantLevel());
        writeH(0);
        writeH(0);
      case 3:
      case 4:
      case 5:
      }
      writeD(item.getObjectId());

      switch (item.getItem().getType2())
      {
      case 0:
        if (item.isAugmented())
        {
          writeD(0xFFFF & item.getAugmentation().getAugmentationId());
          writeD(item.getAugmentation().getAugmentationId() >> 16);
        }
        else
        {
          writeD(0);
          writeD(0);
        }

        break;
      case 1:
      case 2:
      case 6:
      case 7:
      case 8:
      case 9:
        writeD(0);
        writeD(0);
      case 3:
      case 4:
      case 5:
      }
    }
  }

  public String getType() {
    return "[S] 95 GMViewWarehouseWithdrawList";
  }
}