package net.sf.l2j.gameserver.network.serverpackets;

public class AutoAttackStart extends L2GameServerPacket
{
  private int _targetObjId;

  public AutoAttackStart(int targetId)
  {
    _targetObjId = targetId;
  }

  protected final void writeImpl()
  {
    writeC(43);
    writeD(_targetObjId);
  }
}