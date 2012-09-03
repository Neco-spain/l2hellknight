package l2rt.gameserver.model.base;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.tables.SkillTable;

public final class L2EnchantSkillLearn
{
	// these two build the primary key
	private final int _id;
	private final int _level;

	// not needed, just for easier debug
	private final String _name;
	private final String _type;

	private final int _baseLvl;
	private final int _maxLvl;
	private final int _minSkillLevel;

	private int _costMul;

	public L2EnchantSkillLearn(int id, int lvl, String name, String type, int minSkillLvl, int baseLvl, int maxLvl)
	{
		_id = id;
		_level = lvl;
		_baseLvl = baseLvl;
		_maxLvl = maxLvl;
		_minSkillLevel = minSkillLvl;
		_name = name.intern();
		_type = type.intern();
		_costMul = _maxLvl == 15 ? 5 : 1;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return _level;
	}

	/**
	 * @return Returns the minLevel.
	 */
	public int getBaseLevel()
	{
		return _baseLvl;
	}

	/**
	 * @return Returns the minSkillLevel.
	 */
	public int getMinSkillLevel()
	{
		return _minSkillLevel;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return _name;
	}

	public int getCostMult()
	{
		return _costMul;
	}

	/**
	 * @return Returns the spCost.
	 */
	public int[] getCost()
	{
		return SkillTable.getInstance().getInfo(_id, 1).isOffensive() ? _priceCombat[_level % 100] : _priceBuff[_level % 100];
	}

	/** Шанс заточки скилов 2й профы */
	private static final int[][] _chance = { {},
	//    76  77  78  79  80  81  82  83  84  85
			{ 82, 92, 97, 97, 97, 97, 97, 97, 97, 97 }, // 1
			{ 80, 90, 95, 95, 95, 95, 95, 95, 95, 95 }, // 2
			{ 78, 88, 93, 93, 93, 93, 93, 93, 93, 93 }, // 3
			{ 52, 76, 86, 91, 91, 91, 91, 91, 91, 91 }, // 4
			{ 50, 74, 84, 89, 89, 89, 89, 89, 89, 89 }, // 5
			{ 48, 72, 82, 87, 87, 87, 87, 87, 87, 87 }, // 6
			{ 01, 46, 70, 80, 85, 85, 85, 85, 85, 85 }, // 7
			{ 01, 44, 68, 78, 83, 83, 83, 83, 83, 83 }, // 8
			{ 01, 42, 66, 76, 81, 81, 81, 81, 81, 81 }, // 9
			{ 01, 01, 40, 64, 74, 79, 79, 79, 79, 79 }, // 10
			{ 01, 01, 38, 62, 72, 77, 77, 77, 77, 77 }, // 11
			{ 01, 01, 36, 60, 70, 75, 75, 75, 75, 75 }, // 12
			{ 01, 01, 01, 34, 58, 68, 73, 73, 73, 73 }, // 13
			{ 01, 01, 01, 32, 56, 66, 71, 71, 71, 71 }, // 14
			{ 01, 01, 01, 30, 54, 64, 69, 69, 69, 69 }, // 15
			{ 01, 01, 01, 01, 28, 52, 62, 67, 67, 67 }, // 16
			{ 01, 01, 01, 01, 26, 50, 60, 65, 65, 65 }, // 17
			{ 01, 01, 01, 01, 24, 48, 58, 63, 63, 63 }, // 18
			{ 01, 01, 01, 01, 01, 22, 46, 56, 61, 61 }, // 19
			{ 01, 01, 01, 01, 01, 20, 44, 54, 59, 59 }, // 20
			{ 01, 01, 01, 01, 01, 18, 42, 52, 57, 57 }, // 21
			{ 01, 01, 01, 01, 01, 01, 16, 40, 50, 55 }, // 22
			{ 01, 01, 01, 01, 01, 01, 14, 38, 48, 53 }, // 23
			{ 01, 01, 01, 01, 01, 01, 12, 36, 46, 51 }, // 24
			{ 01, 01, 01, 01, 01, 01, 01, 10, 34, 44 }, // 25
			{ 01, 01, 01, 01, 01, 01, 01, 8, 32, 42 }, //  26
			{ 01, 01, 01, 01, 01, 01, 01, 06, 30, 40 }, // 27
			{ 01, 01, 01, 01, 01, 01, 01, 01, 04, 28 }, // 28
			{ 01, 01, 01, 01, 01, 01, 01, 01, 02, 26 }, // 29
			{ 01, 01, 01, 01, 01, 01, 01, 01, 02, 24 }, // 30
	};

