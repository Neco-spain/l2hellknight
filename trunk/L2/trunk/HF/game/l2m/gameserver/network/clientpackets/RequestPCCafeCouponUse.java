package l2m.gameserver.network.clientpackets;

public class RequestPCCafeCouponUse extends L2GameClientPacket
{
  private String _unknown;

  protected void readImpl()
  {
    _unknown = readS();
  }

  protected void runImpl()
  {
  }
}