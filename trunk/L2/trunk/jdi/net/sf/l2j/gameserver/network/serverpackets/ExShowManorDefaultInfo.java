package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Manor;

public class ExShowManorDefaultInfo extends L2GameServerPacket
{
  private static final String _S__FE_1C_EXSHOWSEEDINFO = "[S] FE:1E ExShowManorDefaultInfo";
  private FastList<Integer> _crops = null;

  public ExShowManorDefaultInfo() {
    _crops = L2Manor.getInstance().getAllCrops();
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(30);
    writeC(0);
    writeD(_crops.size());
    for (Iterator i$ = _crops.iterator(); i$.hasNext(); ) { int cropId = ((Integer)i$.next()).intValue();
      writeD(cropId);
      writeD(L2Manor.getInstance().getSeedLevelByCrop(cropId));
      writeD(L2Manor.getInstance().getSeedBasicPriceByCrop(cropId));
      writeD(L2Manor.getInstance().getCropBasicPrice(cropId));
      writeC(1);
      writeD(L2Manor.getInstance().getRewardItem(cropId, 1));
      writeC(1);
      writeD(L2Manor.getInstance().getRewardItem(cropId, 2));
    }
  }

  public String getType()
  {
    return "[S] FE:1E ExShowManorDefaultInfo";
  }
}