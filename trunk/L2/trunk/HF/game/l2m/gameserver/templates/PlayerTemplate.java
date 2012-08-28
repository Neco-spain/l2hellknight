package l2m.gameserver.templates;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.data.xml.holder.ItemHolder;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.base.Race;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.utils.Location;

public class PlayerTemplate extends CharTemplate
{
  public final ClassId classId;
  public final Race race;
  public final String className;
  public final Location spawnLoc = new Location();
  public final boolean isMale;
  public final int classBaseLevel;
  public final double lvlHpAdd;
  public final double lvlHpMod;
  public final double lvlCpAdd;
  public final double lvlCpMod;
  public final double lvlMpAdd;
  public final double lvlMpMod;
  private List<ItemTemplate> _items = new ArrayList();

  public PlayerTemplate(StatsSet set)
  {
    super(set);
    classId = ClassId.VALUES[set.getInteger("classId")];
    race = Race.values()[set.getInteger("raceId")];
    className = set.getString("className");

    spawnLoc.set(new Location(set.getInteger("spawnX"), set.getInteger("spawnY"), set.getInteger("spawnZ")));

    isMale = set.getBool("isMale", true);

    classBaseLevel = set.getInteger("classBaseLevel");
    lvlHpAdd = set.getDouble("lvlHpAdd");
    lvlHpMod = set.getDouble("lvlHpMod");
    lvlCpAdd = set.getDouble("lvlCpAdd");
    lvlCpMod = set.getDouble("lvlCpMod");
    lvlMpAdd = set.getDouble("lvlMpAdd");
    lvlMpMod = set.getDouble("lvlMpMod");
  }

  public void addItem(int itemId)
  {
    ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
    if (item != null)
      _items.add(item);
  }

  public ItemTemplate[] getItems()
  {
    return (ItemTemplate[])_items.toArray(new ItemTemplate[_items.size()]);
  }
}