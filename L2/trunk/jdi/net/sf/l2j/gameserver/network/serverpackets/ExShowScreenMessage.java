package net.sf.l2j.gameserver.network.serverpackets;

public class ExShowScreenMessage extends L2GameServerPacket
{
  private String _text;
  private int _time;

  public ExShowScreenMessage(String text, int time)
  {
    _text = text;
    _time = time;
  }

  public String getType()
  {
    return "ExShowScreenMessage";
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(57);

    writeD(1);
    writeD(-1);
    writeD(2);
    writeD(0);
    writeD(0);
    writeD(0);

    writeD(0);
    writeD(0);

    writeD(_time);

    writeD(1);

    writeS(_text);
  }
}