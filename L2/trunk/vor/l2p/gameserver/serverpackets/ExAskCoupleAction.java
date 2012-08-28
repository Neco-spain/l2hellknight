package l2p.gameserver.serverpackets;

public class ExAskCoupleAction extends L2GameServerPacket
{
  private int _objectId;
  private int _socialId;

  public ExAskCoupleAction(int objectId, int socialId)
  {
    _objectId = objectId;
    _socialId = socialId;
  }

  protected void writeImpl()
  {
    writeEx(187);
    writeD(_socialId);
    writeD(_objectId);
  }
}