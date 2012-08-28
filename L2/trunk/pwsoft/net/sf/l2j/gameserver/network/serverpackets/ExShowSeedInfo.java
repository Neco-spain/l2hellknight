package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastList;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.SeedProduction;
import net.sf.l2j.gameserver.model.L2Manor;

public class ExShowSeedInfo extends L2GameServerPacket
{
  private FastList<CastleManorManager.SeedProduction> _seeds = new FastList();
  private int _manorId;

  public ExShowSeedInfo(int manorId, FastList<CastleManorManager.SeedProduction> seeds)
  {
    _manorId = manorId;
    _seeds.addAll(seeds);
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(28);
    writeC(0);
    writeD(_manorId);
    writeD(0);
    writeD(_seeds.size());
    for (CastleManorManager.SeedProduction seed : _seeds) {
      writeD(seed.getId());
      writeD(seed.getCanProduce());
      writeD(seed.getStartProduce());
      writeD(seed.getPrice());
      writeD(L2Manor.getInstance().getSeedLevel(seed.getId()));
      writeC(1);
      writeD(L2Manor.getInstance().getRewardItemBySeed(seed.getId(), 1));
      writeC(1);
      writeD(L2Manor.getInstance().getRewardItemBySeed(seed.getId(), 2));
    }
    _seeds.clear();
    _seeds = null;
  }
}