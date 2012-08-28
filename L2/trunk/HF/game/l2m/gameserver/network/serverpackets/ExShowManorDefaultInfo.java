package l2m.gameserver.network.serverpackets;

import java.util.Iterator;
import java.util.List;
import l2m.gameserver.model.Manor;

public class ExShowManorDefaultInfo extends L2GameServerPacket
{
  private List<Integer> _crops = null;

  public ExShowManorDefaultInfo()
  {
    _crops = Manor.getInstance().getAllCrops();
  }

  protected void writeImpl()
  {
    writeEx(37);
    writeC(0);
    writeD(_crops.size());
    for (Iterator i$ = _crops.iterator(); i$.hasNext(); ) { int cropId = ((Integer)i$.next()).intValue();

      writeD(cropId);
      writeD(Manor.getInstance().getSeedLevelByCrop(cropId));
      writeD(Manor.getInstance().getSeedBasicPriceByCrop(cropId));
      writeD(Manor.getInstance().getCropBasicPrice(cropId));
      writeC(1);
      writeD(Manor.getInstance().getRewardItem(cropId, 1));
      writeC(1);
      writeD(Manor.getInstance().getRewardItem(cropId, 2));
    }
  }
}