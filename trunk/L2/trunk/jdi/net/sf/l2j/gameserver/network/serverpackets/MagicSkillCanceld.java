package net.sf.l2j.gameserver.network.serverpackets;

public class MagicSkillCanceld extends L2GameServerPacket
{
  private static final String _S__5B_MAGICSKILLCANCELD = "[S] 49 MagicSkillCanceld";
  private int _objectId;

  public MagicSkillCanceld(int objectId)
  {
    _objectId = objectId;
  }

  protected final void writeImpl()
  {
    writeC(73);
    writeD(_objectId);
  }

  public String getType()
  {
    return "[S] 49 MagicSkillCanceld";
  }
}