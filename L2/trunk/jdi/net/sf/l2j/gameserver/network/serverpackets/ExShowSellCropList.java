package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.CropProcure;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExShowSellCropList extends L2GameServerPacket
{
  private static final String _S__FE_21_EXSHOWSELLCROPLIST = "[S] FE:21 ExShowSellCropList";
  private int _manorId = 1;
  private FastMap<Integer, L2ItemInstance> _cropsItems;
  private FastMap<Integer, CastleManorManager.CropProcure> _castleCrops;

  public ExShowSellCropList(L2PcInstance player, int manorId, FastList<CastleManorManager.CropProcure> crops)
  {
    _manorId = manorId;
    _castleCrops = new FastMap();
    _cropsItems = new FastMap();

    FastList allCrops = L2Manor.getInstance().getAllCrops();
    for (Iterator i$ = allCrops.iterator(); i$.hasNext(); ) { int cropId = ((Integer)i$.next()).intValue();
      L2ItemInstance item = player.getInventory().getItemByItemId(cropId);
      if (item != null) {
        _cropsItems.put(Integer.valueOf(cropId), item);
      }
    }

    for (CastleManorManager.CropProcure crop : crops)
      if ((_cropsItems.containsKey(Integer.valueOf(crop.getId()))) && (crop.getAmount() > 0))
        _castleCrops.put(Integer.valueOf(crop.getId()), crop);
  }

  public void runImpl()
  {
  }

  public void writeImpl()
  {
    writeC(254);
    writeH(33);

    writeD(_manorId);
    writeD(_cropsItems.size());

    for (L2ItemInstance item : _cropsItems.values()) {
      writeD(item.getObjectId());
      writeD(item.getItemId());
      writeD(L2Manor.getInstance().getSeedLevelByCrop(item.getItemId()));
      writeC(1);
      writeD(L2Manor.getInstance().getRewardItem(item.getItemId(), 1));
      writeC(1);
      writeD(L2Manor.getInstance().getRewardItem(item.getItemId(), 2));

      if (_castleCrops.containsKey(Integer.valueOf(item.getItemId()))) {
        CastleManorManager.CropProcure crop = (CastleManorManager.CropProcure)_castleCrops.get(Integer.valueOf(item.getItemId()));
        writeD(_manorId);
        writeD(crop.getAmount());
        writeD(crop.getPrice());
        writeC(crop.getReward());
      } else {
        writeD(-1);
        writeD(0);
        writeD(0);
        writeC(0);
      }
      writeD(item.getCount());
    }
  }

  public String getType()
  {
    return "[S] FE:21 ExShowSellCropList";
  }
}