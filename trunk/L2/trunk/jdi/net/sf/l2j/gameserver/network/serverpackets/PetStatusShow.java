package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Summon;

public class PetStatusShow extends L2GameServerPacket
{
  private static final String _S__C9_PETSTATUSSHOW = "[S] B0 PetStatusShow";
  private int _summonType;

  public PetStatusShow(L2Summon summon)
  {
    _summonType = summon.getSummonType();
  }

  protected final void writeImpl()
  {
    writeC(176);
    writeD(_summonType);
  }

  public String getType()
  {
    return "[S] B0 PetStatusShow";
  }
}