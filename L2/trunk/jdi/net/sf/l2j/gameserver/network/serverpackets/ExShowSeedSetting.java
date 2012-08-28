package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import javolution.util.FastList;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.SeedProduction;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.entity.Castle;

public class ExShowSeedSetting extends L2GameServerPacket
{
  private static final String _S__FE_1F_EXSHOWSEEDSETTING = "[S] FE:1F ExShowSeedSetting";
  private int _manorId;
  private int _count;
  private int[] _seedData;

  public void runImpl()
  {
  }

  public ExShowSeedSetting(int manorId)
  {
    _manorId = manorId;
    Castle c = CastleManager.getInstance().getCastleById(_manorId);
    FastList seeds = L2Manor.getInstance().getSeedsForCastle(_manorId);
    _count = seeds.size();
    _seedData = new int[_count * 12];
    int i = 0;
    for (Iterator i$ = seeds.iterator(); i$.hasNext(); ) { int s = ((Integer)i$.next()).intValue();
      _seedData[(i * 12 + 0)] = s;
      _seedData[(i * 12 + 1)] = L2Manor.getInstance().getSeedLevel(s);
      _seedData[(i * 12 + 2)] = L2Manor.getInstance().getRewardItemBySeed(s, 1);
      _seedData[(i * 12 + 3)] = L2Manor.getInstance().getRewardItemBySeed(s, 2);
      _seedData[(i * 12 + 4)] = L2Manor.getInstance().getSeedSaleLimit(s);
      _seedData[(i * 12 + 5)] = L2Manor.getInstance().getSeedBuyPrice(s);
      _seedData[(i * 12 + 6)] = (L2Manor.getInstance().getSeedBasicPrice(s) * 60 / 100);
      _seedData[(i * 12 + 7)] = (L2Manor.getInstance().getSeedBasicPrice(s) * 10);
      CastleManorManager.SeedProduction seedPr = c.getSeed(s, 0);
      if (seedPr != null) {
        _seedData[(i * 12 + 8)] = seedPr.getStartProduce();
        _seedData[(i * 12 + 9)] = seedPr.getPrice();
      } else {
        _seedData[(i * 12 + 8)] = 0;
        _seedData[(i * 12 + 9)] = 0;
      }
      seedPr = c.getSeed(s, 1);
      if (seedPr != null) {
        _seedData[(i * 12 + 10)] = seedPr.getStartProduce();
        _seedData[(i * 12 + 11)] = seedPr.getPrice();
      } else {
        _seedData[(i * 12 + 10)] = 0;
        _seedData[(i * 12 + 11)] = 0;
      }
      i++;
    }
  }

  public void writeImpl()
  {
    writeC(254);
    writeH(31);

    writeD(_manorId);
    writeD(_count);

    for (int i = 0; i < _count; i++) {
      writeD(_seedData[(i * 12 + 0)]);
      writeD(_seedData[(i * 12 + 1)]);
      writeC(1);
      writeD(_seedData[(i * 12 + 2)]);
      writeC(1);
      writeD(_seedData[(i * 12 + 3)]);

      writeD(_seedData[(i * 12 + 4)]);
      writeD(_seedData[(i * 12 + 5)]);
      writeD(_seedData[(i * 12 + 6)]);
      writeD(_seedData[(i * 12 + 7)]);

      writeD(_seedData[(i * 12 + 8)]);
      writeD(_seedData[(i * 12 + 9)]);
      writeD(_seedData[(i * 12 + 10)]);
      writeD(_seedData[(i * 12 + 11)]);
    }
  }

  public String getType()
  {
    return "[S] FE:1F ExShowSeedSetting";
  }
}