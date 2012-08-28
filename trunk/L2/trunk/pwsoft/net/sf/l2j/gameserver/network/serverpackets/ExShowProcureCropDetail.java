package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import java.util.Set;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.CropProcure;
import net.sf.l2j.gameserver.model.entity.Castle;

public class ExShowProcureCropDetail extends L2GameServerPacket
{
  private int _cropId;
  private FastMap<Integer, CastleManorManager.CropProcure> _castleCrops;

  public ExShowProcureCropDetail(int cropId)
  {
    _cropId = cropId;
    _castleCrops = new FastMap();

    for (Castle c : CastleManager.getInstance().getCastles())
    {
      CastleManorManager.CropProcure cropItem = c.getCrop(_cropId, 0);
      if ((cropItem != null) && (cropItem.getAmount() > 0))
        _castleCrops.put(Integer.valueOf(c.getCastleId()), cropItem);
    }
  }

  public void runImpl()
  {
  }

  public void writeImpl()
  {
    writeC(254);
    writeH(34);

    writeD(_cropId);
    writeD(_castleCrops.size());

    for (Iterator i$ = _castleCrops.keySet().iterator(); i$.hasNext(); ) { int manorId = ((Integer)i$.next()).intValue();

      CastleManorManager.CropProcure crop = (CastleManorManager.CropProcure)_castleCrops.get(Integer.valueOf(manorId));
      writeD(manorId);
      writeD(crop.getAmount());
      writeD(crop.getPrice());
      writeC(crop.getReward());
    }
  }

  public void gc()
  {
    _castleCrops.clear();
    _castleCrops = null;
  }
}