package l2m.gameserver.network.serverpackets;

public class ExMagicSkillUseInAirShip extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeEx(115);
  }
}