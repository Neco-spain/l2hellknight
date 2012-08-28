package net.sf.l2j.gameserver.network.serverpackets;

public class AutoAttackStart extends L2GameServerPacket
{
  private static final String _S__3B_AUTOATTACKSTART = "[S] 2B AutoAttackStart";
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

  public String getType()
  {
    return "[S] 2B AutoAttackStart";
  }
}