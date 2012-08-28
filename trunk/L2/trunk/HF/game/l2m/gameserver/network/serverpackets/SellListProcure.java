package l2m.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.residence.Castle;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.templates.manor.CropProcure;

public class SellListProcure extends L2GameServerPacket
{
  private long _money;
  private Map<ItemInstance, Long> _sellList = new HashMap();
  private List<CropProcure> _procureList = new ArrayList();
  private int _castle;

  public SellListProcure(Player player, int castleId)
  {
    _money = player.getAdena();
    _castle = castleId;
    _procureList = ((Castle)ResidenceHolder.getInstance().getResidence(Castle.class, _castle)).getCropProcure(0);
    for (CropProcure c : _procureList)
    {
      ItemInstance item = player.getInventory().getItemByItemId(c.getId());
      if ((item != null) && (c.getAmount() > 0L))
        _sellList.put(item, Long.valueOf(c.getAmount()));
    }
  }

  protected final void writeImpl()
  {
    writeC(239);
    writeQ(_money);
    writeD(0);
    writeH(_sellList.size());

    for (ItemInstance item : _sellList.keySet())
    {
      writeH(item.getTemplate().getType1());
      writeD(item.getObjectId());
      writeD(item.getItemId());
      writeQ(((Long)_sellList.get(item)).longValue());
      writeH(item.getTemplate().getType2ForPackets());
      writeH(0);
      writeQ(0L);
    }
  }
}