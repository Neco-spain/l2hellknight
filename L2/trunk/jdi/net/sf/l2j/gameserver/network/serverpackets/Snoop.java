package net.sf.l2j.gameserver.network.serverpackets;

public class Snoop extends L2GameServerPacket
{
  private static final String _S__D5_SNOOP = "[S] D5 Snoop";
  private int _convoId;
  private String _name;
  private int _type;
  private String _speaker;
  private String _msg;

  public Snoop(int id, String name, int type, String speaker, String msg)
  {
    _convoId = id;
    _name = name;
    _type = type;
    _speaker = speaker;
    _msg = msg;
  }

  protected void writeImpl()
  {
    writeC(213);

    writeD(_convoId);
    writeS(_name);
    writeD(0);
    writeD(_type);
    writeS(_speaker);
    writeS(_msg);
  }

  public String getType()
  {
    return "[S] D5 Snoop";
  }
}