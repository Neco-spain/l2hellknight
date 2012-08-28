package l2p.gameserver.serverpackets;

import java.util.List;
import l2p.gameserver.model.Manor;
import l2p.gameserver.templates.manor.SeedProduction;

public class ExShowSeedInfo extends L2GameServerPacket
{
  private List<SeedProduction> _seeds;
  private int _manorId;

  public ExShowSeedInfo(int manorId, List<SeedProduction> seeds)
  {
    _manorId = manorId;
    _seeds = seeds;
  }

  protected void writeImpl()
  {
    writeEx(35);
    writeC(0);
    writeD(_manorId);
    writeD(0);
    writeD(_seeds.size());
    for (SeedProduction seed : _seeds)
    {
      writeD(seed.getId());

      writeQ(seed.getCanProduce());
      writeQ(seed.getStartProduce());
      writeQ(seed.getPrice());
      writeD(Manor.getInstance().getSeedLevel(seed.getId()));

      writeC(1);
      writeD(Manor.getInstance().getRewardItemBySeed(seed.getId(), 1));

      writeC(1);
      writeD(Manor.getInstance().getRewardItemBySeed(seed.getId(), 2));
    }
  }
}