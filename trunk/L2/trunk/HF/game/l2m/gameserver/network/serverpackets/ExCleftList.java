package l2m.gameserver.network.serverpackets;

public class ExCleftList extends L2GameServerPacket
{
  public static final int CleftType_Close = -1;
  public static final int CleftType_Total = 0;
  public static final int CleftType_Add = 1;
  public static final int CleftType_Remove = 2;
  public static final int CleftType_TeamChange = 3;
  private int CleftType = 0;

  protected void writeImpl()
  {
    writeEx(148);
    writeD(CleftType);
    switch (CleftType)
    {
    case 0:
      break;
    case 1:
      break;
    case 2:
      break;
    case 3:
      break;
    case -1:
    }
  }
}