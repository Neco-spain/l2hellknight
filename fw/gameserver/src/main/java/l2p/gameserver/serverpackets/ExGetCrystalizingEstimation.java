package l2p.gameserver.serverpackets;

import l2p.gameserver.model.items.CrystallizationItem;

import java.util.ArrayList;
import java.util.List;

public class ExGetCrystalizingEstimation extends L2GameServerPacket {

    private List<CrystallizationItem> _items;

    @Override
    protected void writeImpl()
    {
        writeEx(0xE0);
        writeD(_items.size());

        for (CrystallizationItem i : _items)
        {
            writeD(i.getItemId());
            writeQ(i.getCount());
            writeF(i.getChance());
        }
    }

    public void addCrystallizationItem(CrystallizationItem i)
    {
        if (_items == null)
            _items = new ArrayList<CrystallizationItem>();

        _items.add(i);
    }
}