package l2p.gameserver.serverpackets;

public class ExBlockUpSetState extends L2GameServerPacket
{
  private int BlockUpStateType = 0;

  protected void writeImpl()
  {
    writeEx(152);
    writeD(BlockUpStateType);
    switch (BlockUpStateType)
    {
    case 0:
      break;
    case 1:
      break;
    case 2:
    }
  }
}