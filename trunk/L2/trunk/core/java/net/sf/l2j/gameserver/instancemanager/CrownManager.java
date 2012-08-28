package net.sf.l2j.gameserver.instancemanager;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.datatables.CrownTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;

public class CrownManager
{
	private static final Logger _log = Logger.getLogger(CrownManager.class.getName());
	private static CrownManager _instance;
	
	public static final CrownManager getInstance()
	{
		if (_instance == null)
			_instance = new CrownManager();
		return _instance;
	}
	
	public CrownManager()
	{
		_log.info("CrownManager: initialized");
	}
	
	public void checkCrowns(L2Clan clan)
	{
		if (clan == null)
			return;
		
		for (L2ClanMember member : clan.getMembers())
		{
			if (member != null && member.isOnline() && member.getPlayerInstance() != null)
			{
				checkCrowns(member.getPlayerInstance());
			}
		}
	}
	
	public void checkCrowns(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return;
		
		boolean isLeader = false;
		int crownId = -1;
		
		L2Clan activeCharClan = activeChar.getClan();
		
		if (activeCharClan != null)
		{
			Castle activeCharCastle = CastleManager.getInstance().getCastleByOwner(activeCharClan);
			
			if (activeCharCastle != null)
			{
				crownId = CrownTable.getCrownId(activeCharCastle.getCastleId());
			}
			
			if (activeCharClan.getLeader().getObjectId() == activeChar.getObjectId())
			{
				isLeader = true;
			}
		}
		
		if (crownId > 0)
		{
			if (isLeader && activeChar.getInventory().getItemByItemId(6841) == null)
			{
				activeChar.addItem("Crown", 6841, 1, activeChar, true);
				activeChar.getInventory().updateDatabase();
			}
			
			if (activeChar.getInventory().getItemByItemId(crownId) == null)
			{
				activeChar.addItem("Crown", crownId, 1, activeChar, true);
				activeChar.getInventory().updateDatabase();
			}
		}
		
		boolean alreadyFoundCirclet = false;
		boolean alreadyFoundCrown = false;
		for (L2ItemInstance item : activeChar.getInventory().getItems())
		{
			if (CrownTable.getCrownList().contains(item.getItemId()))
			{
				if (crownId > 0)
				{
					if (item.getItemId() == crownId)
					{
						if (!alreadyFoundCirclet)
						{
							alreadyFoundCirclet = true;
							continue;
						}
					}
					else if (item.getItemId() == 6841 && isLeader)
					{
						if (!alreadyFoundCrown)
						{
							alreadyFoundCrown = true;
							continue;
						}
					}
				}
				
				activeChar.destroyItem("Removing Crown", item, activeChar, true);
				activeChar.getInventory().updateDatabase();
			}
		}
	}
}