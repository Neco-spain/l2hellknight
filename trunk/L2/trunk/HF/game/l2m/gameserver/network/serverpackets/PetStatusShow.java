package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Summon;

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