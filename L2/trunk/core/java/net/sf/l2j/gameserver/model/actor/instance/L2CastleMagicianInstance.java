package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2CastleMagicianInstance extends L2NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

	public L2CastleMagicianInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val) 
	{
		player.sendPacket(new ActionFailed());
		String filename = "data/html/castlemagician/magician-no.htm";

		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			filename = "data/html/castlemagician/magician-busy.htm";
			else if (condition == COND_OWNER)
			{
				if (val == 0)
					filename = "data/html/castlemagician/magician.htm";
				else
					filename = "data/html/castlemagician/magician-" + val + ".htm";
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", String.valueOf(getName()+" "+getTitle()));
		player.sendPacket(html);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command) 
	{
		if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException ioobe)
			{
				
			}
			catch (NumberFormatException nfe)
			{
				
			}
			showChatWindow(player, val);
			return;
		}
		else if (command.startsWith("gotoleader"))
		{
			if (player.getClan() != null)
			{
				L2PcInstance clanLeader = player.getClan().getLeader().getPlayerInstance();
				if (clanLeader == null)
					return;
				
				if (clanLeader.getFirstEffect(L2Effect.EffectType.CLAN_GATE) != null)
				{
					if (!validateGateCondition(clanLeader, player))
						return;
								
					player.teleToLocation(clanLeader.getX(), clanLeader.getY(), clanLeader.getZ(), false);
					return;
				}
				String filename = "data/html/castlemagician/magician-nogate.htm";
				showChatWindow(player, filename);
			}
			return;
		}
		else
					{
						super.onBypassFeedback(player, command);
					}
	}
	protected int validateCondition(L2PcInstance player)
	{
		if (player.isGM()) return COND_OWNER;
		if (getCastle() != null && getCastle().getCastleId() > 0)
		{
			if (player.getClan() != null)
			{
				if (getCastle().getSiege().getIsInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE;
				else if (getCastle().getOwnerId() == player.getClanId())
					return COND_OWNER;
			}
		}
		return COND_ALL_FALSE;
	}
	private static final boolean validateGateCondition(L2PcInstance clanLeader, L2PcInstance player)
	{
		if (clanLeader == null)
		{
			player.sendMessage("Couldn't teleport to clan leader. Your clan leader is offline now.");
			return false;
		}
					
		if (clanLeader.isAlikeDead())
		{
			// Need retail message if there's one.
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
			
		if (clanLeader.isInStoreMode())
		{
			// Need retail message if there's one.
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
			
		if (clanLeader.isRooted() || clanLeader.isInCombat())
		{
			// Need retail message if there's one.
			player.sendMessage("Couldn't teleport to clan leader. Your clan leader is in combat.");
			return false;
		}
			
		if (clanLeader.isInOlympiadMode())
		{
			// Need retail message if there's one.
			player.sendMessage("Couldn't teleport to clan leader. Your clan leader is in Olympiad.");
			return false;
		}
			
		if (clanLeader.isInJail())
		{
			player.sendMessage("Couldn't teleport to clan leader. Your clan leader is in Jail.");
			return false;
		}
				
		if (clanLeader.isInDuel())
		{
			player.sendMessage("Couldn't teleport to clan leader. Your clan leader is in duel.");
			return false;
		}
				
		if (clanLeader.isInParty() && clanLeader.getParty().isInDimensionalRift())
		{
			player.sendMessage("Couldn't teleport to clan leader. Your clan leader is in dimensional rift.");
			return false;
		}
			
		if (clanLeader.getClan() != null && CastleManager.getInstance().getCastleByOwner(clanLeader.getClan()) != null && CastleManager.getInstance().getCastleByOwner(clanLeader.getClan()).getSiege().getIsInProgress())
		{
			player.sendMessage("Couldn't teleport to clan leader. Your clan leader is in siege.");
			return false;
		}
			
		if (clanLeader.isFestivalParticipant())
		{
			player.sendMessage("Couldn't teleport to clan leader. Your clan leader is in a festival.");
			return false;
		}
			
		if (clanLeader.inObserverMode())
		{
			player.sendMessage("Couldn't teleport to clan leader. Your clan leader is in Observer Mode.");
			return false;
		}
			
		if (clanLeader.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			// Need retail message if there's one.
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
	
		if (player.isIn7sDungeon())
		{
			final int targetCabal = SevenSigns.getInstance().getPlayerCabal(clanLeader);
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
				{
					player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
					return false;
				}
			}
			else
			{
				if (targetCabal == SevenSigns.CABAL_NULL)
				{
					player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
					return false;
				}
			}
		}
		return true;
	}
}