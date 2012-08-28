package net.sf.l2j.gameserver.network.serverpackets;

public class AutoAttackStop extends L2GameServerPacket
{
  private static final String _S__3C_AUTOATTACKSTOP = "[S] 3C AutoAttackStop";
  private int _targetObjId;

  public AutoAttackStop(int targetObjId)
  {
    _targetObjId = targetObjId;
  }

  protected final void writeImpl()
  {
    writeC(44);
    writeD(_targetObjId);
  }

  public String getType()
  {
    return "[S] 3C AutoAttackStop";
  }
}