	/** Шанс заточки скилов 3ей профы */
	private static final int[][] _chance15 = { {},
	//    76  77  78  79  80  81  82  83  84  85
			{ 18, 28, 38, 48, 58, 82, 92, 97, 97, 97 }, // 1
			{ 01, 01, 01, 46, 56, 80, 90, 95, 95, 95 }, // 2
			{ 01, 01, 01, 01, 54, 78, 88, 93, 93, 93 }, // 3
			{ 01, 01, 01, 01, 42, 52, 76, 86, 91, 91 }, // 4
			{ 01, 01, 01, 01, 01, 50, 74, 84, 89, 89 }, // 5
			{ 01, 01, 01, 01, 01, 48, 72, 82, 87, 87 }, // 6
			{ 01, 01, 01, 01, 01, 01, 46, 70, 80, 85 }, // 7
			{ 01, 01, 01, 01, 01, 01, 44, 68, 78, 83 }, // 8
			{ 01, 01, 01, 01, 01, 01, 42, 66, 76, 81 }, // 9
			{ 01, 01, 01, 01, 01, 01, 01, 40, 64, 74 }, // 10
			{ 01, 01, 01, 01, 01, 01, 01, 38, 62, 72 }, // 11
			{ 01, 01, 01, 01, 01, 01, 01, 36, 60, 70 }, // 12
			{ 01, 01, 01, 01, 01, 01, 01, 01, 34, 58 }, // 13
			{ 01, 01, 01, 01, 01, 01, 01, 01, 32, 56 }, // 14
			{ 01, 01, 01, 01, 01, 01, 01, 01, 30, 54 }, // 15
	};

	/** Цена заточки неатакующих скиллов */
	private static final int[][] _priceBuff = { {}, //
			{ 74250, 503980 }, // 1
			{ 74250, 503980 }, // 2
			{ 74250, 503980 }, // 3
			{ 112050, 528970 }, // 4
			{ 112050, 528970 }, // 5
			{ 112050, 528970 }, // 6
			{ 150300, 554700 }, // 7
			{ 150300, 554700 }, // 8
			{ 150300, 554700 }, // 9
			{ 189000, 595020 }, // 10
			{ 189000, 595020 }, // 11
			{ 189000, 595020 }, // 12
			{ 228150, 622095 }, // 13
			{ 228150, 622095 }, // 14
			{ 228150, 622095 }, // 15
			{ 267750, 666350 }, // 16
			{ 267750, 666350 }, // 17
			{ 267750, 666350 }, // 18
			{ 307800, 696405 }, // 19
			{ 307800, 696405 }, // 20
			{ 307800, 696405 }, // 21
			{ 348300, 743165 }, // 22
			{ 348300, 743165 }, // 23
			{ 348300, 743165 }, // 24
			{ 389250, 775470 }, // 25
			{ 389250, 775470 }, // 26
			{ 389250, 775470 }, // 27
			{ 435000, 809180 }, // 28, цифра неточная
			{ 435000, 809180 }, // 29, цифра неточная
			{ 435000, 809180 }, // 30, цифра неточная
	};

	/** Цена заточки атакующих скиллов */
	private static final int[][] _priceCombat = { {}, //
			{ 133650, 907164 }, // 1
			{ 133650, 907164 }, // 2
			{ 133650, 907164 }, // 3
			{ 201690, 952146 }, // 4
			{ 201690, 952146 }, // 5
			{ 201690, 952146 }, // 6
			{ 270540, 998586 }, // 7
			{ 270540, 998586 }, // 8
			{ 270540, 998586 }, // 9
			{ 340200, 1071036 }, // 10
			{ 340200, 1071036 }, // 11
			{ 340200, 1071036 }, // 12
			{ 410670, 1281402 }, // 13
			{ 410670, 1281402 }, // 14
			{ 410670, 1281402 }, // 15
			{ 481950, 1370772 }, // 16
			{ 481950, 1370772 }, // 17
			{ 481950, 1370772 }, // 18
			{ 554040, 1432602 }, // 19
			{ 554040, 1432602 }, // 20
			{ 554040, 1432602 }, // 21
			{ 626940, 1528794 }, // 22
			{ 626940, 1528794 }, // 23
			{ 626940, 1528794 }, // 24
			{ 709430, 1631444 }, // 25, цифра неточная
			{ 709430, 1631444 }, // 26, цифра неточная
			{ 709430, 1631444 }, // 27, цифра неточная
			{ 802770, 1740986 }, // 28, цифра неточная
			{ 802770, 1740986 }, // 29, цифра неточная
			{ 802770, 1740986 }, // 30, цифра неточная
	};

	/**
	 * Шанс успешной заточки
	 */
	public int getRate(L2Player ply)
	{
		int level = _level % 100;
		int chance = Math.min(_chance[level].length - 1, ply.getLevel() - 76);
		return _maxLvl == 15 ? _chance15[level][chance] : _chance[level][chance];
	}

	public int getMaxLevel()
	{
		return _maxLvl;
	}

	public String getType()
	{
		return _type;
	}

	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + _id;
		result = PRIME * result + _level;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		if(!(obj instanceof L2EnchantSkillLearn))
			return false;
		L2EnchantSkillLearn other = (L2EnchantSkillLearn) obj;
		return getId() == other.getId() && getLevel() == other.getLevel();
	}
}