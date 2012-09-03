package l2rt.gameserver.model;

import javolution.util.FastMap;
import l2rt.gameserver.model.L2Skill.SkillType;
import l2rt.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.skills.effects.EffectTemplate;
import l2rt.gameserver.skills.funcs.Func;
import l2rt.gameserver.skills.skillclasses.Transformation;
import l2rt.util.GArray;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EffectList
{
	private long ownerStoreId;
	private ConcurrentLinkedQueue<L2Effect> _effects;

	//private Object _lock = new Object();

	public EffectList(L2Character owner)
	{
		setOwner(owner);
	}

	/**
	 * Возвращает число эффектов соответствующее данному скиллу
	 */
	public int getEffectsCountForSkill(int skill_id)
	{
		if(_effects == null)
			return 0;
		int count = 0;
		for(L2Effect e : _effects)
			if(e.getSkill().getId() == skill_id)
				count++;
		return count;
	}

	public L2Effect getEffectByType(EffectType et)
	{
		if(_effects != null)
			for(L2Effect e : _effects)
				if(e.getEffectType() == et)
					return e;
		return null;
	}

	public GArray<L2Effect> getEffectsBySkill(L2Skill skill)
	{
		if(skill == null)
			return null;
		return getEffectsBySkillId(skill.getId());
	}

	public GArray<L2Effect> getEffectsBySkillId(int skillId)
	{
		if(_effects == null)
			return null;
		GArray<L2Effect> temp = new GArray<L2Effect>();
		for(L2Effect e : _effects)
			if(e.getSkill().getId() == skillId)
				temp.add(e);

		return temp.isEmpty() ? null : temp;
	}

	public L2Effect getEffectByIndexAndType(int skill_id, EffectType type)
	{
		if(_effects == null)
			return null;
		for(L2Effect e : _effects)
			if(e.getSkill().getId() == skill_id && e.getEffectType() == type)
				return e;
		return null;
	}

	public L2Effect getEffectByStackType(String type)
	{
		if(_effects == null)
			return null;
		for(L2Effect e : _effects)
			if(e.getStackType().equals(type))
				return e;
		return null;
	}

	public boolean containEffectFromSkills(int[] skillIds)
	{
		if(_effects == null)
			return false;
		for(L2Effect e : _effects)
		{
			int id1 = e.getSkill().getId();
			for(int id2 : skillIds)
				if(id1 == id2)
					return true;
		}

		return false;
	}

	public ConcurrentLinkedQueue<L2Effect> getAllEffects()
	{
		if(_effects == null)
			return new ConcurrentLinkedQueue<L2Effect>();
		return _effects;
	}

	/**
	 * @param offensive: 0 - any, less 0 - positive, more 0 - negative
	 * @return
	 */
	public ConcurrentLinkedQueue<L2Effect> getAllCancelableEffects(int offensive)
	{
		ConcurrentLinkedQueue<L2Effect> ret = new ConcurrentLinkedQueue<L2Effect>();
		for(L2Effect e : getAllEffects())
			if(e.getSkill().isCancelable())
				if(offensive == 0 || e.getSkill().isOffensive() == (offensive > 0))
					ret.add(e);
		return ret;
	}

	/**
	 * @param offensive: 0 - any, less 0 - positive, more 0 - negative
	 * @return
	 */
	public int getAllCancelableEffectsCount(int offensive)
	{
		int ret = 0;
		for(L2Effect e : getAllEffects())
			if(e.getSkill().isCancelable())
				if(offensive == 0 || e.getSkill().isOffensive() == (offensive > 0))
					ret++;
		return ret;
	}

	public boolean isEmpty()
	{
		return _effects == null || _effects.isEmpty();
	}

	/**
	 * Возвращает первые эффекты для всех скиллов. Нужно для отображения не
	 * более чем 1 иконки для каждого скилла.
	 */
	public L2Effect[] getAllFirstEffects()
	{
		if(_effects == null)
			return new L2Effect[0];

		FastMap<Integer, L2Effect> temp = new FastMap<Integer, L2Effect>();

		if(_effects != null)
			for(L2Effect ef : _effects)
				if(ef != null)
					temp.put(ef.getSkill().getId(), ef);

		Collection<L2Effect> temp2 = temp.values();
		return temp2.toArray(new L2Effect[temp2.size()]);
	}

	private boolean isNotUsedBuffSlot(L2Effect ef)
	{
		return ef.getSkill().isLikePassive() || ef.getSkill().isOffensive() || ef.getSkill().isToggle() || ef.getSkill() instanceof Transformation || ef.getStackType().equalsIgnoreCase("HpRecoverCast");
	}

	/**
	 * Ограничение на количество бафов
	 */
	private void checkBuffSlots(L2Effect newEffect)
	{
		L2Character owner = getOwner();
		if(owner == null)
			return;

		if(_effects == null || _effects.size() < 12)
			return;

		if(isNotUsedBuffSlot(newEffect))
			return;

		int buffs = 0;
		int songdance = 0;
		GArray<Integer> skills = new GArray<Integer>(_effects.size());
		for(L2Effect ef : _effects)
			if(ef != null && ef.isInUse())
			{
				if(ef.getSkill().equals(newEffect.getSkill())) // мы уже имеем эффект от этого скилла
					return;
				if(!isNotUsedBuffSlot(ef) && !skills.contains(ef.getSkill().getId()))
				{
					if(ef.getSkill().isMusic())
						songdance++;
					else
						buffs++;
					skills.add(ef.getSkill().getId());
				}
			}

		if(newEffect.getSkill().isMusic() ? songdance < owner.getSongLimit() : buffs < owner.getBuffLimit())
			return;

		for(L2Effect ef : _effects)
			if(ef != null && ef.isInUse())
				if(!isNotUsedBuffSlot(ef) && ef.getSkill().isMusic() == newEffect.getSkill().isMusic())
				{
					stopEffect(ef.getSkill().getId());
					break;
				}
	}

	public static boolean checkStackType(EffectTemplate ef1, EffectTemplate ef2)
	{
		if(ef1._stackType != EffectTemplate.NO_STACK && ef1._stackType.equalsIgnoreCase(ef2._stackType))
			return true;
		if(ef1._stackType != EffectTemplate.NO_STACK && ef1._stackType.equalsIgnoreCase(ef2._stackType2))
			return true;
		if(ef1._stackType2 != EffectTemplate.NO_STACK && ef1._stackType2.equalsIgnoreCase(ef2._stackType))
			return true;
		if(ef1._stackType2 != EffectTemplate.NO_STACK && ef1._stackType2.equalsIgnoreCase(ef2._stackType2))
			return true;
		return false;
	}

	public synchronized void addEffect(L2Effect newEffect)
	{
		L2Character owner = getOwner();
		if(owner == null)
			return;

		if(newEffect == null)
			return;

		// Хербы при вызванном саммоне делятся с саммоном пополам
		if((owner.isSummon() || owner.getPet() != null && !owner.getPet().isDead() && owner.getPet().isSummon()) && (newEffect.getSkill().getId() >= 2278 && newEffect.getSkill().getId() <= 2285 || newEffect.getSkill().getId() >= 2512 && newEffect.getSkill().getId() <= 2514))
		{
			newEffect.setPeriod(newEffect.getPeriod() / 2);
			if(!owner.isSummon())
				owner.getPet().altUseSkill(newEffect.getSkill(), owner.getPet());
		}

		boolean sheduleNew = false;

		if(_effects == null)
			_effects = new ConcurrentLinkedQueue<L2Effect>();

		//System.out.println(owner + " " + Arrays.toString(_effects.toArray()));

		// затычка на баффы повышающие хп/мп
		double hp = owner.getCurrentHp();
		double mp = owner.getCurrentMp();
		double cp = owner.getCurrentCp();

		// Проверка на имунность к бафам/дебафам
		if(owner.isEffectImmune() && newEffect.getEffectType() != EffectType.BuffImmunity)
		{
			SkillType st = newEffect.getSkill().getSkillType();
			if(st == SkillType.BUFF || st == SkillType.DEBUFF)
				return;
		}

		String stackType = newEffect.getStackType();

		if(stackType == EffectTemplate.NO_STACK)
		{
			// Удаляем такие же эффекты
			for(L2Effect ef : _effects)
				if(ef != null && ef.isInUse() && ef.getStackType() == EffectTemplate.NO_STACK && ef.getSkill().getId() == newEffect.getSkill().getId() && ef.getEffectType() == newEffect.getEffectType())
					// Если оставшаяся длительность старого эффекта больше чем длительность нового, то оставляем старый.
					if(newEffect.getTimeLeft() > ef.getTimeLeft())
						ef.exit();
					else
						return;
		}
		else
			// Проверяем, нужно ли накладывать эффект, при совпадении StackType.
			// Новый эффект накладывается только в том случае, если у него больше StackOrder и больше длительность.
			// Если условия подходят - удаляем старый.
			for(L2Effect ef : _effects)
				if(ef != null)
				{
					if(!ef.isInUse())
					{
						ef.exit();
						continue;
					}

					if(!checkStackType(ef._template, newEffect._template))
						continue;

					if(ef.getSkill().getId() == newEffect.getSkill().getId() && ef.getEffectType() != newEffect.getEffectType())
						break;

					// Эффекты со StackOrder == -1 заменить нельзя (например, Root).
					if(ef.getStackOrder() == -1)
						return;

					if((sheduleNew = (ef.maybeScheduleNext(newEffect) == 0)) == true)
						break;
				}

		if(!sheduleNew)
		{
			// Проверяем на лимиты бафов/дебафов
			checkBuffSlots(newEffect);

			// Добавляем новый эффект
			_effects.add(newEffect);

			if(newEffect.getEffector().isPlayer() && newEffect.getEffector().getDuel() != null)
				newEffect.getEffector().getDuel().onBuff((L2Player) newEffect.getEffector(), newEffect);

			// Применяем эффект к параметрам персонажа
			owner.addStatFuncs(newEffect.getStatFuncs());

			// Запускаем эффект
			newEffect.setInUse(true);
			newEffect.setActive(true);

			// затычка на баффы повышающие хп/мп
			for(Func f : newEffect.getStatFuncs())
				if(f._stat == Stats.MAX_HP)
					owner.setCurrentHp(hp, false);
				else if(f._stat == Stats.MAX_MP)
					owner.setCurrentMp(mp);
				else if(f._stat == Stats.MAX_CP)
					owner.setCurrentCp(cp);

			// Обновляем иконки
			owner.updateEffectIcons();
		}
	}

	/**
	 * Вызывающий метод синхронизирован, дополнительная синхронизация не нужна.
	 * @see l2rt.gameserver.model.L2Effect#stopEffectTask()
	 * @param effect эффект для удаления
	 */
	public void removeEffect(L2Effect effect)
	{
		L2Character owner = getOwner();
		if(owner == null)
			return;

		if(effect == null || _effects == null || !_effects.contains(effect))
			return;

		owner.removeStatsOwner(effect);
		effect.setInUse(false);

		_effects.remove(effect);

		if(effect.getNext() != null)
		{
			L2Effect next = effect.getNext();
			boolean add = true;
			for(L2Effect ef : _effects)
				if(ef != null && ef.isInUse() && checkStackType(ef._template, next._template))
				{
					add = false;
					break;
				}
			if(add)
			{
				next.setActive(true);
				_effects.add(next);
				owner.addStatFuncs(next.getStatFuncs());
				next.updateEffects();
			}
		}

		if(owner.isPlayer() && effect.getStackType().equalsIgnoreCase("HpRecoverCast"))
			owner.sendPacket(new ShortBuffStatusUpdate());
		owner.updateEffectIcons();
	}

	/**
	 * Низкоуровневый метод, не использовать
	 */
	public void removeFromList(L2Effect effect)
	{
		_effects.remove(effect);
	}

	public void stopAllEffects()
	{
		L2Character owner = getOwner();
		if(owner == null)
			return;

		if(_effects != null)
		{
			owner.setMassUpdating(true);
			for(L2Effect e : _effects)
				if(e != null)
					e.exit();
			owner.setMassUpdating(false);
			owner.sendChanges();
			owner.updateEffectIcons();
		}
	}

	public void stopEffect(int skillId)
	{
		if(_effects != null)
			for(L2Effect e : _effects)
				if(e != null && e.getSkill().getId() == skillId)
					e.exit();
	}

	public void stopEffect(L2Skill skill)
	{
		if(skill != null)
			stopEffect(skill.getId());
	}

	public void stopEffectByDisplayId(int skillId)
	{
		if(_effects != null)
			for(L2Effect e : _effects)
				if(e != null && e.getSkill().getDisplayId() == skillId)
					e.exit();
	}

	public void stopEffects(EffectType type)
	{
		if(_effects != null)
			for(L2Effect e : _effects)
				if(e.getEffectType() == type)
					e.exit();
	}

	/*
	 * Находит скиллы с указанным эффектом, и останавливает у этих скиллов все эффекты (не только указанный).
	 */
	public void stopAllSkillEffects(EffectType type)
	{
		GArray<Integer> temp = new GArray<Integer>();
		if(_effects != null)
			for(L2Effect e : _effects)
				if(e.getEffectType() == type)
					temp.add(e.getSkill().getId());
		for(Integer id : temp)
			stopEffect(id);
	}

	public void setOwner(L2Character owner)
	{
		ownerStoreId = owner == null ? 0 : owner.getStoredId();
	}

	private L2Character getOwner()
	{
		return L2ObjectsStorage.getAsCharacter(ownerStoreId);
	}
}