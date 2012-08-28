package l2m.gameserver.network.serverpackets;

public class AutoAttackStop extends L2GameServerPacket
{
  private int _targetId;

  public AutoAttackStop(int targetId)
  {
    _targetId = targetId;
  }

  protected final void writeImpl()
  {
    writeC(38);
    writeD(_targetId);
  }
}