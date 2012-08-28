package l2p.gameserver.serverpackets;

public class MagicSkillCanceled extends L2GameServerPacket
{
  private int _objectId;

  public MagicSkillCanceled(int objectId)
  {
    _objectId = objectId;
  }

  protected final void writeImpl()
  {
    writeC(73);
    writeD(_objectId);
  }
}