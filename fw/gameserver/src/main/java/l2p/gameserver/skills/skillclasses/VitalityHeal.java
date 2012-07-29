package l2p.gameserver.skills.skillclasses;

import l2p.gameserver.Config;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.templates.StatsSet;

import java.util.List;


public class VitalityHeal extends Skill {
    public VitalityHeal(StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(Creature activeChar, List<Creature> targets) {
        int fullPoints = Config.MAX_VITALITY;
        double percent = _power;

        for (Creature target : targets) {
            if (target.isPlayer()) {
                Player player = target.getPlayer();
                int points = (int) (fullPoints / 100 * percent);
                player.addVitality(points);
            }
            getEffects(activeChar, target, getActivateRate() > 0, false);
        }

        if (isSSPossible())
            activeChar.unChargeShots(isMagic());
    }
}