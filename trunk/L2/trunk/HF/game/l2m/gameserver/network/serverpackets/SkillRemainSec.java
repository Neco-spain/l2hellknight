package l2m.gameserver.serverpackets;

public class SkillRemainSec extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(216);
  }
}