package l2p.gameserver.serverpackets;

public class ExMagicSkillUseInAirShip extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeEx(115);
  }
}