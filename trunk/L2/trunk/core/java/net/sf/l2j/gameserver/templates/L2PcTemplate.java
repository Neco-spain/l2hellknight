package net.sf.l2j.gameserver.templates;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;

public class L2PcTemplate extends L2CharTemplate {

	public final ClassId classId;

	public final Race   race;
	public final String className;

	public final int    spawnX;
	public final int    spawnY;
	public final int    spawnZ;

	public final int     classBaseLevel;
	public final float   lvlHpAdd;
	public final float   lvlHpMod;
	public final float   lvlCpAdd;
	public final float   lvlCpMod;
	public final float   lvlMpAdd;
	public final float   lvlMpMod;

	private List<L2Item> _items = new FastList<L2Item>();


	public L2PcTemplate(StatsSet set)
	{
		super(set);
		classId   = ClassId.values()[set.getInteger("classId")];
		race      = Race.values()[set.getInteger("raceId")];
		className = set.getString("className");

		spawnX    = set.getInteger("spawnX");
		spawnY    = set.getInteger("spawnY");
		spawnZ    = set.getInteger("spawnZ");

		classBaseLevel = set.getInteger("classBaseLevel");
		lvlHpAdd  = set.getFloat("lvlHpAdd");
		lvlHpMod  = set.getFloat("lvlHpMod");
        lvlCpAdd  = set.getFloat("lvlCpAdd");
        lvlCpMod  = set.getFloat("lvlCpMod");
		lvlMpAdd  = set.getFloat("lvlMpAdd");
		lvlMpMod  = set.getFloat("lvlMpMod");
	}

	public void addItem(int itemId)
	{
		L2Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item != null)
			_items.add(item);
	}

	public L2Item[] getItems()
	{
		return _items.toArray(new L2Item[_items.size()]);
	}

	public int getBaseFallSafeHeight(boolean female)
	{
		if (classId.getRace() == Race.darkelf || classId.getRace() == Race.elf)
		{
			return (classId.isMage()) ? ((female) ? 330 : 300) : ((female) ? 380 : 350);
		}

		else if (classId.getRace() == Race.dwarf)
		{
			return ((female) ? 200 : 180);
		}

		else if (classId.getRace() == Race.human)
		{
			return (classId.isMage()) ? ((female) ? 220 : 200) : ((female) ? 270 : 250);
		}

		else if (classId.getRace() == Race.orc)
		{
			return (classId.isMage()) ? ((female) ? 280 : 250) : ((female) ? 220 : 200);
		}

		return Config.ALT_MINIMUM_FALL_HEIGHT;
	}
}
