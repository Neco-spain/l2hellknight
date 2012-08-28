package l2m.gameserver.network.serverpackets;

public class ExAttackInAirShip extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeEx(114);
  }
}