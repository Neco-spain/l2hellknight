package l2p.gameserver.serverpackets.components;

import java.io.PrintStream;
import l2p.gameserver.data.StringHolder;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.item.ItemTemplate;

public class CustomMessage
{
  private String _text;
  private int mark = 0;

  public CustomMessage(String address, Player player, Object[] args)
  {
    _text = StringHolder.getInstance().getNotNull(player, address);
    add(args);
  }

  public CustomMessage addNumber(long number)
  {
    _text = _text.replace("{" + mark + "}", String.valueOf(number));
    mark += 1;
    return this;
  }

  public CustomMessage add(Object[] args)
  {
    for (Object arg : args) {
      if ((arg instanceof String)) {
        addString((String)arg);
      } else if ((arg instanceof Integer)) {
        addNumber(((Integer)arg).intValue());
      } else if ((arg instanceof Long)) {
        addNumber(((Long)arg).longValue());
      } else if ((arg instanceof ItemTemplate)) {
        addItemName((ItemTemplate)arg);
      } else if ((arg instanceof ItemInstance)) {
        addItemName((ItemInstance)arg);
      } else if ((arg instanceof Creature)) {
        addCharName((Creature)arg);
      } else if ((arg instanceof Skill)) {
        addSkillName((Skill)arg);
      }
      else {
        System.out.println("unknown CustomMessage arg type: " + arg);
        Thread.dumpStack();
      }
    }
    return this;
  }

  public CustomMessage addString(String str)
  {
    _text = _text.replace("{" + mark + "}", str);
    mark += 1;
    return this;
  }

  public CustomMessage addSkillName(Skill skill)
  {
    _text = _text.replace("{" + mark + "}", skill.getName());
    mark += 1;
    return this;
  }

  public CustomMessage addSkillName(int skillId, int skillLevel)
  {
    return addSkillName(SkillTable.getInstance().getInfo(skillId, skillLevel));
  }

  public CustomMessage addItemName(ItemTemplate item)
  {
    _text = _text.replace("{" + mark + "}", item.getName());
    mark += 1;
    return this;
  }

  public CustomMessage addItemName(int itemId)
  {
    return addItemName(ItemHolder.getInstance().getTemplate(itemId));
  }

  public CustomMessage addItemName(ItemInstance item)
  {
    return addItemName(item.getTemplate());
  }

  public CustomMessage addCharName(Creature cha)
  {
    _text = _text.replace("{" + mark + "}", cha.getName());
    mark += 1;
    return this;
  }

  public String toString()
  {
    return _text;
  }
}