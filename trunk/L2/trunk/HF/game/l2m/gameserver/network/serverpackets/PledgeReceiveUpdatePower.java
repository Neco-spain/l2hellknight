package l2m.gameserver.serverpackets;

public class PledgeReceiveUpdatePower extends L2GameServerPacket
{
  private int _privs;

  public PledgeReceiveUpdatePower(int privs)
  {
    _privs = privs;
  }

  protected final void writeImpl()
  {
    writeEx(66);
    writeD(_privs);
  }
}