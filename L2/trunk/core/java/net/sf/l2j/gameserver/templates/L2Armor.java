package net.sf.l2j.gameserver.templates;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.skills.funcs.FuncTemplate;

public final class L2Armor extends L2Item
{
	private final int _avoidModifier;
	private final int _pDef;
	private final int _mDef;
	private final int _mpBonus;
	private final int _hpBonus;
	private L2Skill _itemSkill = null; // for passive skill

	public L2Armor(L2ArmorType type, StatsSet set)
	{
		super(type, set);
		_avoidModifier = set.getInteger("avoid_modify");
		_pDef          = set.getInteger("p_def");
		_mDef          = set.getInteger("m_def");
		_mpBonus       = set.getInteger("mp_bonus", 0);
		_hpBonus       = set.getInteger("hp_bonus", 0);

		int sId = set.getInteger("item_skill_id");
		int sLv = set.getInteger("item_skill_lvl");
		if(sId > 0 && sLv > 0)
			_itemSkill = SkillTable.getInstance().getInfo(sId,sLv);
	}

	/**
	 * Returns the type of the armor.
	 * @return L2ArmorType
	 */
	@Override
	public L2ArmorType getItemType()
	{
		return (L2ArmorType)super._type;
	}

	/**
	 * Returns the ID of the item after applying the mask.
	 * @return int : ID of the item
	 */
	@Override
	public final int getItemMask()
	{
		return getItemType().mask();
	}

	/**
	 * Returns the magical defense of the armor
	 * @return int : value of the magic defense
	 */
	public final int getMDef()
	{
		return _mDef;
	}

	/**
	 * Returns the physical defense of the armor
	 * @return int : value of the physical defense
	 */
	public final int getPDef()
	{
		return _pDef;
	}

	/**
	 * Returns avoid modifier given by the armor
	 * @return int : avoid modifier
	 */
	public final int getAvoidModifier()
	{
		return _avoidModifier;
	}

	/**
	 * Returns magical bonus given by the armor
	 * @return int : value of the magical bonus
	 */
	public final int getMpBonus()
	{
		return _mpBonus;
	}

	/**
	 * Returns physical bonus given by the armor
	 * @return int : value of the physical bonus
	 */
	public final int getHpBonus()
	{
		return _hpBonus;
	}

	/**
	 * Returns passive skill linked to that armor
	 * @return
	 */
	public L2Skill getSkill()
	{
		return _itemSkill;
	}

	@Override
	public Func[] getStatFuncs(L2ItemInstance instance, L2Character player)
    {
    	List<Func> funcs = new FastList<Func>();
    	if (_funcTemplates != null)
    	{
    		for (FuncTemplate t : _funcTemplates) {
		    	Env env = new Env();
		    	env.player = player;
		    	env.item = instance;
		    	Func f = t.getFunc(env, instance);
		    	if (f != null)
			    	funcs.add(f);
    		}
    	}
    	return funcs.toArray(new Func[funcs.size()]);
    }
}
