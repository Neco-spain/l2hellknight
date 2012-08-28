package net.sf.l2j.gameserver.templates;

import javolution.util.FastList;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;

public class L2PcTemplate extends L2CharTemplate
{
  public final ClassId classId;
  public final Race race;
  public final String className;
  public final int classBaseLevel;
  public final float lvlHpAdd;
  public final float lvlHpMod;
  public final float lvlCpAdd;
  public final float lvlCpMod;
  public final float lvlMpAdd;
  public final float lvlMpMod;
  private FastList<Integer> _items = new FastList();
  private FastList<Location> _spawnPoints = new FastList();

  public L2PcTemplate(StatsSet set)
  {
    super(set);
    classId = ClassId.values()[set.getInteger("classId")];
    race = Race.values()[set.getInteger("raceId")];
    className = set.getString("className");

    classBaseLevel = set.getInteger("classBaseLevel");
    lvlHpAdd = set.getFloat("lvlHpAdd");
    lvlHpMod = set.getFloat("lvlHpMod");
    lvlCpAdd = set.getFloat("lvlCpAdd");
    lvlCpMod = set.getFloat("lvlCpMod");
    lvlMpAdd = set.getFloat("lvlMpAdd");
    lvlMpMod = set.getFloat("lvlMpMod");
  }

  public void addItem(int itemId)
  {
    L2Item item = ItemTable.getInstance().getTemplate(itemId);
    if (item != null)
      _items.add(Integer.valueOf(itemId));
  }

  public void addSpawnPoint(Location loc)
  {
    _spawnPoints.add(loc);
  }

  public FastList<Integer> getItems()
  {
    return _items;
  }

  public Location getRandomSpawnPoint() {
    return (Location)_spawnPoints.get(Rnd.get(_spawnPoints.size() - 1));
  }
}