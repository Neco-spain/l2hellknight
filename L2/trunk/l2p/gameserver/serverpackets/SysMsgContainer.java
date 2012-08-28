package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Summon;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.entity.residence.Residence;
import l2p.gameserver.model.instances.DoorInstance;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.instances.StaticObjectInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.utils.Location;

public abstract class SysMsgContainer<T extends SysMsgContainer<T>> extends L2GameServerPacket
{
  protected SystemMsg _message;
  protected List<IArgument> _arguments;

  protected SysMsgContainer(int messageId)
  {
    this(SystemMsg.valueOf(messageId));
  }

  protected SysMsgContainer(SystemMsg message)
  {
    if (message == null) {
      throw new IllegalArgumentException("SystemMsg is null");
    }
    _message = message;
    _arguments = new ArrayList(_message.size());
  }

  protected void writeElements()
  {
    if (_message.size() != _arguments.size()) {
      throw new IllegalArgumentException("Wrong count of arguments: " + _message);
    }
    writeD(_message.getId());
    writeD(_arguments.size());
    for (IArgument argument : _arguments)
      argument.write(this);
  }

  public T addName(GameObject object)
  {
    if (object == null) {
      return add(new StringArgument(null));
    }
    if (object.isNpc())
      return add(new NpcNameArgument(((NpcInstance)object).getNpcId() + 1000000));
    if ((object instanceof Summon))
      return add(new NpcNameArgument(((Summon)object).getNpcId() + 1000000));
    if (object.isItem())
      return add(new ItemNameArgument(((ItemInstance)object).getItemId()));
    if (object.isPlayer())
      return add(new PlayerNameArgument((Player)object));
    if (object.isDoor())
      return add(new StaticObjectNameArgument(((DoorInstance)object).getDoorId()));
    if ((object instanceof StaticObjectInstance)) {
      return add(new StaticObjectNameArgument(((StaticObjectInstance)object).getUId()));
    }
    return add(new StringArgument(object.getName()));
  }

  public T addInstanceName(int id)
  {
    return add(new InstanceNameArgument(id));
  }

  public T addSysString(int id)
  {
    return add(new SysStringArgument(id));
  }

  public T addSkillName(Skill skill)
  {
    return addSkillName(skill.getDisplayId(), skill.getDisplayLevel());
  }

  public T addSkillName(int id, int level)
  {
    return add(new SkillArgument(id, level));
  }

  public T addItemName(int item_id)
  {
    return add(new ItemNameArgument(item_id));
  }

  public T addItemNameWithAugmentation(ItemInstance item)
  {
    return add(new ItemNameWithAugmentationArgument(item.getItemId(), item.getAugmentationId()));
  }

  public T addZoneName(Creature c)
  {
    return addZoneName(c.getX(), c.getY(), c.getZ());
  }

  public T addZoneName(Location loc)
  {
    return add(new ZoneArgument(loc.x, loc.y, loc.z));
  }

  public T addZoneName(int x, int y, int z)
  {
    return add(new ZoneArgument(x, y, z));
  }

  public T addResidenceName(Residence r)
  {
    return add(new ResidenceArgument(r.getId()));
  }

  public T addResidenceName(int i)
  {
    return add(new ResidenceArgument(i));
  }

  public T addElementName(int i)
  {
    return add(new ElementNameArgument(i));
  }

  public T addElementName(Element i)
  {
    return add(new ElementNameArgument(i.getId()));
  }

  public T addInteger(double i)
  {
    return add(new IntegerArgument((int)i));
  }

  public T addLong(long i)
  {
    return add(new LongArgument(i));
  }

  public T addString(String t)
  {
    return add(new StringArgument(t));
  }

  protected T add(IArgument arg)
  {
    _arguments.add(arg);

    return this;
  }

  public static class PlayerNameArgument extends SysMsgContainer.StringArgument
  {
    public PlayerNameArgument(Creature creature)
    {
      super();
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.PLAYER_NAME;
    }
  }

  public static class ElementNameArgument extends SysMsgContainer.IntegerArgument
  {
    public ElementNameArgument(int type)
    {
      super();
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.ELEMENT_NAME;
    }
  }

