package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.Player;
import l2m.gameserver.templates.Henna;

public class HennaUnequipList extends L2GameServerPacket
{
  private int _emptySlots;
  private long _adena;
  private List<Henna> availHenna = new ArrayList(3);

  public HennaUnequipList(Player player)
  {
    _adena = player.getAdena();
    _emptySlots = player.getHennaEmptySlots();
    for (int i = 1; i <= 3; i++)
      if (player.getHenna(i) != null)
        availHenna.add(player.getHenna(i));
  }

  protected final void writeImpl()
  {
    writeC(230);

    writeQ(_adena);
    writeD(_emptySlots);
    writeD(availHenna.size());
    for (Henna henna : availHenna)
    {
      writeD(henna.getSymbolId());
      writeD(henna.getDyeId());
      writeQ(henna.getDrawCount());
      writeQ(henna.getPrice());
      writeD(1);
    }
  }
}