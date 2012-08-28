package net.sf.l2j.gameserver.gameserverpackets;

import java.io.IOException;
import java.util.Vector;

public class ServerStatus extends GameServerBasePacket
{
  private Vector<Attribute> _attributes;
  public static final String[] STATUS_STRING = { "Auto", "Good", "Normal", "Full", "Down", "Gm Only" };
  public static final int SERVER_LIST_STATUS = 1;
  public static final int SERVER_LIST_CLOCK = 2;
  public static final int SERVER_LIST_SQUARE_BRACKET = 3;
  public static final int MAX_PLAYERS = 4;
  public static final int TEST_SERVER = 5;
  public static final int STATUS_AUTO = 0;
  public static final int STATUS_GOOD = 1;
  public static final int STATUS_NORMAL = 2;
  public static final int STATUS_FULL = 3;
  public static final int STATUS_DOWN = 4;
  public static final int STATUS_GM_ONLY = 5;
  public static final int ON = 1;
  public static final int OFF = 0;

  public ServerStatus()
  {
    _attributes = new Vector();
  }

  public void addAttribute(int id, int value)
  {
    _attributes.add(new Attribute(id, value));
  }

  public byte[] getContent()
    throws IOException
  {
    writeC(6);
    writeD(_attributes.size());
    for (int i = 0; i < _attributes.size(); i++)
    {
      Attribute temp = (Attribute)_attributes.get(i);

      writeD(temp.id);
      writeD(temp.value);
    }

    return getBytes();
  }

  class Attribute
  {
    public int id;
    public int value;

    Attribute(int pId, int pValue)
    {
      id = pId;
      value = pValue;
    }
  }
}