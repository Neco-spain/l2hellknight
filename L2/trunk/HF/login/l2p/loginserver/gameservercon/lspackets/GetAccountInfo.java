package l2m.loginserver.gameservercon.lspackets;

import l2m.loginserver.gameservercon.SendablePacket;

public class GetAccountInfo extends SendablePacket
{
  private String _name;

  public GetAccountInfo(String name)
  {
    _name = name;
  }

  protected void writeImpl()
  {
    writeC(4);
    writeS(_name);
  }
}