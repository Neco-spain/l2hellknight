package net.sf.l2j.gameserver.network.serverpackets;

public class AskJoinParty extends L2GameServerPacket
{
  private String _requestorName;
  private int _itemDistribution;

  public AskJoinParty(String requestorName, int itemDistribution)
  {
    _requestorName = requestorName;
    _itemDistribution = itemDistribution;
  }

  protected final void writeImpl()
  {
    writeC(57);
    writeS(_requestorName);
    writeD(_itemDistribution);
  }
}