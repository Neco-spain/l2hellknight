package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PartySmallWindowDelete extends L2GameServerPacket
{
  private L2PcInstance _member;

  public PartySmallWindowDelete(L2PcInstance member)
  {
    _member = member;
  }

  protected final void writeImpl()
  {
    writeC(81);
    writeD(_member.getObjectId());
    writeS(_member.getName());
  }
}