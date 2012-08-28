package l2m.gameserver.network.serverpackets;

public class ExSubPledgeSkillAdd extends L2GameServerPacket
{
  private int _type;
  private int _id;
  private int _level;

  public ExSubPledgeSkillAdd(int type, int id, int level)
  {
    _type = type;
    _id = id;
    _level = level;
  }

  protected void writeImpl()
  {
    writeEx(118);
    writeD(_type);
    writeD(_id);
    writeD(_level);
  }
}