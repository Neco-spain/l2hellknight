package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Vector;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class SystemMessage extends L2GameServerPacket
{
  private static final int TYPE_ZONE_NAME = 7;
  private static final int TYPE_SKILL_NAME = 4;
  private static final int TYPE_ITEM_NAME = 3;
  private static final int TYPE_NPC_NAME = 2;
  private static final int TYPE_NUMBER = 1;
  private static final int TYPE_TEXT = 0;
  private static final String _S__7A_SYSTEMMESSAGE = "[S] 64 SystemMessage";
  private int _messageId;
  private Vector<Integer> _types = new Vector();
  private Vector<Object> _values = new Vector();
  private int _skillLvL = 1;

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
    SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
    sm.addString(msg);

    return sm;
  }

  public SystemMessage addString(String text)
  {
    _types.add(new Integer(0));
    _values.add(text);

    return this;
  }

  public SystemMessage addNumber(int number)
  {
    _types.add(new Integer(1));
    _values.add(new Integer(number));
    return this;
  }

  public SystemMessage addNpcName(int id)
  {
    _types.add(new Integer(2));
    _values.add(new Integer(1000000 + id));

    return this;
  }

  public SystemMessage addPcName(L2PcInstance pc)
  {
    return addString(pc.getAppearance().getVisibleName());
  }

  public SystemMessage addItemName(int id)
  {
    _types.add(new Integer(3));
    _values.add(new Integer(id));

    return this;
  }

  public SystemMessage addZoneName(int x, int y, int z)
  {
    _types.add(new Integer(7));
    int[] coord = { x, y, z };
    _values.add(coord);

    return this;
  }
  public SystemMessage addSkillName(int id) {
    return addSkillName(id, 1);
  }

  public SystemMessage addSkillName(int id, int lvl) {
    _types.add(new Integer(4));
    _values.add(new Integer(id));
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

  public String getType()
  {
    return "[S] 64 SystemMessage";
  }

  public int getMessageID()
  {
    return _messageId;
  }
}