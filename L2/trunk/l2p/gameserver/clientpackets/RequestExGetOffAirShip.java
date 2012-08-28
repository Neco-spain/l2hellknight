package l2p.gameserver.clientpackets;

public class RequestExGetOffAirShip extends L2GameClientPacket
{
  private int _x;
  private int _y;
  private int _z;
  private int _id;

  protected void readImpl()
  {
    _x = readD();
    _y = readD();
    _z = readD();
    _id = readD();
  }

  protected void runImpl()
  {
  }
}