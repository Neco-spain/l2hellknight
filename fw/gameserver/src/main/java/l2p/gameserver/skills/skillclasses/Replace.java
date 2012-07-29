package l2p.gameserver.skills.skillclasses;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Summon;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.utils.Location;

import java.util.List;

public class Replace extends Skill {

    public Replace(StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(Creature activeChar, List<Creature> targets) {
        if (!(activeChar instanceof Player))
            return;
        Player activePlayer = activeChar.getPlayer();
        Summon activePet = ((Player) activeChar).getSummonList().getFirstServitor();

        if (activePet == null)
            return; // TODO: SysMessage

        Location loc_pet = activePet.getLoc();
        Location loc_cha = activePlayer.getLoc();
        activePlayer.teleToLocation(loc_pet);
        activePet.teleToLocation(loc_cha);

    }
}
