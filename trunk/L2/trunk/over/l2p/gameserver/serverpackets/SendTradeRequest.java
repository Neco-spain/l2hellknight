package l2p.gameserver.serverpackets;

public class SendTradeRequest extends L2GameServerPacket
{
  private int _senderId;

  public SendTradeRequest(int senderId)
  {
    _senderId = senderId;
  }

  protected final void writeImpl()
  {
    writeC(112);
    writeD(_senderId);
  }
}