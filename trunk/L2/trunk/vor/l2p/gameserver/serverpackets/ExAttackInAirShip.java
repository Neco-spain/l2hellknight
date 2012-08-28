package l2p.gameserver.serverpackets;

public class ExAttackInAirShip extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeEx(114);
  }
}