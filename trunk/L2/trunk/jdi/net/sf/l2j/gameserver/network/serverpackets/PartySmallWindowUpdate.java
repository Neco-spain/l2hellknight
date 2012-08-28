package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;

public class PartySmallWindowUpdate extends L2GameServerPacket
{
  private static final String _S__67_PARTYSMALLWINDOWUPDATE = "[S] 52 PartySmallWindowUpdate";
  private L2PcInstance _member;

  public PartySmallWindowUpdate(L2PcInstance member)
  {
    _member = member;
  }

  protected final void writeImpl()
  {
    writeC(82);
    writeD(_member.getObjectId());
    writeS(_member.getName());

    writeD((int)_member.getCurrentCp());
    writeD(_member.getMaxCp());

    writeD((int)_member.getCurrentHp());
    writeD(_member.getMaxHp());
    writeD((int)_member.getCurrentMp());
    writeD(_member.getMaxMp());
    writeD(_member.getLevel());
    writeD(_member.getClassId().getId());
  }

  public String getType()
  {
    return "[S] 52 PartySmallWindowUpdate";
  }
}