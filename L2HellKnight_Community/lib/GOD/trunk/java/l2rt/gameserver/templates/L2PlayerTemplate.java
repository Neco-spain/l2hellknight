package l2rt.gameserver.templates;

import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.model.base.Race;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.GArray;
import l2rt.util.Location;

public class L2PlayerTemplate extends L2CharTemplate
{
	/** The Class<?> object of the L2Player */
	public final ClassId classId;

	public final Race race;
	public final String className;

	public final Location spawnLoc = new Location();

	public final boolean isMale;

	public final int classBaseLevel;
	public final float lvlHpAdd;
	public final float lvlHpMod;
	public final float lvlCpAdd;
	public final float lvlCpMod;
	public final float lvlMpAdd;
	public final float lvlMpMod;

	private GArray<L2Item> _items = new GArray<L2Item>();

	public L2PlayerTemplate(StatsSet set)
	{
		super(set);
		classId = ClassId.values()[set.getInteger("classId")];
		race = Race.values()[set.getInteger("raceId")];
		className = set.getString("className");

		spawnLoc.set(new Location(set.getInteger("spawnX"), set.getInteger("spawnY"), set.getInteger("spawnZ")));

		isMale = set.getBool("isMale", true);

		classBaseLevel = set.getInteger("classBaseLevel");
		lvlHpAdd = set.getFloat("lvlHpAdd");
		lvlHpMod = set.getFloat("lvlHpMod");
		lvlCpAdd = set.getFloat("lvlCpAdd");
		lvlCpMod = set.getFloat("lvlCpMod");
		lvlMpAdd = set.getFloat("lvlMpAdd");
		lvlMpMod = set.getFloat("lvlMpMod");
	}

	/**
	 * add starter equipment
	 * @param i
	 */
	public void addItem(int itemId)
	{
		L2Item item = ItemTemplates.getInstance().getTemplate(itemId);
		if(item != null)
			_items.add(item);
	}

	/**
	 *
	 * @return itemIds of all the starter equipment
	 */
	public L2Item[] getItems()
	{
		return _items.toArray(new L2Item[_items.size()]);
	}
}