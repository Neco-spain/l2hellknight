package l2rt.gameserver.model;

import javolution.util.FastList;
import l2rt.gameserver.model.base.UsablePacketItem;
import l2rt.gameserver.model.base.UsablePacketSkill;

public final class L2SkillLearn
{
	public final short id;
	public final short skillLevel;
	public final int _spCost;
	public final int _repCost;
	public final byte minLevel;
	public final short itemId;
	public final int itemCount;
	public final boolean common;
	public final boolean clan;
	public final boolean transformation;

	// not needed, just for easier debug
	public final String name;
	private FastList<UsablePacketItem> required = FastList.newInstance();
	private FastList<UsablePacketSkill> prequisites = FastList.newInstance();
	public L2SkillLearn(short id, short lvl, byte minLvl, String name, int cost, short itemId, int itemCount, boolean common, boolean clan, boolean transformation)
	{
		this.id = id;
		skillLevel = lvl;
		minLevel = minLvl;
		this.name = name.intern();
		if(clan)
		{
			_spCost = 0;
			_repCost = cost;
		}
		else if(transformation)
		{
			_repCost = 0;
			_spCost = 0;
		}
		else
		{
			_repCost = 0;
			_spCost = cost;
		}
		this.itemId = itemId;
		this.itemCount = itemCount;
		this.common = common;
		this.clan = clan;
		this.transformation = transformation;
	}

	public short getId()
	{
		return id;
	}

	public short getLevel()
	{
		return skillLevel;
	}

	public byte getMinLevel()
	{
		return minLevel;
	}

	public String getName()
	{
		return name;
	}

	public int getSpCost()
	{
		return _spCost;
	}

	public short getItemId()
	{
		return itemId;
	}

	public int getItemCount() //TODO: long
	{
		return itemCount;
	}

	public int getRepCost()
	{
		return _repCost;
	}

	@Override
	public String toString()
	{
		return "SkillLearn for " + name + " id " + id + " level " + skillLevel;
	}

	public int getReuse()
	{
		return 0;
	}

	public FastList<UsablePacketSkill> getPrequisiteSkills()
	{
		return prequisites;
	}

	public final FastList<UsablePacketItem> getRequiredItems()
	{
		return required;
	}

	
}