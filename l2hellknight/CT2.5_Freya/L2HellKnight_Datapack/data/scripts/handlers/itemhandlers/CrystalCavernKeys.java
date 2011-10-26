package handlers.itemhandlers;


import java.util.Iterator;
import l2.hellknight.gameserver.handler.IItemHandler;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.model.L2ItemInstance;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2DoorInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

public class CrystalCavernKeys
    implements IItemHandler
{

    public CrystalCavernKeys()
    {
    }

    public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
    {
        int itemId = item.getItemId();
        if(itemId != 9694)
            return;
        if(!(playable instanceof L2PcInstance))
            return;
        L2PcInstance activeChar = (L2PcInstance)playable;
        if(activeChar.getAbnormalEffect() > 0 || activeChar.isInCombat())
        {
            activeChar.sendMessage("You cannot use the key now.");
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        L2Object target = activeChar.getTarget();
        if(!(target instanceof L2DoorInstance))
        {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        L2DoorInstance door = (L2DoorInstance)target;
        int doorId = door.getDoorId();
        if(doorId < 0x1719162 || doorId > 0x1719164)
        {
            activeChar.sendMessage("Incorrect Door.");
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if(activeChar.getCrystalCavernsZoneDoor() + 0x1719160 != doorId)
        {
            activeChar.sendMessage("Too far.");
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if(!playable.destroyItem("Consume", item.getObjectId(), 1L, null, false))
            return;
        if(activeChar.getInstanceId() != door.getInstanceId())
        {
            @SuppressWarnings("rawtypes")
			Iterator i$ = InstanceManager.getInstance().getInstance(activeChar.getInstanceId()).getDoors().iterator();
            do
            {
                if(!i$.hasNext())
                    break;
                L2DoorInstance instanceDoor = (L2DoorInstance)i$.next();
                if(instanceDoor.getDoorId() == door.getDoorId() && !instanceDoor.getOpen())
                {
                    instanceDoor.openMe();
                    activeChar.setCrystalCavernsDoor(instanceDoor.getDoorId());
                }
            } while(true);
        } else
        if(!door.getOpen())
        {
            door.openMe();
            activeChar.setCrystalCavernsDoor(door.getDoorId());
        }
    }
}