package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Vector;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class ConfirmDlg extends L2GameServerPacket
{
  private static final String _S__ED_CONFIRMDLG = "[S] ed ConfirmDlg";
  private int _messageId;
  private int _skillLvL = 1;
  private static final int TYPE_ZONE_NAME = 7;
  private static final int TYPE_SKILL_NAME = 4;
  private static final int TYPE_ITEM_NAME = 3;
  private static final int TYPE_NPC_NAME = 2;
  private static final int TYPE_NUMBER = 1;
  private static final int TYPE_TEXT = 0;
  private Vector<Integer> _types = new Vector();
  private Vector<Object> _values = new Vector();

  private int _time = 0;
  private int _requesterId = 0;

  public ConfirmDlg(int messageId)
  {
    _messageId = messageId;
  }

  public ConfirmDlg addString(String text)
  {
    _types.add(new Integer(0));
    _values.add(text);
    return this;
  }

  public ConfirmDlg addNumber(int number)
  {
    _types.add(new Integer(1));
    _values.add(new Integer(number));
    return this;
  }

  public ConfirmDlg addCharName(L2Character cha)
  {
    if ((cha instanceof L2NpcInstance))
      return addNpcName((L2NpcInstance)cha);
    if ((cha instanceof L2PcInstance))
      return addPcName((L2PcInstance)cha);
    if ((cha instanceof L2Summon))
      return addNpcName((L2Summon)cha);
    return addString(cha.getName());
  }

  public ConfirmDlg addPcName(L2PcInstance pc)
  {
    return addString(pc.getName());
  }

  public ConfirmDlg addNpcName(L2NpcInstance npc)
  {
    return addNpcName(npc.getTemplate());
  }

  public ConfirmDlg addNpcName(L2Summon npc)
  {
    return addNpcName(npc.getNpcId());
  }

  public ConfirmDlg addNpcName(L2NpcTemplate tpl)
  {
    return addNpcName(tpl.npcId);
  }

  public ConfirmDlg addNpcName(int id)
  {
    _types.add(new Integer(2));
    _values.add(new Integer(1000000 + id));
    return this;
  }

  public ConfirmDlg addItemName(L2ItemInstance item)
  {
    return addItemName(item.getItem().getItemId());
  }

  public ConfirmDlg addItemName(L2Item item)
  {
    return addItemName(item.getItemId());
  }

  public ConfirmDlg addItemName(int id)
  {
    _types.add(new Integer(3));
    _values.add(new Integer(id));
    return this;
  }

  public ConfirmDlg addZoneName(int x, int y, int z)
  {
    _types.add(new Integer(7));
    int[] coord = { x, y, z };
    _values.add(coord);
    return this;
  }

  public ConfirmDlg addSkillName(L2Effect effect)
  {
    return addSkillName(effect.getSkill());
  }

  public ConfirmDlg addSkillName(L2Skill skill)
  {
    if (skill.getId() != skill.getDisplayId())
      return addString(skill.getName());
    return addSkillName(skill.getId(), skill.getLevel());
  }

  public ConfirmDlg addSkillName(int id)
  {
    return addSkillName(id, 1);
  }

  public ConfirmDlg addSkillName(int id, int lvl)
  {
    _types.add(new Integer(4));
    _values.add(new Integer(id));
    _skillLvL = lvl;
    return this;
  }

  public ConfirmDlg addTime(int time)
  {
    _time = time;
    return this;
  }

  public ConfirmDlg addRequesterId(int id)
  {
    _requesterId = id;
    return this;
  }

  protected final void writeImpl()
  {
    writeC(237);
    writeD(_messageId);

    if ((_types != null) && (_types.size() > 0))
    {
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

      writeD(_time);
      if (_time != 0)
      {
        ((L2GameClient)getClient()).getActiveChar().addConfirmDlgRequestTime(_requesterId, _time);
      }

      if (_requesterId != 0)
        writeD(_requesterId);
    }
    else
    {
      writeD(0);
      writeD(0);
      writeD(0);
    }
  }

  public String getType()
  {
    return "[S] ed ConfirmDlg";
  }
}