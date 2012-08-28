package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastList;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.CropProcure;
import net.sf.l2j.gameserver.model.L2Manor;

public class ExShowCropInfo extends L2GameServerPacket
{
  private static final String _S__FE_1C_EXSHOWSEEDINFO = "[S] FE:1D ExShowCropInfo";
  private FastList<CastleManorManager.CropProcure> _crops;
  private int _manorId;

  public ExShowCropInfo(int manorId, FastList<CastleManorManager.CropProcure> crops)
  {
    _manorId = manorId;
    _crops = crops;
    if (_crops == null)
      _crops = new FastList();
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(29);
    writeC(0);
    writeD(_manorId);
    writeD(0);
    writeD(_crops.size());
    for (CastleManorManager.CropProcure crop : _crops) {
      writeD(crop.getId());
      writeD(crop.getAmount());
      writeD(crop.getStartAmount());
      writeD(crop.getPrice());
      writeC(crop.getReward());
      writeD(L2Manor.getInstance().getSeedLevelByCrop(crop.getId()));
      writeC(1);
      writeD(L2Manor.getInstance().getRewardItem(crop.getId(), 1));
      writeC(1);
      writeD(L2Manor.getInstance().getRewardItem(crop.getId(), 2));
    }
  }

  public String getType()
  {
    return "[S] FE:1D ExShowCropInfo";
  }
}