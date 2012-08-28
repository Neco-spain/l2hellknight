package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class SystemMessage extends L2GameServerPacket
{
  private static final int TYPE_ZONE_NAME = 7;
  private static final int TYPE_SKILL_NAME = 4;
  private static final int TYPE_ITEM_NAME = 3;
  private static final int TYPE_NPC_NAME = 2;
  private static final int TYPE_NUMBER = 1;
  private static final int TYPE_TEXT = 0;
  private int _messageId;
  private FastTable<Integer> _types = new FastTable();
  private FastTable<Object> _values = new FastTable();
  private int _skillLvL = 1;

  public static SystemMessage id(SystemMessageId messageId)
  {
    return new SystemMessage(messageId);
  }

  public SystemMessage(SystemMessageId messageId)
  {
    _messageId = messageId.getId();
  }

  @Deprecated
  public SystemMessage(int messageId) {
    _messageId = messageId;
  }

  public static SystemMessage sendString(String msg)
  {
    return id(SystemMessageId.S1_S2).addString(msg);
  }

  public SystemMessage addString(String text)
  {
    _types.add(Integer.valueOf(0));
    _values.add(text);

    return this;
  }

  public SystemMessage addNumber(int number)
  {
    _types.add(Integer.valueOf(1));
    _values.add(Integer.valueOf(number));
    return this;
  }

  public SystemMessage addNpcName(int id)
  {
    _types.add(Integer.valueOf(2));
    _values.add(Integer.valueOf(1000000 + id));

    return this;
  }

  public SystemMessage addItemName(int id)
  {
    _types.add(Integer.valueOf(3));
    _values.add(Integer.valueOf(id));

    return this;
  }

  public SystemMessage addZoneName(int x, int y, int z)
  {
    _types.add(Integer.valueOf(7));
    int[] coord = { x, y, z };
    _values.add(coord);

    return this;
  }
  public SystemMessage addSkillName(int id) {
    return addSkillName(id, 1); } 
  public SystemMessage addSkillName(L2Skill skill) { return addSkillName(skill.getId(), 1); }

  public SystemMessage addSkillName(int id, int lvl)
  {
    _types.add(Integer.valueOf(4));
    _values.add(Integer.valueOf(id));
    _skillLvL = lvl;
    return this;
  }

  protected final void writeImpl()
  {
    writeC(100);

    writeD(_messageId);
    writeD(_types.size());

    for (int i = 0; i < _types.size(); i++)
    {
      int t = ((Integer)_types.get(i)).intValue();

      writeD(t);

      switch (t)
      {
      case 0:
        writeS((String)_values.get(i));
        break;
      case 1:
      case 2:
      case 3:
        int t1 = ((Integer)_values.get(i)).intValue();
        writeD(t1);
        break;
      case 4:
        int t1 = ((Integer)_values.get(i)).intValue();
        writeD(t1);
        writeD(_skillLvL);
        break;
      case 7:
        int t1 = ((int[])(int[])_values.get(i))[0];
        int t2 = ((int[])(int[])_values.get(i))[1];
        int t3 = ((int[])(int[])_values.get(i))[2];
        writeD(t1);
        writeD(t2);
        writeD(t3);
      case 5:
      case 6:
      }
    }
  }

  public int getMessageID()
  {
    return _messageId;
  }
}