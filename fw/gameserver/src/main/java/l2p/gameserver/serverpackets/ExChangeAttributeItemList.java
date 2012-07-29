package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInfo;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;

public class ExChangeAttributeItemList extends L2GameServerPacket {
    private List<ItemInfo> _itemsList = new ArrayList<ItemInfo>();

    int _itemId = 33502;

    public ExChangeAttributeItemList(Player player) {
        ItemInstance[] items = player.getInventory().getItems();
        for (ItemInstance item : items) {
            if (!(item.isWeapon() & item.getCrystalType().equals(ItemTemplate.Grade.S) & item.getAttributes().getValue() > 0))
                if (!(item.isWeapon() & item.getCrystalType().equals(ItemTemplate.Grade.S80) & item.getAttributes().getValue() > 0))
                    if (!(item.isWeapon() & item.getCrystalType().equals(ItemTemplate.Grade.S84) & item.getAttributes().getValue() > 0)) {
                        continue;
                    }
            _itemsList.add(new ItemInfo(item));
        }
    }

    protected void writeImpl() {
        writeEx(0x117);
        writeD(_itemId);
        writeD(_itemsList.size());
        for (ItemInfo item : _itemsList)
            writeItemInfo(item);
    }
}
