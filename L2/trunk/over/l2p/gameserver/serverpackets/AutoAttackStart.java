package l2p.gameserver.serverpackets;

public class AutoAttackStart extends L2GameServerPacket
{
  private int _targetId;

  public AutoAttackStart(int targetId)
  {
    _targetId = targetId;
  }

  protected final void writeImpl()
  {
    writeC(37);
    writeD(_targetId);
  }
}