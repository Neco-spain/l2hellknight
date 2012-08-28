package l2p.gameserver.serverpackets;

public class ExCleftState extends L2GameServerPacket
{
  public static final int CleftState_Total = 0;
  public static final int CleftState_TowerDestroy = 1;
  public static final int CleftState_CatUpdate = 2;
  public static final int CleftState_Result = 3;
  public static final int CleftState_PvPKill = 4;
  private int CleftState = 0;

  protected void writeImpl()
  {
    writeEx(149);
    writeD(CleftState);
    switch (CleftState)
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
    }
  }
}