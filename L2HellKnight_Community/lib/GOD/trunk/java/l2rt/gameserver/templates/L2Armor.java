package l2rt.gameserver.templates;

import l2rt.gameserver.model.base.ClassType;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.skills.funcs.FuncTemplate;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2Weapon.WeaponType;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class L2Armor extends L2Item
{
	public static final double EMPTY_RING = 5;
	public static final double EMPTY_EARRING = 9;
	public static final double EMPTY_NECKLACE = 13;
	public static final double EMPTY_HELMET = 12;
	public static final double EMPTY_BODY_FIGHTER = 31;
	public static final double EMPTY_LEGS_FIGHTER = 18;
	public static final double EMPTY_BODY_MYSTIC = 15;
	public static final double EMPTY_LEGS_MYSTIC = 8;
	public static final double EMPTY_GLOVES = 8;
	public static final double EMPTY_BOOTS = 7;

	private final int _pDef;
	private final int _mDef;
	private final int _mpBonus;
	private final double _evsmod;
	private final ClassType _classtype;
    private ConcurrentHashMap<String, List<Integer>> exInfo;

    public enum ArmorType
	{
		NONE(1, "None"),
		LIGHT(2, "Light"),
		HEAVY(3, "Heavy"),
		MAGIC(4, "Magic"),
		PET(5, "Pet"),
		SIGIL(6, "Sigil");

		final int _id;
		final String _name;

		ArmorType(int id, String name)
		{
			_id = id;
			_name = name;
		}

		public long mask()
		{
			return 1L << (_id + WeaponType.values().length);
		}

		@Override
		public String toString()
		{
			return _name;
		}
	}

	/**
	 * Constructor<?> for Armor.<BR><BR>
	 * <U><I>Variables filled :</I></U><BR>
	 * <LI>_avoidModifier</LI>
	 * <LI>_pDef & _mDef</LI>
	 * <LI>_mpBonus & _hpBonus</LI>
	 * @param type : L2ArmorType designating the type of armor
	 * @param set : StatsSet designating the set of couples (key,value) caracterizing the armor
	 * @see L2Item constructor
	 */
	public L2Armor(ArmorType type, StatsSet set)
	{
		super(type, set);

		_pDef = set.getInteger("p_def", 0);
		_mDef = set.getInteger("m_def", 0);
		_mpBonus = set.getInteger("mp_bonus", 0);
		_evsmod = set.getDouble("avoid_modify", 0.0);
		_classtype = set.getEnum("player_class", ClassType.class, null);

		int sId = set.getInteger("enchant4_skill_id", 0);
		int sLv = set.getInteger("enchant4_skill_lvl", 0);
		if(sId > 0 && sLv > 0)
			_enchant4Skill = SkillTable.getInstance().getInfo(sId, sLv);

		if(_pDef > 0)
			attachFunction(new FuncTemplate(null, "Add", Stats.POWER_DEFENCE, 0x10, _pDef));
		if(_mDef > 0)
			attachFunction(new FuncTemplate(null, "Add", Stats.MAGIC_DEFENCE, 0x10, _mDef));
		if(_mpBonus > 0)
			attachFunction(new FuncTemplate(null, "Add", Stats.MAX_MP, 0x10, _mpBonus));
		if(_evsmod != 0)
			attachFunction(new FuncTemplate(null, "Add", Stats.EVASION_RATE, 0x10, _evsmod));

		if(_crystalType != Grade.NONE)
		{
			if(_pDef > 0)
			{
				attachFunction(new FuncTemplate(null, "Enchant", Stats.POWER_DEFENCE, 0x0C, 0));
				if(set.getInteger("type2") == L2Item.TYPE2_SHIELD_ARMOR)
					attachFunction(new FuncTemplate(null, "Enchant", Stats.MAX_HP, 0x80, 0));
			}
			if(_mDef > 0)
				attachFunction(new FuncTemplate(null, "Enchant", Stats.MAGIC_DEFENCE, 0x0C, 0));
		}
	}

	/**
	 * Returns the type of the armor.
	 * @return L2ArmorType
	 */
	@Override
	public ArmorType getItemType()
	{
		return (ArmorType) super.type;
	}

	/**
	 * Returns the ID of the item after applying the mask.
	 * @return int : ID of the item
	 */
	@Override
	public final long getItemMask()
	{
		return getItemType().mask();
	}

	public ClassType getClassType()
	{
		return _classtype;
	}
}