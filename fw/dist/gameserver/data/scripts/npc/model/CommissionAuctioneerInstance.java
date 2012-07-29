package npc.model;

import l2p.gameserver.instancemanager.commission.CommissionShopManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.templates.npc.NpcTemplate;

/**
 * @author : Ragnarok
 * @date : 10.02.12  0:07
 */
public class CommissionAuctioneerInstance extends NpcInstance {
    public CommissionAuctioneerInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        if (!canBypassCheck(player, this))
            return;
        if (command.equalsIgnoreCase("ShowCommission")) {
            CommissionShopManager.getInstance().showCommission(player);
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
