package items;

import java.util.concurrent.ScheduledFuture;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.*;

public class SoulBreakingArrow
    implements IItemHandler, ScriptFile
{
    public static class UnBlockTask
        implements Runnable
    {

        public void run()
        {
            L2NpcInstance npc = L2ObjectsStorage.getAsNpc(npcStoreId);
            if(npc == null)
            {
                return;
            } else
            {
                npc.unblock();
                return;
            }
        }

        private final long npcStoreId;

        public UnBlockTask(L2Character npc)
        {
            npcStoreId = npc.getStoredId().longValue();
        }
    }


    public SoulBreakingArrow()
    {
        _blockTask = null;
    }

    public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
    {
        if(playable == null || !playable.isPlayer())
            return;
        L2Player player = playable.getPlayer();
        if(player == null)
            return;
        if(player.getActiveWeaponInstance() != null && player.getActiveWeaponItem() != null && (player.getActiveWeaponItem().getItemType().mask() & 32L) != 0L)
        {
            if(player.isSkillDisabled(Integer.valueOf(2234)))
                return;
            L2Object target = player.getTarget();
            if(target == null || !target.isNpc())
            {
                player.sendPacket(new L2GameServerPacket[] {
                    Msg.INVALID_TARGET
                });
                return;
            }
            L2NpcInstance frintezza = (L2NpcInstance)target;
            if(!player.isInRange(frintezza, 900L))
            {
                player.sendPacket(new L2GameServerPacket[] {
                    Msg.YOUR_TARGET_IS_OUT_OF_RANGE
                });
                return;
            }
            if(frintezza.getNpcId() == 29045 && !frintezza.isBlocked())
            {
                player.broadcastPacket(new L2GameServerPacket[] {
                    new MagicSkillUse(player, frintezza, 2234, 1, 2000, 0L)
                });
                player.getInventory().destroyItem(item, 1L, true);
                player.disableSkill(2234, 3000L);
                frintezza.block();
                if(_blockTask != null)
                {
                    _blockTask.cancel(false);
                    _blockTask = null;
                }
                _blockTask = ThreadPoolManager.getInstance().scheduleAi(new UnBlockTask(frintezza), 10000L, false);
            }
        } else
        {
            player.sendPacket(new L2GameServerPacket[] {
                (new SystemMessage(113)).addItemName(Integer.valueOf(item.getItemId()))
            });
        }
    }

    public final int[] getItemIds()
    {
        return ITEM_IDS;
    }

    public void onLoad()
    {
        ItemHandler.getInstance().registerItemHandler(this);
    }

    public void onReload()
    {
    }

    public void onShutdown()
    {
    }

    private static final int ITEM_IDS[] = {
        8192
    };
    private ScheduledFuture _blockTask;

}