  public static class ZoneArgument extends SysMsgContainer.IArgument
  {
    private final int _x;
    private final int _y;
    private final int _z;

    public ZoneArgument(int t1, int t2, int t3)
    {
      _x = t1;
      _y = t2;
      _z = t3;
    }

    void writeData(SysMsgContainer message)
    {
      message.writeD(_x);
      message.writeD(_y);
      message.writeD(_z);
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.ZONE_NAME;
    }
  }

  public static class SkillArgument extends SysMsgContainer.IArgument
  {
    private final int _skillId;
    private final int _skillLevel;

    public SkillArgument(int t1, int t2)
    {
      _skillId = t1;
      _skillLevel = t2;
    }

    void writeData(SysMsgContainer message)
    {
      message.writeD(_skillId);
      message.writeD(_skillLevel);
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.SKILL_NAME;
    }
  }

  public static class StringArgument extends SysMsgContainer.IArgument
  {
    private final String _data;

    public StringArgument(String da)
    {
      _data = (da == null ? "null" : da);
    }

    void writeData(SysMsgContainer message)
    {
      message.writeS(_data);
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.TEXT;
    }
  }

  public static class LongArgument extends SysMsgContainer.IArgument
  {
    private final long _data;

    public LongArgument(long da)
    {
      _data = da;
    }

    void writeData(SysMsgContainer message)
    {
      message.writeQ(_data);
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.LONG;
    }
  }

  public static class StaticObjectNameArgument extends SysMsgContainer.IntegerArgument
  {
    public StaticObjectNameArgument(int da)
    {
      super();
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.STATIC_OBJECT_NAME;
    }
  }

  public static class ResidenceArgument extends SysMsgContainer.IntegerArgument
  {
    public ResidenceArgument(int da)
    {
      super();
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.RESIDENCE_NAME;
    }
  }

  public static class SysStringArgument extends SysMsgContainer.IntegerArgument
  {
    public SysStringArgument(int da)
    {
      super();
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.SYSTEM_STRING;
    }
  }

  public static class InstanceNameArgument extends SysMsgContainer.IntegerArgument
  {
    public InstanceNameArgument(int da)
    {
      super();
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.INSTANCE_NAME;
    }
  }

  public static class ItemNameWithAugmentationArgument extends SysMsgContainer.IArgument
  {
    private final int _itemId;
    private final int _augmentationId;

    public ItemNameWithAugmentationArgument(int itemId, int augmentationId)
    {
      _itemId = itemId;
      _augmentationId = augmentationId;
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.ITEM_NAME_WITH_AUGMENTATION;
    }

    void writeData(SysMsgContainer message)
    {
      message.writeD(_itemId);
      message.writeD(_augmentationId);
    }
  }

  public static class ItemNameArgument extends SysMsgContainer.IntegerArgument
  {
    public ItemNameArgument(int da)
    {
      super();
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.ITEM_NAME;
    }
  }

  public static class NpcNameArgument extends SysMsgContainer.IntegerArgument
  {
    public NpcNameArgument(int da)
    {
      super();
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.NPC_NAME;
    }
  }

  public static class IntegerArgument extends SysMsgContainer.IArgument
  {
    private final int _data;

    public IntegerArgument(int da)
    {
      _data = da;
    }

    public void writeData(SysMsgContainer message)
    {
      message.writeD(_data);
    }

    SysMsgContainer.Types getType()
    {
      return SysMsgContainer.Types.NUMBER;
    }
  }

  public static abstract class IArgument
  {
    void write(SysMsgContainer m)
    {
      m.writeD(getType().ordinal());

      writeData(m);
    }

    abstract SysMsgContainer.Types getType();

    abstract void writeData(SysMsgContainer paramSysMsgContainer);
  }

  public static enum Types
  {
    TEXT, 
    NUMBER, 
    NPC_NAME, 
    ITEM_NAME, 
    SKILL_NAME, 
    RESIDENCE_NAME, 
    LONG, 
    ZONE_NAME, 
    ITEM_NAME_WITH_AUGMENTATION, 
    ELEMENT_NAME, 
    INSTANCE_NAME, 
    STATIC_OBJECT_NAME, 
    PLAYER_NAME, 
    SYSTEM_STRING;
  }
}