package ai.residences.clanhall;

import l2p.gameserver.ai.Fighter;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.components.NpcString;

/**
 * @author VISTALL
 * @date 15:17/03.06.2011
 */
public class RainbowEnragedYeti extends Fighter {
    public RainbowEnragedYeti(NpcInstance actor) {
        super(actor);
    }

    @Override
    public void onEvtSpawn() {
        super.onEvtSpawn();

        Functions.npcShout(getActor(), NpcString.OOOH_WHO_POURED_NECTAR_ON_MY_HEAD_WHILE_I_WAS_SLEEPING);
    }
}
