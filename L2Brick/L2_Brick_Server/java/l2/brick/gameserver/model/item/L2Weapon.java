/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2.brick.gameserver.model.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javolution.util.FastList;

import l2.brick.gameserver.handler.ISkillHandler;
import l2.brick.gameserver.handler.SkillHandler;
import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.StatsSet;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.item.instance.L2ItemInstance;
import l2.brick.gameserver.model.item.type.L2WeaponType;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.skills.Env;
import l2.brick.gameserver.skills.Formulas;
import l2.brick.gameserver.skills.SkillHolder;
import l2.brick.gameserver.skills.conditions.Condition;
import l2.brick.gameserver.skills.conditions.ConditionGameChance;
import l2.brick.gameserver.skills.funcs.Func;
import l2.brick.gameserver.skills.funcs.FuncTemplate;
import l2.brick.gameserver.templates.skills.L2SkillType;
import l2.brick.util.StringUtil;

/**
 * This class is dedicated to the management of weapons.
 */
public final class L2Weapon extends L2Item
{
	private final L2WeaponType _type;
	private final boolean _isMagicWeapon;
	private final int _rndDam;
	private final int _soulShotCount;
	private final int _spiritShotCount;
	private final int _mpConsume;
	/**
	 * Skill that activates when item is enchanted +4 (for duals).
	 */
	private SkillHolder _enchant4Skill = null;
	private final int _changeWeaponId;
	
	// Attached skills for Special Abilities
	private SkillHolder _skillsOnCast;
	private Condition _skillsOnCastCondition = null;
	private SkillHolder _skillsOnCrit;
	private Condition _skillsOnCritCondition = null;
	
	private final int _reuseDelay;
	
	private final boolean _isForceEquip;
	private final boolean _isAttackWeapon;
	private final boolean _useWeaponSkillsOnly;
	
	/**
	 * Constructor for Weapon.
	 * @param set the StatsSet designating the set of couples (key,value) characterizing the weapon.
	 * @see L2Item constructor
	 */
	public L2Weapon(StatsSet set)
	{
		super(set);
		_type = L2WeaponType.valueOf(set.getString("weapon_type", "none").toUpperCase());
		_type1 = L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE;
		_type2 = L2Item.TYPE2_WEAPON;
		_isMagicWeapon = set.getBool("is_magic_weapon", false);
		_soulShotCount = set.getInteger("soulshots", 0);
		_spiritShotCount = set.getInteger("spiritshots", 0);
		_rndDam = set.getInteger("random_damage", 0);
		_mpConsume = set.getInteger("mp_consume", 0);
		_reuseDelay = set.getInteger("reuse_delay", 0);
		
		String skill = set.getString("enchant4_skill", null);
		if (skill != null)
		{
			String[] info = skill.split("-");
			
			if ((info != null) && (info.length == 2))
			{
				int id = 0;
				int level = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
				}
				catch (Exception nfe)
				{
					// Incorrect syntax, dont add new skill
					_log.info(StringUtil.concat("> Couldnt parse ", skill, " in weapon enchant skills! item ", toString()));
				}
				if ((id > 0) && (level > 0))
				{
					_enchant4Skill = new SkillHolder(id, level);
				}
			}
		}
		
