package net.sf.l2j.gameserver.network.serverpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class WareHouseWithdrawalList extends L2GameServerPacket
{
  public static final int PRIVATE = 1;
  public static final int CLAN = 2;
  public static final int CASTLE = 3;
  public static final int FREIGHT = 4;
  private static Logger _log = Logger.getLogger(WareHouseWithdrawalList.class.getName());
  private static final String _S__54_WAREHOUSEWITHDRAWALLIST = "[S] 42 WareHouseWithdrawalList";
  private L2PcInstance _activeChar;
  private int _playerAdena;
  private L2ItemInstance[] _items;
  private int _whType;

  public WareHouseWithdrawalList(L2PcInstance player, int type)
  {
    _activeChar = player;
    _whType = type;

    _playerAdena = _activeChar.getAdena();
    if (_activeChar.getActiveWarehouse() == null)
    {
      _log.warning("error while sending withdraw request to: " + _activeChar.getName());
      return;
    }
    _items = _activeChar.getActiveWarehouse().getItems();

    if (Config.DEBUG)
      for (L2ItemInstance item : _items)
        _log.fine("item:" + item.getItem().getName() + " type1:" + item.getItem().getType1() + " type2:" + item.getItem().getType2());
  }

  protected final void writeImpl()
  {
    writeC(66);

    writeH(_whType);
    writeD(_playerAdena);
    writeH(_items.length);

    for (L2ItemInstance item : _items)
    {
      writeH(item.getItem().getType1());
      writeD(0);
      writeD(item.getItemId());
      writeD(item.getCount());
      writeH(item.getItem().getType2());
      writeH(0);
      writeD(item.getItem().getBodyPart());
      writeH(item.getEnchantLevel());
      writeH(0);
      writeH(0);
      writeD(item.getObjectId());
      if (item.isAugmented())
      {
        writeD(0xFFFF & item.getAugmentation().getAugmentationId());
        writeD(item.getAugmentation().getAugmentationId() >> 16);
      } else {
        writeQ(0L);
      }
    }
  }

  public String getType()
  {
    return "[S] 42 WareHouseWithdrawalList";
  }
}