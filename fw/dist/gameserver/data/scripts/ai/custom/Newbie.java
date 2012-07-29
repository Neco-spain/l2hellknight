package ai.custom;

import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.ExShowScreenMessage;
import l2p.gameserver.serverpackets.components.NpcString;
import services.SupportMagic;

public class Newbie extends DefaultAI {
    private long lastebuff = 0;

    public Newbie(NpcInstance actor) {
        super(actor);
        this.AI_TASK_ACTIVE_DELAY = 1000;
    }

    @Override
    protected boolean thinkActive() {
        NpcInstance actor = getActor();
        if (actor.getDistance(actor.getTarget()) < 100 && System.currentTimeMillis() - lastebuff > 30000) {
            lastebuff = System.currentTimeMillis();
            for (Player player : World.getAroundPlayers(actor, 300, 200)) {
                SupportMagic.doSupportMagic(actor, player, false);
                player.sendPacket(new ExShowScreenMessage(NpcString.NEWBIE_GUIDE_GIVE_YOU_THE_MAGIC_OF_SATTELITE_S1, 4000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, player.getName()));
            }

        }
        return true;
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }
}