		skill = set.getString("oncast_skill", null);
		if (skill != null)
		{
			String[] info = skill.split("-");
			String infochance = set.getString("oncast_chance", null);
			if ((info != null) && (info.length == 2))
			{
				int id = 0;
				int level = 0;
				int chance = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
					if (infochance != null)
					{
						chance = Integer.parseInt(infochance);
					}
				}
				catch (Exception nfe)
				{
					// Incorrect syntax, dont add new skill
					_log.info(StringUtil.concat("> Couldnt parse ", skill, " in weapon oncast skills! item ", toString()));
				}
				if ((id > 0) && (level > 0) && (chance > 0))
				{
					_skillsOnCast = new SkillHolder(id, level);
					if (infochance != null)
					{
						_skillsOnCastCondition = new ConditionGameChance(chance);
					}
				}
			}
		}
		
		skill = set.getString("oncrit_skill", null);
		if (skill != null)
		{
			String[] info = skill.split("-");
			String infochance = set.getString("oncrit_chance", null);
			if ((info != null) && (info.length == 2))
			{
				int id = 0;
				int level = 0;
				int chance = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
					if (infochance != null)
					{
						chance = Integer.parseInt(infochance);
					}
				}
				catch (Exception nfe)
				{
					// Incorrect syntax, dont add new skill
					_log.info(StringUtil.concat("> Couldnt parse ", skill, " in weapon oncrit skills! item ", toString()));
				}
				if ((id > 0) && (level > 0) && (chance > 0))
				{
					_skillsOnCrit = new SkillHolder(id, level);
					if (infochance != null)
					{
						_skillsOnCritCondition = new ConditionGameChance(chance);
					}
				}
			}
		}
		
		_changeWeaponId = set.getInteger("change_weaponId", 0);
		_isForceEquip = set.getBool("isForceEquip", false);
		_isAttackWeapon = set.getBool("isAttackWeapon", true);
		_useWeaponSkillsOnly = set.getBool("useWeaponSkillsOnly", false);
	}
	
	/**
	 * @return the type of Weapon
	 */
	@Override
	public L2WeaponType getItemType()
	{
		return _type;
	}
	
	/**
	 * @return the ID of the Etc item after applying the mask.
	 */
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * @return {@code true} if the weapon is magic, {@code false} otherwise.
	 */
	public boolean isMagicWeapon()
	{
		return _isMagicWeapon;
	}
	
	/**
	 * @return the quantity of SoulShot used.
	 */
	public int getSoulShotCount()
	{
		return _soulShotCount;
	}
	
	/**
	 * @return the quantity of SpiritShot used.
	 */
	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}
	
	/**
	 * @return the random damage inflicted by the weapon.
	 */
	public int getRandomDamage()
	{
		return _rndDam;
	}
	
	/**
	 * @return the Reuse Delay of the L2Weapon.
	 */
	public int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	/**
	 * @return the MP consumption with the weapon.
	 */
	public int getMpConsume()
	{
		return _mpConsume;
	}
	
	/**
	 * @return the skill that player get when has equipped weapon +4 or more (for duals SA).
	 */
	public L2Skill getEnchant4Skill()
	{
		if (_enchant4Skill == null)
		{
			return null;
		}
		return _enchant4Skill.getSkill();
	}
	
	/**
	 * @return the Id in which weapon this weapon can be changed.
	 */
	public int getChangeWeaponId()
	{
		return _changeWeaponId;
	}
	
	/**
	 * @return {@code true} if the weapon is force equip, {@code false} otherwise.
	 */
	public boolean isForceEquip()
	{
		return _isForceEquip;
	}
	
	/**
	 * @return {@code true} if the weapon is attack weapon, {@code false} otherwise.
	 */
	public boolean isAttackWeapon()
	{
		return _isAttackWeapon;
	}
	
	/**
	 * @return {@code true} if the weapon is skills only, {@code false} otherwise.
	 */
	public boolean useWeaponSkillsOnly()
	{
		return _useWeaponSkillsOnly;
	}
	
	/**
	 * @param instance the L2ItemInstance pointing out the weapon.
	 * @param player the L2Character pointing out the player.
	 * @return an array of Func objects containing the list of functions used by the weapon.
	 */
	@Override
	public Func[] getStatFuncs(L2ItemInstance instance, L2Character player)
	{
		if ((_funcTemplates == null) || (_funcTemplates.length == 0))
		{
			return _emptyFunctionSet;
		}
		
		ArrayList<Func> funcs = new ArrayList<Func>(_funcTemplates.length);
		
		Env env = new Env();
		env.player = player;
		env.item = instance;
		Func f;
		
		for (FuncTemplate t : _funcTemplates)
		{
			f = t.getFunc(env, instance);
			if (f != null)
			{
				funcs.add(f);
			}
		}
		
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	/**
	 * @param caster the L2Character pointing out the caster
	 * @param target the L2Character pointing out the target
	 * @param crit the boolean tells whether the hit was critical
	 * @return the effects of skills associated with the item to be triggered onHit.
	 */
	public L2Effect[] getSkillEffects(L2Character caster, L2Character target, boolean crit)
	{
		if ((_skillsOnCrit == null) || !crit)
		{
			return _emptyEffectSet;
		}
		List<L2Effect> effects = new FastList<L2Effect>();
		
		if (_skillsOnCritCondition != null)
		{
			Env env = new Env();
			env.player = caster;
			env.target = target;
			env.skill = _skillsOnCrit.getSkill();
			if (!_skillsOnCritCondition.test(env))
			{
				return _emptyEffectSet; // Skill condition not met
			}
		}
		
		byte shld = Formulas.calcShldUse(caster, target, _skillsOnCrit.getSkill());
		if (!Formulas.calcSkillSuccess(caster, target, _skillsOnCrit.getSkill(), shld, false, false, false))
		{
			return _emptyEffectSet; // These skills should not work on RaidBoss
		}
		if (target.getFirstEffect(_skillsOnCrit.getSkill().getId()) != null)
		{
			target.getFirstEffect(_skillsOnCrit.getSkill().getId()).exit();
		}
		for (L2Effect e : _skillsOnCrit.getSkill().getEffects(caster, target, new Env(shld, false, false, false)))
		{
			effects.add(e);
		}
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	/**
	 * @param caster the L2Character pointing out the caster
	 * @param target the L2Character pointing out the target
	 * @param trigger the L2Skill pointing out the skill triggering this action
	 * @return the effects of skills associated with the item to be triggered onCast.
	 */
	public L2Effect[] getSkillEffects(L2Character caster, L2Character target, L2Skill trigger)
	{
		if (_skillsOnCast == null)
		{
			return _emptyEffectSet;
		}
		if (trigger.isOffensive() != _skillsOnCast.getSkill().isOffensive())
		{
			return _emptyEffectSet; // Trigger only same type of skill
		}
		if (trigger.isToggle() && (_skillsOnCast.getSkill().getSkillType() == L2SkillType.BUFF))
		{
			return _emptyEffectSet; // No buffing with toggle skills
		}
		if (!trigger.isMagic() && (_skillsOnCast.getSkill().getSkillType() == L2SkillType.BUFF))
		{
			return _emptyEffectSet; // No buffing with not magic skills
		}
		
		if (_skillsOnCastCondition != null)
		{
			Env env = new Env();
			env.player = caster;
			env.target = target;
			env.skill = _skillsOnCast.getSkill();
			if (!_skillsOnCastCondition.test(env))
			{
				return _emptyEffectSet;
			}
		}
		
		byte shld = Formulas.calcShldUse(caster, target, _skillsOnCast.getSkill());
		if (_skillsOnCast.getSkill().isOffensive() && !Formulas.calcSkillSuccess(caster, target, _skillsOnCast.getSkill(), shld, false, false, false))
		{
			return _emptyEffectSet;
		}
		
		// Get the skill handler corresponding to the skill type
		ISkillHandler handler = SkillHandler.getInstance().getHandler(_skillsOnCast.getSkill().getSkillType());
		
		L2Character[] targets = new L2Character[1];
		targets[0] = target;
		
		// Launch the magic skill and calculate its effects
		if (handler != null)
		{
			handler.useSkill(caster, _skillsOnCast.getSkill(), targets);
		}
		else
		{
			_skillsOnCast.getSkill().useSkill(caster, targets);
		}
		
		// notify quests of a skill use
		if (caster instanceof L2PcInstance)
		{
			// Mobs in range 1000 see spell
			Collection<L2Object> objs = caster.getKnownList().getKnownObjects().values();
			for (L2Object spMob : objs)
			{
				if (spMob instanceof L2Npc)
				{
					L2Npc npcMob = (L2Npc) spMob;
					
					if (npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null)
					{
						for (Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
						{
							quest.notifySkillSee(npcMob, (L2PcInstance) caster, _skillsOnCast.getSkill(), targets, false);
						}
					}
				}
			}
		}
		return _emptyEffectSet;
	}
}
