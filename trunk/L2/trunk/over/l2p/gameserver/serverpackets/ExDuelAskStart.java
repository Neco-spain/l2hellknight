package l2p.gameserver.serverpackets;

public class ExDuelAskStart extends L2GameServerPacket
{
  String _requestor;
  int _isPartyDuel;

  public ExDuelAskStart(String requestor, int isPartyDuel)
  {
    _requestor = requestor;
    _isPartyDuel = isPartyDuel;
  }

  protected final void writeImpl()
  {
    writeEx(76);
    writeS(_requestor);
    writeD(_isPartyDuel);
  }
}