package items;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2DoorInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Rnd;

public abstract class DoorKey
    implements IItemHandler, ScriptFile
{

    public DoorKey()
    {
    }

    public void useItem(L2Playable playable, L2ItemInstance item)
    {
        int itemId = item.getItemId();
        if(playable == null || !playable.isPlayer())
            return;
        L2Player activeChar = (L2Player)playable;
        l2rt.gameserver.model.L2Object target = activeChar.getTarget();
        if(itemId == 8060)
        {
            L2Skill skill = SkillTable.getInstance().getInfo(2260, 1);
            if(skill != null)
                activeChar.broadcastPacket(new L2GameServerPacket[] {
                    new MagicSkillUse(activeChar, activeChar, skill.getId(), 1, 0, 0L)
                });
            return;
        }
        if(!(target instanceof L2DoorInstance))
        {
            activeChar.sendPacket(new L2GameServerPacket[] {
                THAT_IS_THE_INCORRECT_TARGET
            });
            activeChar.sendActionFailed();
            return;
        }
        L2DoorInstance door = (L2DoorInstance)target;
        if(!activeChar.isInRange(door, 100L))
        {
            activeChar.sendMessage("Too far.");
            activeChar.sendActionFailed();
            return;
        }
        if(activeChar.getAbnormalEffect() > 0 || activeChar.isInCombat())
        {
            activeChar.sendMessage("You are currently engaged in combat.");
            activeChar.sendActionFailed();
            return;
        }
        int openChance = 35;
        switch(itemId)
        {
        default:
            break;

        case 8273: 
            if(door.getDoorName().startsWith("Rune_Anteroom"))
            {
                if(activeChar.getInventory().getItemByItemId(itemId) != null)
                    activeChar.getInventory().destroyItem(item.getObjectId(), 1L, true);
                else
                    return;
                if(openChance > 0 && Rnd.get(100) < openChance)
                {
                    activeChar.sendMessage("You opened Anterooms Door.");
                    door.openMe();
                    door.onOpen();
                    activeChar.broadcastPacket(new L2GameServerPacket[] {
                        new SocialAction(activeChar.getObjectId(), 3)
                    });
                } else
                {
                    activeChar.sendMessage("You failed to open Anterooms Door.");
                    activeChar.broadcastPacket(new L2GameServerPacket[] {
                        new SocialAction(activeChar.getObjectId(), 13)
                    });
                    PlaySound playSound = new PlaySound("interfacesound.system_close_01");
                    activeChar.sendPacket(new L2GameServerPacket[] {
                        playSound
                    });
                }
            } else
            {
                activeChar.sendMessage("Incorrect Door.");
            }
            break;

        case 8274: 
            if(door.getDoorName().startsWith("Rune_Altar_Entrance"))
            {
                if(activeChar.getInventory().getItemByItemId(itemId) != null)
                    activeChar.getInventory().destroyItem(item.getObjectId(), 1L, true);
                else
                    return;
                if(openChance > 0 && Rnd.get(100) < openChance)
                {
                    activeChar.sendMessage("You opened Altar Entrance.");
                    door.openMe();
                    door.onOpen();
                    activeChar.broadcastPacket(new L2GameServerPacket[] {
                        new SocialAction(activeChar.getObjectId(), 3)
                    });
                } else
                {
                    activeChar.sendMessage("You failed to open Altar Entrance.");
                    activeChar.broadcastPacket(new L2GameServerPacket[] {
                        new SocialAction(activeChar.getObjectId(), 13)
                    });
                    PlaySound playSound = new PlaySound("interfacesound.system_close_01");
                    activeChar.sendPacket(new L2GameServerPacket[] {
                        playSound
                    });
                }
            } else
            {
                activeChar.sendMessage("Incorrect Door.");
            }
            break;

        case 8275: 
            if(door.getDoorName().startsWith("Rune_Door_of_Darkness"))
            {
                if(activeChar.getInventory().getItemByItemId(itemId) != null)
                    activeChar.getInventory().destroyItem(item.getObjectId(), 1L, true);
                else
                    return;
                if(openChance > 0 && Rnd.get(100) < openChance)
                {
                    activeChar.sendMessage("You opened Door of Darkness.");
                    door.openMe();
                    door.onOpen();
                    activeChar.broadcastPacket(new L2GameServerPacket[] {
                        new SocialAction(activeChar.getObjectId(), 3)
                    });
                } else
                {
                    activeChar.sendMessage("You failed to open Door of Darkness.");
                    activeChar.broadcastPacket(new L2GameServerPacket[] {
                        new SocialAction(activeChar.getObjectId(), 13)
                    });
                    PlaySound playSound = new PlaySound("interfacesound.system_close_01");
                    activeChar.sendPacket(new L2GameServerPacket[] {
                        playSound
                    });
                }
            } else
            {
                activeChar.sendMessage("Incorrect Door.");
            }
            break;

        case 8056: 
            if(door.getDoorId() != 0x1613db2 && door.getDoorId() != 0x1613db3 && door.getDoorId() != 0x1613db4 || door.isOpen())
            {
                activeChar.sendPacket(new L2GameServerPacket[] {
                    THAT_IS_THE_INCORRECT_TARGET
                });
                activeChar.sendActionFailed();
                return;
            }
            if(activeChar.getInventory().getItemByItemId(itemId) != null)
                activeChar.getInventory().destroyItem(item.getObjectId(), 1L, true);
            else
                return;
            door.openMe();
            door.onOpen();
            break;

        case 9698: 
            if(door.getDoorId() != 0x1719174 || door.isOpen())
            {
                activeChar.sendPacket(new L2GameServerPacket[] {
                    THAT_IS_THE_INCORRECT_TARGET
                });
                activeChar.sendActionFailed();
                return;
            }
            if(activeChar.getInventory().getItemByItemId(itemId) != null)
                activeChar.getInventory().destroyItem(item.getObjectId(), 1L, true);
            else
                return;
            door.openMe();
            break;

        case 9699: 
            if(door.getDoorId() != 0x1719176 || door.isOpen())
            {
                activeChar.sendPacket(new L2GameServerPacket[] {
                    THAT_IS_THE_INCORRECT_TARGET
                });
                activeChar.sendActionFailed();
                return;
            }
            if(activeChar.getInventory().getItemByItemId(itemId) != null)
                activeChar.getInventory().destroyItem(item.getObjectId(), 1L, true);
            else
                return;
            door.openMe();
            break;

        case 9694: 
            if(door.getDoorId() != 0x1719161 && door.getDoorId() != 0x1719162 && door.getDoorId() != 0x1719163 && door.getDoorId() != 0x1719164 || door.isOpen())
            {
                activeChar.sendPacket(new L2GameServerPacket[] {
                    THAT_IS_THE_INCORRECT_TARGET
                });
                activeChar.sendActionFailed();
                return;
            }
            if(activeChar.getInventory().getItemByItemId(itemId) != null)
                activeChar.getInventory().destroyItem(item.getObjectId(), 1L, true);
            else
                return;
            door.openMe();
            break;
        }
    }

    public int[] getItemIds()
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
        8273, 8274, 8275, 8056, 8060, 9698, 9699, 9694
    };
    public static final int INTERACTION_DISTANCE = 100;
    static final SystemMessage THAT_IS_THE_INCORRECT_TARGET = new SystemMessage(144);

}
