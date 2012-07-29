package l2p.gameserver.stats.conditions;

import l2p.gameserver.model.Player;
import l2p.gameserver.stats.Env;
import l2p.gameserver.templates.item.ArmorTemplate.ArmorType;

public class ConditionUsingArmor extends Condition {
    private final ArmorType _armor;

    public ConditionUsingArmor(ArmorType armor) {
        _armor = armor;
    }

    @Override
    protected boolean testImpl(Env env) {
        if (env.character.isPlayer() && ((Player) env.character).isWearingArmor(_armor))
            return true;

        return false;
    }
}
