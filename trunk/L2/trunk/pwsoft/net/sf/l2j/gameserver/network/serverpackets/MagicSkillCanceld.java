package net.sf.l2j.gameserver.network.serverpackets;

public class MagicSkillCanceld extends L2GameServerPacket
{
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
}