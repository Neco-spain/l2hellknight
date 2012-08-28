package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.data.xml.holder.HennaHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.templates.Henna;

public class HennaEquipList extends L2GameServerPacket
{
  private int _emptySlots;
  private long _adena;
  private List<Henna> _hennas = new ArrayList();

  public HennaEquipList(Player player)
  {
    _adena = player.getAdena();
    _emptySlots = player.getHennaEmptySlots();

    List list = HennaHolder.getInstance().generateList(player);
    for (Henna element : list)
      if (player.getInventory().getItemByItemId(element.getDyeId()) != null)
        _hennas.add(element);
  }

  protected final void writeImpl()
  {
    writeC(238);

    writeQ(_adena);
    writeD(_emptySlots);
    if (_hennas.size() != 0)
    {
      writeD(_hennas.size());
      for (Henna henna : _hennas)
      {
        writeD(henna.getSymbolId());
        writeD(henna.getDyeId());
        writeQ(henna.getDrawCount());
        writeQ(henna.getPrice());
        writeD(1);
      }
    }
    else
    {
      writeD(1);
      writeD(0);
      writeD(0);
      writeQ(0L);
      writeQ(0L);
      writeD(0);
    }
  }
}