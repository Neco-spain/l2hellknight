package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Vector;

public class StatusUpdate extends L2GameServerPacket
{
  private static final String _S__1A_STATUSUPDATE = "[S] 0e StatusUpdate";
  public static final int LEVEL = 1;
  public static final int EXP = 2;
  public static final int STR = 3;
  public static final int DEX = 4;
  public static final int CON = 5;
  public static final int INT = 6;
  public static final int WIT = 7;
  public static final int MEN = 8;
  public static final int CUR_HP = 9;
  public static final int MAX_HP = 10;
  public static final int CUR_MP = 11;
  public static final int MAX_MP = 12;
  public static final int SP = 13;
  public static final int CUR_LOAD = 14;
  public static final int MAX_LOAD = 15;
  public static final int P_ATK = 17;
  public static final int ATK_SPD = 18;
  public static final int P_DEF = 19;
  public static final int EVASION = 20;
  public static final int ACCURACY = 21;
  public static final int CRITICAL = 22;
  public static final int M_ATK = 23;
  public static final int CAST_SPD = 24;
  public static final int M_DEF = 25;
  public static final int PVP_FLAG = 26;
  public static final int KARMA = 27;
  public static final int CUR_CP = 33;
  public static final int MAX_CP = 34;
  private int _objectId;
  private Vector<Attribute> _attributes;

  public StatusUpdate(int objectId)
  {
    _attributes = new Vector();
    _objectId = objectId;
  }

  public void addAttribute(int id, int level)
  {
    _attributes.add(new Attribute(id, level));
  }

  protected final void writeImpl()
  {
    writeC(14);
    writeD(_objectId);
    writeD(_attributes.size());

    for (int i = 0; i < _attributes.size(); i++)
    {
      Attribute temp = (Attribute)_attributes.get(i);

      writeD(temp.id);
      writeD(temp.value);
    }
  }

  public String getType()
  {
    return "[S] 0e StatusUpdate";
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