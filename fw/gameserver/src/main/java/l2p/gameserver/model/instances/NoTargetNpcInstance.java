package l2p.gameserver.model.instances;

import l2p.gameserver.model.Player;
import l2p.gameserver.templates.npc.NpcTemplate;

public class NoTargetNpcInstance extends NpcInstance {
    public NoTargetNpcInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onAction(Player player, boolean shift) {
        player.sendActionFailed();
        return;
    }

    @Override
    public boolean isInvul() {
        return true;
    }


}