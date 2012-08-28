package l2m.gameserver.network.serverpackets;

public class ExBlockUpSetList extends L2GameServerPacket
{
  private int BlockUpType = 0;

  protected void writeImpl()
  {
    writeEx(151);
    writeD(BlockUpType);
    switch (BlockUpType)
    {
    case 0:
      break;
    case 1:
      break;
    case 2:
      break;
    case 3:
      break;
    case 4:
      break;
    case 5:
      break;
    case -1:
    }
  }
}