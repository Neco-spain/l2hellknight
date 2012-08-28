package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import javolution.util.FastList;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.CropProcure;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.entity.Castle;

public class ExShowCropSetting extends L2GameServerPacket
{
  private static final String _S__FE_20_EXSHOWCROPSETTING = "[S] FE:20 ExShowCropSetting";
  private int _manorId;
  private int _count;
  private int[] _cropData;

  public void runImpl()
  {
  }

  public ExShowCropSetting(int manorId)
  {
    _manorId = manorId;
    Castle c = CastleManager.getInstance().getCastleById(_manorId);
    FastList crops = L2Manor.getInstance().getCropsForCastle(_manorId);
    _count = crops.size();
    _cropData = new int[_count * 14];
    int i = 0;
    for (Iterator i$ = crops.iterator(); i$.hasNext(); ) { int cr = ((Integer)i$.next()).intValue();
      _cropData[(i * 14 + 0)] = cr;
      _cropData[(i * 14 + 1)] = L2Manor.getInstance().getSeedLevelByCrop(cr);
      _cropData[(i * 14 + 2)] = L2Manor.getInstance().getRewardItem(cr, 1);
      _cropData[(i * 14 + 3)] = L2Manor.getInstance().getRewardItem(cr, 2);
      _cropData[(i * 14 + 4)] = L2Manor.getInstance().getCropPuchaseLimit(cr);
      _cropData[(i * 14 + 5)] = 0;
      _cropData[(i * 14 + 6)] = (L2Manor.getInstance().getCropBasicPrice(cr) * 60 / 100);
      _cropData[(i * 14 + 7)] = (L2Manor.getInstance().getCropBasicPrice(cr) * 10);
      CastleManorManager.CropProcure cropPr = c.getCrop(cr, 0);
      if (cropPr != null) {
        _cropData[(i * 14 + 8)] = cropPr.getStartAmount();
        _cropData[(i * 14 + 9)] = cropPr.getPrice();
        _cropData[(i * 14 + 10)] = cropPr.getReward();
      } else {
        _cropData[(i * 14 + 8)] = 0;
        _cropData[(i * 14 + 9)] = 0;
        _cropData[(i * 14 + 10)] = 0;
      }
      cropPr = c.getCrop(cr, 1);
      if (cropPr != null) {
        _cropData[(i * 14 + 11)] = cropPr.getStartAmount();
        _cropData[(i * 14 + 12)] = cropPr.getPrice();
        _cropData[(i * 14 + 13)] = cropPr.getReward();
      } else {
        _cropData[(i * 14 + 11)] = 0;
        _cropData[(i * 14 + 12)] = 0;
        _cropData[(i * 14 + 13)] = 0;
      }
      i++;
    }
  }

  public void writeImpl()
  {
    writeC(254);
    writeH(32);

    writeD(_manorId);
    writeD(_count);

    for (int i = 0; i < _count; i++) {
      writeD(_cropData[(i * 14 + 0)]);
      writeD(_cropData[(i * 14 + 1)]);
      writeC(1);
      writeD(_cropData[(i * 14 + 2)]);
      writeC(1);
      writeD(_cropData[(i * 14 + 3)]);

      writeD(_cropData[(i * 14 + 4)]);
      writeD(_cropData[(i * 14 + 5)]);
      writeD(_cropData[(i * 14 + 6)]);
      writeD(_cropData[(i * 14 + 7)]);

      writeD(_cropData[(i * 14 + 8)]);
      writeD(_cropData[(i * 14 + 9)]);
      writeC(_cropData[(i * 14 + 10)]);

      writeD(_cropData[(i * 14 + 11)]);
      writeD(_cropData[(i * 14 + 12)]);
      writeC(_cropData[(i * 14 + 13)]);
    }
  }

  public String getType()
  {
    return "[S] FE:20 ExShowCropSetting";
  }
}