package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Effect;
import l2p.gameserver.serverpackets.ExRegenMax;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.Stats;

public class EffectHealHPCP extends Effect {
    public EffectHealHPCP(Env env, EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getEffected().isPlayer() && getCount() > 0 && getPeriod() > 0)
            getEffected().sendPacket(new ExRegenMax(calc(), (int) (getCount() * getPeriod() / 1000), Math.round(getPeriod() / 1000)));

        /**
         switch(getSkill().getId().intValue())
         {
         case 2031: // Lesser Healing Potion
         getEffected().sendPacket(new ExRegenMax(ExRegenMax.POTION_HEALING_LESSER));
         break;
         case 2032: // Healing Potion
         getEffected().sendPacket(new ExRegenMax(ExRegenMax.POTION_HEALING_MEDIUM));
         break;
         case 2037: // Greater Healing Potion
         getEffected().sendPacket(new ExRegenMax(ExRegenMax.POTION_HEALING_GREATER));
         break;
         }
         */
    }

    @Override
    public boolean onActionTime() {
        if (_effected.isDead() || _effected.isHealBlocked())
            return false;

        double newHp = calc() * _effected.calcStat(Stats.HEAL_EFFECTIVNESS, 100, _effector, getSkill()) / 100;
        double addToHp = Math.max(0, Math.min(newHp, _effected.calcStat(Stats.HP_LIMIT, null, null) * _effected.getMaxHp() / 100. - _effected.getCurrentHp()));
        _effected.sendPacket(new SystemMessage(SystemMessage.S1_HPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToHp)));
        if (addToHp > 0)
            _effected.setCurrentHp(addToHp + _effected.getCurrentHp(), false);

        else {
            double newCp = calc() * _effected.getMaxCp() / 100;
            double addToCp = Math.max(0, Math.min(newCp, _effected.calcStat(Stats.CP_LIMIT, null, null) * _effected.getMaxCp() / 100. - _effected.getCurrentCp()));
            _effected.sendPacket(new SystemMessage(SystemMessage.S1_WILL_RESTORE_S2S_CP).addNumber((long) addToCp));
            if (addToCp > 0)
                _effected.setCurrentCp(addToCp + _effected.getCurrentCp());
        }
        return true;
    }
}
