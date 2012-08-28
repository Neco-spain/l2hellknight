package net.sf.l2j.gameserver.network.serverpackets;

import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class WareHouseDepositList extends L2GameServerPacket
{
  public static final int PRIVATE = 1;
  public static final int CLAN = 2;
  public static final int CASTLE = 3;
  public static final int FREIGHT = 4;
  private static Logger _log = Logger.getLogger(WareHouseDepositList.class.getName());
  private static final String _S__53_WAREHOUSEDEPOSITLIST = "[S] 41 WareHouseDepositList";
  private L2PcInstance _activeChar;
  private int _playerAdena;
  private FastList<L2ItemInstance> _items;
  private int _whType;

  public WareHouseDepositList(L2PcInstance player, int type)
  {
    _activeChar = player;
    _whType = type;
    _playerAdena = _activeChar.getAdena();
    _items = new FastList();

    for (L2ItemInstance temp : _activeChar.getInventory().getAvailableItems(true)) {
      _items.add(temp);
    }

    if (_whType == 1)
    {
      for (L2ItemInstance temp : player.getInventory().getItems())
      {
        if ((temp != null) && (!temp.isEquipped()) && ((temp.isShadowItem()) || (temp.isAugmented())))
          _items.add(temp);
      }
    }
  }

  protected final void writeImpl()
  {
    writeC(65);

    writeH(_whType);
    writeD(_playerAdena);
    int count = _items.size();
    if (Config.DEBUG) _log.fine("count:" + count);
    writeH(count);

    for (L2ItemInstance item : _items)
    {
      writeH(item.getItem().getType1());
      writeD(item.getObjectId());
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
    return "[S] 41 WareHouseDepositList";
  }
}