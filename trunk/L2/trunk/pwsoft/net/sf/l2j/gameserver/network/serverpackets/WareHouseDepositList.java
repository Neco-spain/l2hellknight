package net.sf.l2j.gameserver.network.serverpackets;

import java.util.logging.Logger;
import javolution.util.FastTable;
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
  private L2PcInstance _activeChar;
  private int _playerAdena;
  private FastTable<L2ItemInstance> _items;
  private int _whType;

  public WareHouseDepositList(L2PcInstance player, int type)
  {
    _activeChar = player;
    _whType = type;
    _playerAdena = _activeChar.getAdena();
    _items = new FastTable();

    for (L2ItemInstance item : player.getInventory().getAllItems())
      if (item != null) if (item.canBeStored(player, _whType == 1))
          _items.add(item);
  }

  protected final void writeImpl()
  {
    writeC(65);

    writeH(_whType);
    writeD(_playerAdena);

    writeH(_items.size());

    for (int i = _items.size() - 1; i > -1; i--)
    {
      L2ItemInstance item = (L2ItemInstance)_items.get(i);
      if ((item == null) || (item.getItem() == null)) {
        continue;
      }
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

  public void gc()
  {
    _items.clear();
    _items = null;
  }

  public String getType()
  {
    return "S.WareHouseDepositList";
  }
}