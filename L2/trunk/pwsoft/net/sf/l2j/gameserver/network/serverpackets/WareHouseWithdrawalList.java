package net.sf.l2j.gameserver.network.serverpackets;

import java.util.NoSuchElementException;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.cache.Static;
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
  private L2PcInstance _activeChar;
  private int _playerAdena;
  private FastTable<L2ItemInstance> _items;
  private int _whType;
  private boolean can_writeImpl = false;

  public WareHouseWithdrawalList(L2PcInstance player, int type)
  {
    _activeChar = player;
    _whType = type;
    ItemContainer warehouse = player.getActiveWarehouse();
    _items = new FastTable();
    switch (_whType)
    {
    case 1:
      _items.addAll(warehouse.listItems(1));
      break;
    case 2:
    case 3:
      _items.addAll(warehouse.listItems(2));
      break;
    case 4:
      _items.addAll(warehouse.listItems(4));
      break;
    default:
      throw new NoSuchElementException("Invalid value of 'type' argument");
    }

    _playerAdena = _activeChar.getAdena();

    if (_items.size() == 0)
    {
      player.sendPacket(Static.NO_ITEM_DEPOSITED_IN_WH);
      return;
    }

    can_writeImpl = true;
  }

  protected final void writeImpl()
  {
    if (!can_writeImpl) {
      return;
    }
    writeC(66);

    writeH(_whType);
    writeD(_playerAdena);
    writeH(_items.size());

    int i = 0; for (int n = _items.size(); i < n; i++)
    {
      L2ItemInstance item = (L2ItemInstance)_items.get(i);
      if ((item == null) || (item.getItem() == null)) {
        continue;
      }
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
        writeD(item.getAugmentation().getAugmentationId() & 0xFFFF);
        writeD(item.getAugmentation().getAugmentationId() >> 16);
      }
      else
      {
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
    return "S.WareHouseWithdrawalList";
  }
}