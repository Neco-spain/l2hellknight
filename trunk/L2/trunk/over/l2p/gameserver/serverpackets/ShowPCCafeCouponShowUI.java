package l2p.gameserver.serverpackets;

public class ShowPCCafeCouponShowUI extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeEx(68);
  }
}