package handler.items;

import l2p.gameserver.listener.actor.player.OnPlayerExitListener;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.ExChangeNicknameNColor;

public class NameColor extends SimpleItemHandler {
    private static final int[] ITEM_IDS = new int[]{13021, 13307};

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }

    @Override
    protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl) {
        player.setVar("NameColorItemOID", item.getObjectId(), -1);
        player.addListener(new OnPlayerExitListener() {
            @Override
            public void onPlayerExit(Player player) {
                player.unsetVar("NameColorItemOID");
            }
        });
        player.sendPacket(new ExChangeNicknameNColor(item.getObjectId()));
        return true;
    }
}
