package l2p.gameserver.clientpackets;

public class MoveWithDelta extends L2GameClientPacket
{
  private int _dx;
  private int _dy;
  private int _dz;

  protected void readImpl()
  {
    _dx = readD();
    _dy = readD();
    _dz = readD();
  }

  protected void runImpl()
  {
  }
}