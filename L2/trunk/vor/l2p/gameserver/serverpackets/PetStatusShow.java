package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Summon;

public class PetStatusShow extends L2GameServerPacket
{
  private int _summonType;

  public PetStatusShow(Summon summon)
  {
    _summonType = summon.getSummonType();
  }

  protected final void writeImpl()
  {
    writeC(177);
    writeD(_summonType);
  }
}