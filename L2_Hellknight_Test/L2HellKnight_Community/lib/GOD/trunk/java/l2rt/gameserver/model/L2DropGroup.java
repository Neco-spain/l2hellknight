package l2rt.gameserver.model;

import l2rt.Config;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.base.ItemToDrop;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2ReflectionBossInstance;
import l2rt.util.GArray;
import l2rt.util.Rnd;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map.Entry;

public class L2DropGroup implements Cloneable
{
	private int _id;
	private double _chance;
	private boolean _isAdena = false; // Шанс фиксирован, растет только количество
	private boolean _fixedQty = false; // Вместо увеличения количества используется увеличение количества роллов группы
	private boolean _notRate = false; // Рейты вообще не применяются
	private GArray<L2DropData> _items = new GArray<L2DropData>();

	public L2DropGroup(int id)
	{
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public boolean fixedQty()
	{
		return _fixedQty;
	}

	public boolean notRate()
	{
		return _notRate;
	}

	public void addDropItem(L2DropData item)
	{
		if(item.getItem().isAdena())
			_isAdena = true;
		if(item.getItem().isRaidAccessory() || item.getItem().isArrow() || item.getItem().isHerb())
			_notRate = true;
		if(item.getItem().isEquipment() || item.getItem().isKeyMatherial())
			_fixedQty = true;
		item.setChanceInGroup(_chance);
		_chance += item.getChance();
		_items.add(item);
	}

	/**
	 * Возвращает список вещей или копию списка
	 */
	public GArray<L2DropData> getDropItems(boolean copy)
	{
		if(!copy)
			return _items;
		GArray<L2DropData> temp = new GArray<L2DropData>();
		temp.addAll(_items);
		return temp;
	}

	/**
	 * Возвращает полностью независимую копию группы
	 */
	@Override
	public L2DropGroup clone()
	{
		L2DropGroup ret = new L2DropGroup(_id);
		for(L2DropData i : _items)
			ret.addDropItem(i.clone());
		return ret;
	}

	/**
	 * Возвращает оригинальный список вещей если рейты не нужны или клон с примененными рейтами
	 */
	public GArray<L2DropData> getRatedItems(double mod)
	{
		if(mod == 1 || _notRate)
			return _items;
		GArray<L2DropData> ret = new GArray<L2DropData>();

		for(L2DropData i : _items)
			ret.add(i.clone()); // создаем копию группы

		double perItemChance = 1000000. / ret.size();
		double gChance = 0;
		for(L2DropData i : ret)
		{
			double avgQty = (i.getMinDrop() + i.getMaxDrop()) / 2.; // среднее количество дропа
			double newChance = mod * i.getChance() * avgQty; // новый шанс группы плюс количество дропа, например 1-4 с шансом 43% при рейте 3х дадут ((1+4)/2)*0.43*3=3.225
			long avgCount = (long) Math.ceil(newChance / perItemChance); // новое количество дропа, допустим при количестве групп 3 пример выше даст 3.225/(1/3)=ceil(9.675)=10

			long min = avgCount, max = avgCount; // создаем некоторый разброс количества
			long shift = Math.min(Math.round(avgCount * 1. / 3.), avgCount - 1);
			if(shift > 0)
			{
				min -= shift;
				max += shift;
			}
			i.setMinDrop(min);
			i.setMaxDrop(max);

			i.setChance(newChance / avgCount); // новый шанс считается как полный новый шанс деленный на среднее количество, для примера выше 32.25%
			i.setChanceInGroup(gChance);
			gChance += i.getChance();
		}

		return ret;
	}

	/**
	 * Эта функция выбирает одну вещь из группы
	 * Используется в основном механизме расчета дропа
	 */
	public Collection<ItemToDrop> roll(int diff, boolean isSpoil, L2MonsterInstance monster, L2Player player, double mod)
	{
		if(_isAdena)
			return rollAdena(diff, player, mod);
		if(isSpoil)
			return rollSpoil(diff, player, mod);
		if(monster.isRaid() || _notRate || _fixedQty)
			return rollFixedQty(diff, monster, player, mod);

		// если множитель дропа большой разбивать дроп на кучки
		// количество итераций не более L2Drop.MAX_DROP_ITERATIONS
		double cmod = mod * ((monster.isRaid() || monster instanceof L2ReflectionBossInstance) ? Config.RATE_DROP_RAIDBOSS : Config.RATE_DROP_ITEMS * player.getRateItems());
		if(cmod > Config.RATE_BREAKPOINT)
		{
			long iters = Math.min((long) Math.ceil(cmod / Config.RATE_BREAKPOINT), Config.MAX_DROP_ITERATIONS);
			GArray<ItemToDrop> ret = new GArray<ItemToDrop>();
			for(int i = 0; i < iters; i++)
				ret.addAll(rollNormal(diff, monster, player, mod / iters));
			return ret;
		}

		return rollNormal(diff, monster, player, mod);
	}

	public Collection<ItemToDrop> rollNormal(int diff, L2MonsterInstance monster, L2Player player, double mod)
	{
		// Поправка на глубоко синих мобов
		if(Config.DEEPBLUE_DROP_RULES && diff > 0)
			mod *= Experience.penaltyModifier(diff, 9);
		if(mod <= 0)
			return null;

		float rate;
		if(monster.isRaid() || monster instanceof L2ReflectionBossInstance)
			rate = Config.RATE_DROP_RAIDBOSS * player.getRateItems();
		else
			rate = Config.RATE_DROP_ITEMS * player.getRateItems();

		double calcChance = 0;
		double rollChance = 0;
		GArray<L2DropData> items;
		double mult = 1;

		items = getRatedItems(rate * mod);

		// Считаем шанс группы
		for(L2DropData i : items)
			calcChance += i.getChance();
		rollChance = calcChance;

		if(Rnd.get(1, L2Drop.MAX_CHANCE) > calcChance)
			return null;

		GArray<ItemToDrop> ret = new GArray<ItemToDrop>();
		rollFinal(items, ret, mult, rollChance);
		return ret;
	}

	public Collection<ItemToDrop> rollFixedQty(int diff, L2MonsterInstance monster, L2Player player, double mod)
	{
		// Поправка на глубоко синих мобов
		if(Config.DEEPBLUE_DROP_RULES && diff > 0)
			mod *= Experience.penaltyModifier(diff, 9);
		if(mod <= 0)
			return null;
		double rate;
		if(_notRate)
			rate = Math.min(mod, 1);
		else if(monster.isRaid() || monster instanceof L2ReflectionBossInstance)
			rate = Config.RATE_DROP_RAIDBOSS * mod;
		else
			rate = Config.RATE_DROP_ITEMS * player.getRateItems() * mod;

		// Считаем шанс группы
		double calcChance = _chance * rate;
		Entry<Double, Integer> e = balanceChanceAndMult(calcChance);
		calcChance = e.getKey();
		int dropmult = e.getValue();

		GArray<ItemToDrop> ret = new GArray<ItemToDrop>();
		for(int n = 0; n < dropmult; n++)
			if(Rnd.get(1, L2Drop.MAX_CHANCE) < calcChance)
				rollFinal(_items, ret, 1, _chance);
		return ret;
	}

	private Collection<ItemToDrop> rollSpoil(int diff, L2Player player, double mod)
	{
		float rate = Config.RATE_DROP_SPOIL * player.getRateSpoil();
		// Поправка на глубоко синих мобов
		if(Config.DEEPBLUE_DROP_RULES && diff > 0)
			mod *= Experience.penaltyModifier(diff, 9);
		if(mod <= 0)
			return null;

		// Считаем шанс группы
		double calcChance = _chance * rate * mod;
		Entry<Double, Integer> e = balanceChanceAndMult(calcChance);
		calcChance = e.getKey();
		int dropmult = e.getValue();

		if(Rnd.get(1, L2Drop.MAX_CHANCE) > calcChance)
			return null;

		GArray<ItemToDrop> ret = new GArray<ItemToDrop>(1);
		rollFinal(_items, ret, dropmult, _chance);
		return ret;
	}

	private Collection<ItemToDrop> rollAdena(int diff, L2Player player, double mod)
	{
		float rate = Config.getRateAdena(player);
		if(Config.DEEPBLUE_DROP_RULES && diff > 0)
			mod *= Experience.penaltyModifier(diff, 9);
		double chance = _chance;
		if(mod > 10)
		{
			mod *= _chance / L2Drop.MAX_CHANCE;
			chance = L2Drop.MAX_CHANCE;
		}
		if(mod <= 0 || Rnd.get(1, L2Drop.MAX_CHANCE) > chance)
			return null;
		double mult = rate * mod;

		GArray<ItemToDrop> ret = new GArray<ItemToDrop>(1);
		rollFinal(_items, ret, mult, _chance);
		for(ItemToDrop i : ret)
			i.isAdena = true;
		return ret;
	}

	public static Entry<Double, Integer> balanceChanceAndMult(Double calcChance)
	{
		Integer dropmult = 1;
		if(calcChance > L2Drop.MAX_CHANCE)
		{
			if(calcChance % L2Drop.MAX_CHANCE == 0) // если кратен 100% то тупо умножаем количество
				dropmult = (int) (calcChance / L2Drop.MAX_CHANCE);
			else
				// иначе балансируем
				dropmult = (int) Math.ceil(calcChance / L2Drop.MAX_CHANCE); // множитель равен шанс / 100% округление вверх
			calcChance = calcChance / dropmult; // шанс равен шанс / множитель
			// в результате получаем увеличение количества и уменьшение шанса, при этом шанс не падает ниже 50%
		}
		return new SimpleEntry<Double, Integer>(calcChance, dropmult);
	}

	private void rollFinal(GArray<L2DropData> items, GArray<ItemToDrop> ret, double mult, double chanceSum)
	{
		// перебираем все вещи в группе и проверяем шанс
		int chance = Rnd.get(0, (int) chanceSum);
		for(L2DropData i : items)
		{
			if(chance < i.getChanceInGroup())
				continue;
			boolean notlast = false;
			for(L2DropData t : items)
				if(t.getChanceInGroup() > i.getChanceInGroup() && chance > t.getChanceInGroup())
				{
					notlast = true;
					break;
				}
			if(notlast)
				continue;

			ItemToDrop t = new ItemToDrop(i.getItemId());

			if(i.getMinDrop() >= i.getMaxDrop())
				t.count = (int) Math.round(i.getMinDrop() * mult);
			else
				t.count = Rnd.get((int) Math.round(i.getMinDrop() * mult), (int) Math.round(i.getMaxDrop() * mult));

			ret.add(t);
			break;
		}
	}

	public double getChance()
	{
		return _chance;
	}

	public void setChance(double chance)
	{
		_chance = chance;
	}

	public boolean isAdena()
	{
		return _isAdena;
	}
}