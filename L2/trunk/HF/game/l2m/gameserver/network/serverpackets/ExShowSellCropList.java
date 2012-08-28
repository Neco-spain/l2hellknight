package l2m.gameserver.network.serverpackets;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import l2m.gameserver.model.Manor;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.templates.manor.CropProcure;

public class ExShowSellCropList extends L2GameServerPacket
{
  private int _manorId = 1;
  private Map<Integer, ItemInstance> _cropsItems;
  private Map<Integer, CropProcure> _castleCrops;

  public ExShowSellCropList(Player player, int manorId, List<CropProcure> crops)
  {
    _manorId = manorId;
    _castleCrops = new TreeMap();
    _cropsItems = new TreeMap();

    List allCrops = Manor.getInstance().getAllCrops();
    for (Iterator i$ = allCrops.iterator(); i$.hasNext(); ) { int cropId = ((Integer)i$.next()).intValue();

      ItemInstance item = player.getInventory().getItemByItemId(cropId);
      if (item != null) {
        _cropsItems.put(Integer.valueOf(cropId), item);
      }
    }
    for (CropProcure crop : crops)
      if ((_cropsItems.containsKey(Integer.valueOf(crop.getId()))) && (crop.getAmount() > 0L))
        _castleCrops.put(Integer.valueOf(crop.getId()), crop);
  }

  public void writeImpl()
  {
    writeEx(44);

    writeD(_manorId);
    writeD(_cropsItems.size());

    for (ItemInstance item : _cropsItems.values())
    {
      writeD(item.getObjectId());
      writeD(item.getItemId());
      writeD(Manor.getInstance().getSeedLevelByCrop(item.getItemId()));

      writeC(1);
      writeD(Manor.getInstance().getRewardItem(item.getItemId(), 1));

      writeC(1);
      writeD(Manor.getInstance().getRewardItem(item.getItemId(), 2));

      if (_castleCrops.containsKey(Integer.valueOf(item.getItemId())))
      {
        CropProcure crop = (CropProcure)_castleCrops.get(Integer.valueOf(item.getItemId()));
        writeD(_manorId);
        writeQ(crop.getAmount());
        writeQ(crop.getPrice());
        writeC(crop.getReward());
      }
      else
      {
        writeD(-1);
        writeQ(0L);
        writeQ(0L);
        writeC(0);
      }
      writeQ(item.getCount());
    }
  }
}