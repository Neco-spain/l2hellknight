package net.sf.l2j.gameserver.network.serverpackets;

public class SendTradeRequest extends L2GameServerPacket
{
  private int _senderID;

  public SendTradeRequest(int senderID)
  {
    _senderID = senderID;
  }

  protected final void writeImpl()
  {
    writeC(94);
    writeD(_senderID);
  }

  public String getType()
  {
    return "S.SendTradeRequest";
  }
}