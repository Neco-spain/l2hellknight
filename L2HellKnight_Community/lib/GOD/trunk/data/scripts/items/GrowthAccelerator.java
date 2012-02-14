package items;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Rnd;
import l2rt.util.Util;

public class GrowthAccelerator
    implements IItemHandler, ScriptFile
{

    public GrowthAccelerator()
    {
    }

    public int[] getItemIds()
    {
        return (new int[] {
            14832
        });
    }

    public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
    {
        if(playable == null || !playable.isPlayer() || item == null)
            return;
        L2Player player = playable.getPlayer();
        if(player.getTarget() == null || player.getTarget() != null && !player.getTarget().isNpc())
        {
            player.sendPacket(new L2GameServerPacket[] {
                Msg.THAT_IS_THE_INCORRECT_TARGET
            });
            return;
        }
        L2NpcInstance npc = (L2NpcInstance)player.getTarget();
        if(npc == null)
        {
            player.sendPacket(new L2GameServerPacket[] {
                Msg.THAT_IS_THE_INCORRECT_TARGET
            });
            return;
        }
        int npcId = npc.getNpcId();
        if(Util.isInArray(npcId, COCOONS))
        {
            if(!player.isInRange(npc, 120L))
            {
                player.sendPacket(new L2GameServerPacket[] {
                    (new SystemMessage(113)).addItemName(Integer.valueOf(item.getItemId()))
                });
                return;
            }
            if(Rnd.chance(50))
            {
                try
                {
                    player.sendPacket(new L2GameServerPacket[] {
                        new MagicSkillUse(player, npc, 2905, 1, 4000, 0L)
                    });
                    player.getInventory().destroyItem(item, 1L, false);
                    L2Spawn sp = new L2Spawn(NpcTable.getTemplate(25667));
                    l2rt.util.Location pos = GeoEngine.findPointToStay(npc.getX(), npc.getY(), npc.getZ(), 0, 0, npc.getReflection().getGeoIndex());
                    sp.setLoc(pos);
                    npc.broadcastPacket(new L2GameServerPacket[] {
                        new SocialAction(npc.getObjectId(), 1)
                    });
                    npc.doDie(player);
                    L2NpcInstance raidboss = sp.doSpawn(true);
                    raidboss.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, Integer.valueOf(Rnd.get(1, 100)));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            } else
            {
                npc.broadcastPacket(new L2GameServerPacket[] {
                    new SocialAction(npc.getObjectId(), 1)
                });
                npc.doDie(player);
            }
        } else
        {
            player.sendPacket(new L2GameServerPacket[] {
                Msg.THAT_IS_THE_INCORRECT_TARGET
            });
        }
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

    private static final int GROWTH_ACCELERATOR = 14832;
    private static final int STAKATO_CHEIF = 25667;
    private static final int COCOONS[] = {
        18793, 18794, 18795, 18796, 18797, 18798
    };

}
