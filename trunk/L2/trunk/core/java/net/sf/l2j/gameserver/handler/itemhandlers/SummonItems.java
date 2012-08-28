package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SummonItemsData;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2SummonItem;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.events.CTF;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.PetInfo;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.FloodProtector;

public class SummonItems implements IItemHandler
{
	@SuppressWarnings("unused")
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		if (!TvTEvent.onItemSummon(playable.getObjectId()))
			return;

		L2PcInstance activeChar = (L2PcInstance)playable;

		if ( !FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_ITEMPETSUMMON) ) return;

		if (activeChar._inEventCTF && CTF._started && !Config.CTF_ALLOW_SUMMON)
		{
			activeChar.sendPacket(new ActionFailed());
			return;
		}

		if(activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}

		if (activeChar.inObserverMode())
			return;

		if (activeChar.isInOlympiadMode())
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
            return;
        }

		L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());

		if ((activeChar.getPet() != null || activeChar.isMounted()) && sitem.isPetSummon())
		{
            activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
			return;
		}

		if (activeChar.isAttackingNow())
		{
            activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}

        if (activeChar.isCursedWeaponEquiped() && sitem.isPetSummon())
        {
        	activeChar.sendPacket(new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));
        	return;
        }

        int npcID = sitem.getNpcId();

        if (npcID == 0)
        	return;

		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcID);

        if (npcTemplate == null)
            return;

        switch (sitem.getType())
        {
        case 0: // static summons (like christmas tree)
            try
            {
                L2Spawn spawn = new L2Spawn(npcTemplate);

                if (spawn == null)
                	return;

                spawn.setId(IdFactory.getInstance().getNextId());
                spawn.setLocx(activeChar.getX());
                spawn.setLocy(activeChar.getY());
                spawn.setLocz(activeChar.getZ());
				L2World.getInstance().storeObject(spawn.spawnOne(true));
                activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false);
                activeChar.sendMessage("Created " + npcTemplate.name + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
            }
            catch (Exception e)
            {
                activeChar.sendMessage("Target is not ingame.");
            }

        	break;
        case 1: // pet summons
        	L2PetInstance petSummon = L2PetInstance.spawnPet(npcTemplate, activeChar, item);

    		if (petSummon == null)
    			break;

    		petSummon.setTitle(activeChar.getName());

    		if (!petSummon.isRespawned())
    		{
    			petSummon.setCurrentHp(petSummon.getMaxHp());
    			petSummon.setCurrentMp(petSummon.getMaxMp());
    			petSummon.getStat().setExp(petSummon.getExpForThisLevel());
    			petSummon.setCurrentFed(petSummon.getMaxFed());
    		}

    		petSummon.setRunning();

    		if (!petSummon.isRespawned())
    			petSummon.store();

            activeChar.setPet(petSummon);

    		activeChar.sendPacket(new MagicSkillUser(activeChar, 2046, 1, 1000, 600000));
    		activeChar.sendPacket(new SystemMessage(SystemMessageId.SUMMON_A_PET));
            L2World.getInstance().storeObject(petSummon);
    		petSummon.spawnMe(activeChar.getX()+50, activeChar.getY()+100, activeChar.getZ());
            activeChar.sendPacket(new PetInfo(petSummon));
    		petSummon.startFeed(false);
    		item.setEnchantLevel(petSummon.getLevel());

    		ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFinalizer(activeChar, petSummon), 900);

    		if (petSummon.getCurrentFed() <= 0)
    			ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFeedWait(activeChar, petSummon), 60000);
    		else
    			petSummon.startFeed(false);

        	break;
        case 2: // wyvern
        	if(!activeChar.disarmWeapons()) return;
        	Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, sitem.getNpcId());
            activeChar.sendPacket(mount);
            activeChar.broadcastPacket(mount);
            activeChar.setMountType(mount.getMountType());
            activeChar.setMountObjectID(item.getObjectId());
        }
	}

	static class PetSummonFeedWait implements Runnable
	{
		private L2PcInstance _activeChar;
		private L2PetInstance _petSummon;

		PetSummonFeedWait(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}

		public void run()
		{
			try
			{
				if (_petSummon.getCurrentFed() <= 0 )
					_petSummon.unSummon(_activeChar);
				else
					_petSummon.startFeed(false);
			}
			catch (Throwable e)
			{
			}
		}
	}

	static class PetSummonFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;
		private L2PetInstance _petSummon;

		PetSummonFinalizer(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}

		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_petSummon.setFollowStatus(true);
		        _petSummon.setShowSummonAnimation(false);
			}
			catch (Throwable e)
			{
			}
		}
	}

	public int[] getItemIds()
    {
    	return SummonItemsData.getInstance().itemIDs();
    }
}
