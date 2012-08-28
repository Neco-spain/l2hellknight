package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.L2GameClient;

public class PartySmallWindowAdd extends L2GameServerPacket
{
  private L2PcInstance _member;

  public PartySmallWindowAdd(L2PcInstance member)
  {
    _member = member;
  }

  protected final void writeImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    writeC(79);
    writeD(player.getObjectId());
    writeD(0);
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
    writeD(0);
    writeD(0);
  }
}