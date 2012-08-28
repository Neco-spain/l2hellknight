package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Summon;

public class SetSummonRemainTime extends L2GameServerPacket
{
  private final int _maxFed;
  private final int _curFed;

  public SetSummonRemainTime(Summon summon)
  {
    _curFed = summon.getCurrentFed();
    _maxFed = summon.getMaxFed();
  }

  protected final void writeImpl()
  {
    writeC(209);
    writeD(_maxFed);
    writeD(_curFed);
  }
}