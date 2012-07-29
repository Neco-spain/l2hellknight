package l2p.gameserver.serverpackets;

import l2p.commons.lang.ArrayUtils;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInfo;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.Warehouse.ItemClassComparator;
import l2p.gameserver.model.items.Warehouse.WarehouseType;

import java.util.ArrayList;
import java.util.List;


public class WareHouseDepositList extends L2GameServerPacket {
    private int _whtype;
    private long _adena;
    private List<ItemInfo> _itemList;

    public WareHouseDepositList(Player cha, WarehouseType whtype) {
        _whtype = whtype.ordinal();
        _adena = cha.getAdena();

        ItemInstance[] items = cha.getInventory().getItems();
        ArrayUtils.eqSort(items, ItemClassComparator.getInstance());
        _itemList = new ArrayList<ItemInfo>(items.length);
        for (ItemInstance item : items){
            if (item.canBeStored(cha, _whtype == 1)) {
                _itemList.add(new ItemInfo(item));
            }
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0x41);
        writeH(_whtype);
        writeQ(_adena);
        writeD(0x00); // кол-во занятых слотов в вх
        writeH(0x00); // кол-во следующего списка (itemId) стекуемых предметов, находящихся в хранилище.
			//{
			//	writeD(0x00); itemId
			//}
		writeH(_itemList.size());
        for (ItemInfo item : _itemList) {
            writeItemInfo(item);
            writeD(item.getObjectId());
        }
    }
}