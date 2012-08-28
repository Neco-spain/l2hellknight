package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class L2DoormenInstance extends L2FolkInstance
{
	private ClanHall _clanHall;
	private static int COND_ALL_FALSE = 0;
	private static int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static int COND_CASTLE_OWNER = 2;
	private static int COND_HALL_OWNER = 3;
	private static int COND_FORT_OWNER = 4;

	/**
	 * @param template
	 */
	public L2DoormenInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	public final ClanHall getClanHall()
	{
		//_log.warning(this.getName()+" searching ch");
		if(_clanHall == null)
		{
			_clanHall = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
		}
		//if (_ClanHall != null)
		//    _log.warning(this.getName()+" found ch "+_ClanHall.getName());
		return _clanHall;
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		int condition = validateCondition(player);
		if(condition <= COND_ALL_FALSE)
			return;
		if(condition == COND_BUSY_BECAUSE_OF_SIEGE)
			return;
		else if(condition == COND_CASTLE_OWNER || condition == COND_HALL_OWNER || condition == COND_FORT_OWNER)
		{
			if(command.startsWith("Chat"))
			{
				showMessageWindow(player);
				return;
			}
			else if(command.startsWith("open_doors"))
			{
				if(condition == COND_HALL_OWNER)
				{
					getClanHall().openCloseDoors(true);
					player.sendPacket(new NpcHtmlMessage(getObjectId(), "<html><body>You have <font color=\"LEVEL\">opened</font> the clan hall door.<br>Outsiders may enter the clan hall while the door is open. Please close it when you've finished your business.<br><center><button value=\"Close\" action=\"bypass -h npc_" + getObjectId() + "_close_doors\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>"));
				}
				else if(condition == COND_CASTLE_OWNER)
				{
					//DoorTable doorTable = DoorTable.getInstance();
					StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid

					while(st.hasMoreTokens())
					{
						getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
					}

					st = null;
					return;
				}
				else if(condition == COND_FORT_OWNER)
				{
					StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid/fortid

					while(st.hasMoreTokens())
					{
						getFort().openDoor(player, Integer.parseInt(st.nextToken()));
					}

					st = null;
					return;
				}

			}

			if(command.startsWith("RideWyvern"))
			{
				if(!player.isClanLeader())
				{
					player.sendMessage("Only clan leaders are allowed.");
					return;
				}
				if(player.getPet() == null)
				{
					if(player.isMounted())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
						sm.addString("You Already Have a Pet or Are Mounted.");
						player.sendPacket(sm);
						sm = null;
						return;
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
						sm.addString("Summon your Strider first.");
						player.sendPacket(sm);
						sm = null;
						return;
					}
				}
				else if(player.getPet().getNpcId() == 12526 || player.getPet().getNpcId() == 12527 || player.getPet().getNpcId() == 12528)
				{
					if(player.getInventory().getItemByItemId(1460) != null && player.getInventory().getItemByItemId(1460).getCount() >= 10)
					{
						if(player.getPet().getLevel() < 55)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
							sm.addString("Your Strider Has not reached the required level.");
							player.sendPacket(sm);
							sm = null;
							return;
						}
						else
						{
							if(!player.disarmWeapons())
								return;
							player.getPet().unSummon(player);
							player.getInventory().destroyItemByItemId("Wyvern", 1460, 10, player, player.getTarget());
							Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
							player.sendPacket(mount);
							player.broadcastPacket(mount);
							player.setMountType(mount.getMountType());
							player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
							sm.addString("The Wyvern has been summoned successfully!");
							player.sendPacket(sm);
							sm = null;
							mount = null;
							return;
						}
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
						sm.addString("You need 10 Crystals: B Grade.");
						player.sendPacket(sm);
						sm = null;
						return;
					}
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Unsummon your pet.");
					player.sendPacket(sm);
					sm = null;
					return;
				}
			}
			else if(command.startsWith("close_doors"))
			{
				if(condition == COND_HALL_OWNER)
				{
					getClanHall().openCloseDoors(false);
					player.sendPacket(new NpcHtmlMessage(getObjectId(), "<html><body>You have <font color=\"LEVEL\">closed</font> the clan hall door.<br>Good day!<br><center><button value=\"To Begining\" action=\"bypass -h npc_" + getObjectId() + "_Chat\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>"));
				}
				else if(condition == COND_CASTLE_OWNER)
				{
					//DoorTable doorTable = DoorTable.getInstance();
					StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid
					//L2Clan playersClan = player.getClan();

					while(st.hasMoreTokens())
					{
						getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
					}

					st = null;
					return;
				}
				else if(condition == COND_FORT_OWNER)
				{
					StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid/fortid

					while(st.hasMoreTokens())
					{
						getFort().closeDoor(player, Integer.parseInt(st.nextToken()));
					}

					st = null;
					return;
				}
			}
		}

		super.onBypassFeedback(player, command);
	}

	/**
	 * this is called when a player interacts with this NPC
	 * 
	 * @param player
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		if(!canTarget(player))
			return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if(this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			my = null;

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if(!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}

	public void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
		String filename = "data/html/doormen/" + getTemplate().npcId + "-no.htm";

		int condition = validateCondition(player);
		if(condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			filename = "data/html/doormen/" + getTemplate().npcId + "-busy.htm"; // Busy because of siege
		}
		else if(condition == COND_CASTLE_OWNER)
		{
			filename = "data/html/doormen/" + getTemplate().npcId + ".htm"; // Owner message window
		}
		else if(condition == COND_FORT_OWNER)
		{
			filename = "data/html/doormen/" + getTemplate().npcId + ".htm"; // Owner message window
		}

		// Prepare doormen for clan hall
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		String str;
		if(getClanHall() != null)
		{
			if(condition == COND_HALL_OWNER)
			{
				str = "<html><body>Hello!<br><font color=\"55FFFF\">" + getName() + "</font>, I am honored to serve your clan.<br>How may i assist you?<br>";
				str += "<center><br><button value=\"Open Door\" action=\"bypass -h npc_%objectId%_open_doors\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>";
				str += "<button value=\"Close Door\" action=\"bypass -h npc_%objectId%_close_doors\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>";
				if(getClanHall().getId() >= 36 && getClanHall().getId() <= 41)
				{
					str += "<button value=\"Wyvern Exchange\" action=\"bypass -h npc_%objectId%_RideWyvern\" width=85 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
				}
				else
				{
					str += "</center></body></html>";
				}
			}
			else
			{
				L2Clan owner = ClanTable.getInstance().getClan(getClanHall().getOwnerId());
				if(owner != null && owner.getLeader() != null)
				{
					str = "<html><body>Hello there!<br>This clan hall is owned by <font color=\"55FFFF\">" + owner.getLeader().getName() + " who is the Lord of the ";
					str += owner.getName() + "</font> clan.<br>";
					str += "I am sorry, but only the clan members who belong to the <font color=\"55FFFF\">" + owner.getName() + "</font> clan can enter the clan hall.</body></html>";
				}
				else
				{
					str = "<html><body>" + getName() + ":<br1>Clan hall <font color=\"LEVEL\">" + getClanHall().getName() + "</font> have no owner clan.<br>You can rent it at auctioneers..</body></html>";
				}
			}
			html.setHtml(str);
		}
		else
		{
			html.setFile(filename);
		}

		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);

		filename = null;
		html = null;
		str = null;
	}

	private int validateCondition(L2PcInstance player)
	{
		if(player.getClan() != null)
		{
			// Prepare doormen for clan hall
			if(getClanHall() != null)
			{
				if(player.getClanId() == getClanHall().getOwnerId())
					return COND_HALL_OWNER;
				else
					return COND_ALL_FALSE;
			}
			// Prepare doormen for Castle
			if(getCastle() != null && getCastle().getCastleId() > 0)
			{
				if(getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
					return COND_CASTLE_OWNER; // Owner
			}
			// Prepare doormen for Fortress
			if(getFort() != null && getFort().getFortId() > 0)
			{
				if(getFort().getOwnerId() == player.getClanId()) // Clan owns fort
					return COND_FORT_OWNER;
			}
		}

		return COND_ALL_FALSE;
	}
}
