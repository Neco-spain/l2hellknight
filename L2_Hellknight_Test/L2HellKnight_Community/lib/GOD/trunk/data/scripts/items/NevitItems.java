package items;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;

/**
 * @author : Ragnarok
 * @date : 14.10.2010   18:28:32
 */
public class NevitItems implements IItemHandler, ScriptFile {
    private static final int[] itemId = {
            17094
    };

    @Override
    public void useItem(L2Playable playable, L2ItemInstance item, Boolean val) {
        if(playable == null || !playable.isPlayer())
            return;
        L2Player player = (L2Player) playable;
        player.getInventory().destroyItem(item, 1, false);
        player.setRecomHave(player.getRecomHave()+10);
        player.sendPacket(new SystemMessage(3207).addNumber(10));
        player.updateVoteInfo();
    }

    @Override
    public int[] getItemIds() {
        return itemId;
    }

    @Override
    public void onLoad() {
        ItemHandler.getInstance().registerItemHandler(this);
    }

    @Override
    public void onReload() {

    }

    @Override
    public void onShutdown() {

    }
}
