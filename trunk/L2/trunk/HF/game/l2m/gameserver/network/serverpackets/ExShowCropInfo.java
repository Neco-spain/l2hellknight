package l2m.gameserver.network.serverpackets;

import java.util.List;
import l2m.gameserver.model.Manor;
import l2m.gameserver.templates.manor.CropProcure;

public class ExShowCropInfo extends L2GameServerPacket
{
  private List<CropProcure> _crops;
  private int _manorId;

  public ExShowCropInfo(int manorId, List<CropProcure> crops)
  {
    _manorId = manorId;
    _crops = crops;
  }

  protected void writeImpl()
  {
    writeEx(36);
    writeC(0);
    writeD(_manorId);
    writeD(0);
    writeD(_crops.size());
    for (CropProcure crop : _crops)
    {
      writeD(crop.getId());
      writeQ(crop.getAmount());
      writeQ(crop.getStartAmount());
      writeQ(crop.getPrice());
      writeC(crop.getReward());
      writeD(Manor.getInstance().getSeedLevelByCrop(crop.getId()));

      writeC(1);
      writeD(Manor.getInstance().getRewardItem(crop.getId(), 1));

      writeC(1);
      writeD(Manor.getInstance().getRewardItem(crop.getId(), 2));
    }
  }
}