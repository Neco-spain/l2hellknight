package handler.items;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.ExChangeAttributeItemList;

public class CristallChangeAttr extends SimpleItemHandler {

    private static final int CrystalAttr_id = 33502;

    @Override
    protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl) {
        if (!player.isPlayer())
            return false;

        //отправлем персу пакет на смену аттрибута
        player.sendPacket(new ExChangeAttributeItemList(player));
        player.sendMessage("Вы заюзали кристал смены атрибута");
        return true;
    }

    @Override
    public int[] getItemIds() {
        return new int[]{CrystalAttr_id};
    }
}
