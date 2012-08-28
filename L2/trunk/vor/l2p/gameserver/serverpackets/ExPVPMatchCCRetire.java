package l2p.gameserver.serverpackets;

public class ExPVPMatchCCRetire extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExPVPMatchCCRetire();

  public void writeImpl()
  {
    writeEx(139);
  }
}