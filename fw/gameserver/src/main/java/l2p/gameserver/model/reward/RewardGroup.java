package l2p.gameserver.model.reward;

import l2p.commons.math.SafeMath;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RewardGroup implements Cloneable {
    private double _chance;
    private boolean _isAdena = false; // Шанс фиксирован, растет только количество
    private boolean _notRate = false; // Рейты вообще не применяются
    private List<RewardData> _items = new ArrayList<RewardData>();
    private double _chanceSum;

    public RewardGroup(double chance) {
        setChance(chance);
    }

    public boolean notRate() {
        return _notRate;
    }

    public void setNotRate(boolean notRate) {
        _notRate = notRate;
    }

    public double getChance() {
        return _chance;
    }

    public void setChance(double chance) {
        _chance = chance;
    }

    public boolean isAdena() {
        return _isAdena;
    }

    public void setIsAdena(boolean isAdena) {
        _isAdena = isAdena;
    }

    public void addData(RewardData item) {
        if (item.getItem().isAdena())
            _isAdena = true;
        _chanceSum += item.getChance();
        item.setChanceInGroup(_chanceSum);
        _items.add(item);
    }

    /**
     * Возвращает список вещей
     */
    public List<RewardData> getItems() {
        return _items;
    }

    /**
     * Возвращает полностью независимую копию группы
     */
    @Override
    public RewardGroup clone() {
        RewardGroup ret = new RewardGroup(_chance);
        for (RewardData i : _items)
            ret.addData(i.clone());
        return ret;
    }

    /**
     * Функция используется в основном механизме расчета дропа, выбирает одну/несколько вещей из группы, в зависимости от рейтов
     */
    public List<RewardItem> roll(RewardType type, Player player, double mod, boolean isRaid, boolean isSiegeGuard) {
        switch (type) {
            case NOT_RATED_GROUPED:
            case NOT_RATED_NOT_GROUPED:
                return rollItems(mod, 1.0, 1.0);
            case SWEEP:
                return rollItems(mod, (Config.RATE_DROP_SPOIL + player.getVitalityBonus()), player.getRateSpoil());
            case RATED_GROUPED:
                if (_isAdena)
                    return rollAdena(mod, (Config.RATE_DROP_ADENA + player.getVitalityBonus()), player.getRateAdena());

                if (isRaid)
                    return rollItems(mod, (Config.RATE_DROP_RAIDBOSS + player.getVitalityBonus()), 1.0);

                if (isSiegeGuard)
                    return rollItems(mod, (Config.RATE_DROP_SIEGE_GUARD + player.getVitalityBonus()), 1.0);

                return rollItems(mod, (Config.RATE_DROP_ITEMS + player.getVitalityBonus()), player.getRateItems());
            default:
                return Collections.emptyList();
        }
    }

    public List<RewardItem> rollItems(double mod, double baseRate, double playerRate) {
        if (mod <= 0)
            return Collections.emptyList();

        double rate;
        if (_notRate)
            rate = Math.min(mod, 1.0);
        else
            rate = baseRate * playerRate * mod;

        double mult = Math.ceil(rate);

        List<RewardItem> ret = new ArrayList<RewardItem>((int) (mult * _items.size()));
        for (long n = 0; n < mult; n++)
            if (Rnd.get(1, RewardList.MAX_CHANCE) <= _chance * Math.min(rate - n, 1.0))
                rollFinal(_items, ret, 1., Math.max(_chanceSum, RewardList.MAX_CHANCE));
        return ret;
    }

    private List<RewardItem> rollAdena(double mod, double baseRate, double playerRate) {
        double chance = _chance;
        if (mod > 10) {
            mod *= _chance / RewardList.MAX_CHANCE;
            chance = RewardList.MAX_CHANCE;
        }

        if (mod <= 0)
            return Collections.emptyList();

        if (Rnd.get(1, RewardList.MAX_CHANCE) > chance)
            return Collections.emptyList();

        double rate = baseRate * playerRate * mod;

        List<RewardItem> ret = new ArrayList<RewardItem>(_items.size());
        rollFinal(_items, ret, rate, Math.max(_chanceSum, RewardList.MAX_CHANCE));
        for (RewardItem i : ret)
            i.isAdena = true;

        return ret;
    }

    private void rollFinal(List<RewardData> items, List<RewardItem> ret, double mult, double chanceSum) {
        // перебираем все вещи в группе и проверяем шанс
        int chance = Rnd.get(0, (int) chanceSum);
        long count;

        for (RewardData i : items) {
            if (chance < i.getChanceInGroup() && chance > i.getChanceInGroup() - i.getChance()) {
                double imult = i.notRate() ? 1.0 : mult;

                if (i.getMinDrop() >= i.getMaxDrop())
                    count = Math.round(i.getMinDrop() * imult);
                else
                    count = Rnd.get(Math.round(i.getMinDrop() * imult), Math.round(i.getMaxDrop() * imult));

                RewardItem t = null;

                for (RewardItem r : ret)
                    if (i.getItemId() == r.itemId) {
                        t = r;
                        break;
                    }

                if (t == null) {
                    ret.add(t = new RewardItem(i.getItemId()));
                    t.count = count;
                } else if (!i.notRate()) {
                    t.count = SafeMath.addAndLimit(t.count, count);
                }

                break;
            }
        }
    }
}