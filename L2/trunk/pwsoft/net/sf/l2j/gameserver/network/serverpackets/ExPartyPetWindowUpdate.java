package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Summon;

public class ExPartyPetWindowUpdate extends L2GameServerPacket
{
  private final L2Summon _summon;

  public ExPartyPetWindowUpdate(L2Summon summon)
  {
    _summon = summon;
  }

  protected void writeImpl()
  {
  }
}