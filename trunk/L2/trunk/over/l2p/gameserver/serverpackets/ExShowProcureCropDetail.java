package l2p.gameserver.serverpackets;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.templates.manor.CropProcure;

public class ExShowProcureCropDetail extends L2GameServerPacket
{
  private int _cropId;
  private Map<Integer, CropProcure> _castleCrops;

  public ExShowProcureCropDetail(int cropId)
  {
    _cropId = cropId;
    _castleCrops = new TreeMap();

    List castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
    for (Castle c : castleList)
    {
      CropProcure cropItem = c.getCrop(_cropId, 0);
      if ((cropItem != null) && (cropItem.getAmount() > 0L))
        _castleCrops.put(Integer.valueOf(c.getId()), cropItem);
    }
  }

  public void writeImpl()
  {
    writeEx(120);

    writeD(_cropId);
    writeD(_castleCrops.size());

    for (Iterator i$ = _castleCrops.keySet().iterator(); i$.hasNext(); ) { int manorId = ((Integer)i$.next()).intValue();

      CropProcure crop = (CropProcure)_castleCrops.get(Integer.valueOf(manorId));
      writeD(manorId);
      writeQ(crop.getAmount());
      writeQ(crop.getPrice());
      writeC(crop.getReward());
    }
  }
}