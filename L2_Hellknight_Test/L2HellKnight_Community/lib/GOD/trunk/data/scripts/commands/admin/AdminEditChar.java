package commands.admin;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import l2rt.database.mysql;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2SubClass;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.model.base.PlayerClass;
import l2rt.gameserver.model.base.Race;
import l2rt.gameserver.model.entity.Hero;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.network.serverpackets.SkillList;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.util.GArray;
import l2rt.util.Log;
import l2rt.util.Util;

@SuppressWarnings("unused")
public class AdminEditChar implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_edit_character,
		admin_character_actions,
		admin_current_player,
		admin_nokarma,
		admin_setkarma,
		admin_character_list,
		admin_show_characters,
		admin_find_character,
		admin_save_modifications,
		admin_rec,
		admin_setclass,
		admin_settitle,
		admin_setname,
		admin_setsex,
		admin_setcolor,
		admin_add_exp_sp_to_character,
		admin_add_exp_sp,
		admin_sethero,
		admin_setnoble,
		admin_trans,
		admin_setsubclass,
		admin_setfame,
		admin_setbday
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(activeChar.getPlayerAccess().CanRename)
			if(fullString.startsWith("admin_settitle"))
				try
				{
					String val = fullString.substring(15);
					L2Object target = activeChar.getTarget();
					L2Player player = null;
					if(target == null)
						return false;
					if(target.isPlayer())
					{
						player = (L2Player) target;
						player.setTitle(val);
						player.sendMessage("Your title has been changed by a GM");
						player.sendChanges();
					}
					else if(target.isNpc())
					{
						((L2NpcInstance) target).setTitle(val);
						target.decayMe();
						target.spawnMe();
					}

					return true;
				}
				catch(StringIndexOutOfBoundsException e)
				{ // Case of empty character title
					activeChar.sendMessage("You need to specify the new title.");
					return false;
				}
			else if(fullString.startsWith("admin_setname"))
				try
				{
					String val = fullString.substring(14);
					L2Object target = activeChar.getTarget();
					L2Player player;
					if(target != null && target.isPlayer())
						player = (L2Player) target;
					else
						return false;
					if(mysql.simple_get_int("count(*)", "characters", "`char_name` like '" + val + "'") > 0)
					{
						activeChar.sendMessage("Name already exist.");
						return false;
					}
					Log.add("Character " + player.getName() + " renamed to " + val + " by GM " + activeChar.getName(), "renames");
					player.reName(val);
					player.sendMessage("Your name has been changed by a GM");
					return true;
				}
				catch(StringIndexOutOfBoundsException e)
				{ // Case of empty character name
					activeChar.sendMessage("You need to specify the new name.");
					return false;
				}

		if(!activeChar.getPlayerAccess().CanEditChar && !activeChar.getPlayerAccess().CanViewChar)
			return false;

		if(fullString.equals("admin_current_player"))
			showCharacterList(activeChar, null);
		else if(fullString.startsWith("admin_character_list"))
			try
			{
				String val = fullString.substring(21);
				L2Player target = L2ObjectsStorage.getPlayer(val);
				showCharacterList(activeChar, target);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				// Case of empty character name
			}
		else if(fullString.startsWith("admin_show_characters"))
			try
			{
				String val = fullString.substring(22);
				int page = Integer.parseInt(val);
				listCharacters(activeChar, page);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				// Case of empty page
			}
		else if(fullString.startsWith("admin_find_character"))
			try
			{
				String val = fullString.substring(21);
				findCharacter(activeChar, val);
			}
			catch(StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage("You didnt enter a character name to find.");

				listCharacters(activeChar, 0);
			}
		else if(!activeChar.getPlayerAccess().CanEditChar)
			return false;
		else if(fullString.equals("admin_edit_character"))
			editCharacter(activeChar);
		else if(fullString.equals("admin_character_actions"))
			showCharacterActions(activeChar);
		else if(fullString.equals("admin_nokarma"))
			setTargetKarma(activeChar, 0);
		else if(fullString.startsWith("admin_setkarma"))
			try
			{
				String val = fullString.substring(15);
				int karma = Integer.parseInt(val);
				setTargetKarma(activeChar, karma);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please specify new karma value.");
			}
		else if(fullString.startsWith("admin_save_modifications"))
			try
			{
				String val = fullString.substring(24);
				adminModifyCharacter(activeChar, val);
			}
			catch(StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage("Error while modifying character.");
				listCharacters(activeChar, 0);
			}
		else if(fullString.equals("admin_rec"))
		{
			L2Object target = activeChar.getTarget();
			L2Player player = null;
			if(target != null && target.isPlayer())
				player = (L2Player) target;
			else
				return false;
			player.setRecomHave(player.getRecomHave() + 1);
			player.sendMessage("You have been recommended by a GM");
			player.broadcastUserInfo(true);
		}
		else if(fullString.startsWith("admin_rec"))
			try
			{
				String val = fullString.substring(10);
				int recVal = Integer.parseInt(val);
				L2Object target = activeChar.getTarget();
				L2Player player = null;
				if(target != null && target.isPlayer())
					player = (L2Player) target;
				else
					return false;
				player.setRecomHave(player.getRecomHave() + recVal);
				player.sendMessage("You have been recommended by a GM");
				player.broadcastUserInfo(true);
			}
			catch(NumberFormatException e)
			{
				activeChar.sendMessage("Command format is //rec <number>");
			}
		else if(fullString.startsWith("admin_setclass"))
		{
			try
			{
				String val = fullString.substring(15);
				int classidval = Integer.parseInt(val);
				L2Object target = activeChar.getTarget();
				L2Player player = null;
				if(target instanceof L2Player)
					player = (L2Player) target;
				else
					return false;
				boolean valid = false;
				for(ClassId classid : ClassId.values())
					if(classidval == classid.getId())
						valid = true;
				if(valid && (player.getClassId().getId() != classidval))
				{
					player.setClassId(classidval, true);
					if(!player.isSubClassActive())
						player.setBaseClass(classidval);
					String newclass = player.getTemplate().className;
					/*player.store();*/
					player.sendMessage("A GM changed your class to " + newclass);
					player.broadcastUserInfo(true);
					activeChar.sendMessage(player.getName() + " is a " + newclass);
				}
				activeChar.sendMessage("Usage: //setclass <valid_new_classid>");
			}
			catch(StringIndexOutOfBoundsException e)
			{
				AdminHelpPage.showHelpPage(activeChar, "charclasses.htm");
			}
		}
		else if(fullString.startsWith("admin_sethero"))
		{
			// Статус меняется только на текущую логон сессию
			L2Object target = activeChar.getTarget();
			L2Player player;
			if(wordList.length > 1 && wordList[1] != null)
			{
				player = L2ObjectsStorage.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
					return false;
				}
			}
			else if(target != null && target.isPlayer())
				player = (L2Player) target;
			else
			{
				activeChar.sendMessage("You must specify the name or target character.");
				return false;
			}

			if(player.isHero())
			{
				player.setHero(false);
				player.updatePledgeClass();
				Hero.removeSkills(player);
			}
			else
			{
				player.setHero(true);
				player.updatePledgeClass();
				Hero.addSkills(player);
			}

			player.sendPacket(new SkillList(player));

			if(player.isHero())
			{
				player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
				Announcements.getInstance().announceToAll(player.getName() + " has become a hero.");
			}
			player.sendMessage("Admin changed your hero status.");
			player.broadcastUserInfo(true);
		}
		else if(fullString.startsWith("admin_setnoble"))
		{
			// Статус сохраняется в базе
			L2Object target = activeChar.getTarget();
			L2Player player;
			if(wordList.length > 1 && wordList[1] != null)
			{
				player = L2ObjectsStorage.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
					return false;
				}
			}
			else if(target != null && target.isPlayer())
				player = (L2Player) target;
			else
			{
				activeChar.sendMessage("You must specify the name or target character.");
				return false;
			}

			if(player.isNoble())
			{
				Olympiad.removeNoble(player);
				player.setNoble(false);
				player.sendMessage("Admin changed your noble status, now you are not nobless.");
			}
			else
			{
				Olympiad.addNoble(player);
				player.setNoble(true);
				player.broadcastPacket(new SocialAction(player.getObjectId(), SocialAction.VICTORY));
				player.sendMessage("Admin changed your noble status, now you nobless.");
			}

			player.updatePledgeClass();
			player.updateNobleSkills();
			player.sendPacket(new SkillList(player));
			player.broadcastUserInfo(true);
		}
		else if(fullString.startsWith("admin_setsex"))
		{
			L2Object target = activeChar.getTarget();
			L2Player player = null;
			if(target != null && target.isPlayer())
				player = (L2Player) target;
			else
				return false;
			player.changeSex();
			player.sendMessage("Your gender has been changed by a GM");
			player.broadcastUserInfo(true);
		}
		else if(fullString.startsWith("admin_setcolor"))
			try
			{
				String val = fullString.substring(15);
				L2Object target = activeChar.getTarget();
				L2Player player = null;
				if(target != null && target.isPlayer())
					player = (L2Player) target;
				else
					return false;
				player.setNameColor(Integer.decode("0x" + val));
				player.sendMessage("Your name color has been changed by a GM");
				player.broadcastUserInfo(true);
			}
			catch(StringIndexOutOfBoundsException e)
			{ // Case of empty color
				activeChar.sendMessage("You need to specify the new color.");
			}
		else if(fullString.startsWith("admin_add_exp_sp_to_character"))
			addExpSp(activeChar);
		else if(fullString.startsWith("admin_add_exp_sp"))
			try
			{
				final String val = fullString.substring(16);
				adminAddExpSp(activeChar, val);
			}
			catch(final StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage("Error while adding Exp-Sp.");
			}
		else if(fullString.startsWith("admin_trans"))
		{
			StringTokenizer st = new StringTokenizer(fullString);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				int transformId = 0;
				try
				{
					transformId = Integer.parseInt(st.nextToken());
				}
				catch(Exception e)
				{
					activeChar.sendMessage("Specify a valid integer value.");
					return false;
				}
				if(transformId != 0 && activeChar.getTransformation() != 0)
				{
					activeChar.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
					return false;
				}
				activeChar.setTransformation(transformId);
				activeChar.sendMessage("Transforming...");
			}
			else
				activeChar.sendMessage("Usage: //trans <ID>");
		}
		else if(fullString.startsWith("admin_setsubclass"))
		{
			final L2Object target = activeChar.getTarget();
			if(target == null || !target.isPlayer())
			{
				activeChar.sendPacket(Msg.SELECT_TARGET);
				return false;
			}
			final L2Player player = (L2Player) target;

			StringTokenizer st = new StringTokenizer(fullString);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				short classId = Short.parseShort(st.nextToken());
				if(!player.addSubClass(classId, true))
				{
					activeChar.sendMessage(new CustomMessage("l2rt.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", activeChar));
					return false;
				}
				player.sendPacket(Msg.CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS); // Transfer to new class.
			}
			else
				setSubclass(activeChar, player);
		}
		else if(fullString.startsWith("admin_setfame"))
			try
			{
				String val = fullString.substring(14);
				int fame = Integer.parseInt(val);
				setTargetFame(activeChar, fame);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please specify new fame value.");
			}
		else if(fullString.startsWith("admin_setbday"))
		{
			String msgUsage = "Usage: //setbday YYYY-MM-DD";
			String date = fullString.substring(14);
			if(date.length() != 10 || !Util.isMatchingRegexp(date, "[0-9]{4}-[0-9]{2}-[0-9]{2}"))
			{
				activeChar.sendMessage(msgUsage);
				return false;
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			try
			{
				dateFormat.parse(date);
			}
			catch(ParseException e)
			{
				activeChar.sendMessage(msgUsage);
			}

			if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage("Please select a character.");
				return false;
			}

			if(!mysql.set("update characters set createtime = UNIX_TIMESTAMP('" + date + "') where obj_Id = " + activeChar.getTarget().getObjectId()))
			{
				activeChar.sendMessage(msgUsage);
				return false;
			}

			activeChar.sendMessage("New Birthday for " + activeChar.getTarget().getName() + ": " + date);
			activeChar.getTarget().getPlayer().sendMessage("Admin changed your birthday to: " + date);
		}
		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void listCharacters(L2Player activeChar, int page)
	{
		GArray<L2Player> players = L2ObjectsStorage.getAllPlayers();

		int MaxCharactersPerPage = 20;
		int MaxPages = players.size() / MaxCharactersPerPage;

		if(players.size() > MaxCharactersPerPage * MaxPages)
			MaxPages++;

		// Check if number of users changed
		if(page > MaxPages)
			page = MaxPages;

		int CharactersStart = MaxCharactersPerPage * page;
		int CharactersEnd = players.size();
		if(CharactersEnd - CharactersStart > MaxCharactersPerPage)
			CharactersEnd = CharactersStart + MaxCharactersPerPage;

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=270>You can find a character by writing his name and</td></tr>");
		replyMSG.append("<tr><td width=270>clicking Find bellow.<br></td></tr>");
		replyMSG.append("<tr><td width=270>Note: Names should be written case sensitive.</td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		replyMSG.append("</td></tr></table></center><br><br>");

		for(int x = 0; x < MaxPages; x++)
		{
			int pagenr = x + 1;
			replyMSG.append("<center><a action=\"bypass -h admin_show_characters " + x + "\">Page " + pagenr + "</a></center>");
		}
		replyMSG.append("<br>");

		// List Players in a Table
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=80>Name:</td><td width=110>Class:</td><td width=40>Level:</td></tr>");
		for(int i = CharactersStart; i < CharactersEnd; i++)
		{
			L2Player p = players.get(i);
			replyMSG.append("<tr><td width=80>" + "<a action=\"bypass -h admin_character_list " + p.getName() + "\">" + p.getName() + "</a></td><td width=110>" + p.getTemplate().className + "</td><td width=40>" + p.getLevel() + "</td></tr>");
		}
		replyMSG.append("</table>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	public static void showCharacterList(L2Player activeChar, L2Player player)
	{
		if(player == null)
		{
			L2Object target = activeChar.getTarget();
			if(target != null && target.isPlayer())
				player = (L2Player) target;
			else
				return;
		}
		else
			activeChar.setTarget(player);

		String clanName = "No Clan";
		if(player.getClan() != null)
			clanName = player.getClan().getName() + "/" + player.getClan().getLevel();

		NumberFormat df = NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.setMaximumFractionDigits(4);
		df.setMinimumFractionDigits(1);

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table><br>");

		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=100>Account/IP:</td><td>" + player.getAccountName() + "/" + player.getIP() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Name/Level:</td><td>" + player.getName() + "/" + player.getLevel() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Class/Id:</td><td>" + player.getTemplate().className + "/" + player.getClassId().getId() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Clan/Level:</td><td>" + clanName + "</td></tr>");
		replyMSG.append("<tr><td width=100>Exp/Sp:</td><td>" + player.getExp() + "/" + player.getSp() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Cur/Max Hp:</td><td>" + (int) player.getCurrentHp() + "/" + player.getMaxHp() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Cur/Max Mp:</td><td>" + (int) player.getCurrentMp() + "/" + player.getMaxMp() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Cur/Max Load:</td><td>" + player.getCurrentLoad() + "/" + player.getMaxLoad() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Patk/Matk:</td><td>" + player.getPAtk(null) + "/" + player.getMAtk(null, null) + "</td></tr>");
		replyMSG.append("<tr><td width=100>Pdef/Mdef:</td><td>" + player.getPDef(null) + "/" + player.getMDef(null, null) + "</td></tr>");
		replyMSG.append("<tr><td width=100>PAtkSpd/MAtkSpd:</td><td>" + player.getPAtkSpd() + "/" + player.getMAtkSpd() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Acc/Evas:</td><td>" + player.getAccuracy() + "/" + player.getEvasionRate(null) + "</td></tr>");
		replyMSG.append("<tr><td width=100>Crit/MCrit:</td><td>" + player.getCriticalHit(null, null) + "/" + df.format(player.getMagicCriticalRate(null, null)) + "%</td></tr>");
		replyMSG.append("<tr><td width=100>Walk/Run:</td><td>" + player.getWalkSpeed() + "/" + player.getRunSpeed() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Karma/Fame:</td><td>" + player.getKarma() + "/" + player.getFame() + "</td></tr>");
		replyMSG.append("<tr><td width=100>PvP/PK:</td><td>" + player.getPvpKills() + "/" + player.getPkKills() + "</td></tr>");
		replyMSG.append("<tr><td width=100>Coordinates:</td><td>" + player.getX() + "," + player.getY() + "," + player.getZ() + "</td></tr>");
		replyMSG.append("</table><br>");

		replyMSG.append("<table><tr>");
		replyMSG.append("<td><button value=\"Go To\" action=\"bypass -h admin_goto_char_menu " + player.getName() + "\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Recall\" action=\"bypass -h admin_recall_char_menu " + player.getName() + "\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
	    replyMSG.append("<td><button value=\"Set Noble\" action=\"bypass -h admin_setnoble\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr><tr>");
		replyMSG.append("<td><button value=\"Skills\" action=\"bypass -h admin_show_skills\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Effects\" action=\"bypass -h admin_show_effects\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Actions\" action=\"bypass -h admin_character_actions\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr><tr>");
		replyMSG.append("<td><button value=\"Stats\" action=\"bypass -h admin_edit_character\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Exp & Sp\" action=\"bypass -h admin_add_exp_sp_to_character\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Class\" action=\"bypass -h admin_setclass\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void setTargetKarma(L2Player activeChar, int newKarma)
	{
		L2Object target = activeChar.getTarget();
		if(target == null)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}

		L2Player player;
		if(target.isPlayer())
			player = (L2Player) target;
		else
			return;

		if(newKarma >= 0)
		{
			int oldKarma = player.getKarma();
			player.setKarma(newKarma);

			player.sendMessage("Admin has changed your karma from " + oldKarma + " to " + newKarma + ".");
			activeChar.sendMessage("Successfully Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
		}
		else
			activeChar.sendMessage("You must enter a value for karma greater than or equal to 0.");
	}

	private void setTargetFame(L2Player activeChar, int newFame)
	{
		L2Object target = activeChar.getTarget();
		if(target == null)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}

		L2Player player;
		if(target.isPlayer())
			player = (L2Player) target;
		else
			return;

		if(newFame >= 0)
		{
			int oldFame = player.getFame();
			player.setFame(newFame, "Admin manual");

			player.sendMessage("Admin has changed your fame from " + oldFame + " to " + newFame + ".");
			activeChar.sendMessage("Successfully Changed fame for " + player.getName() + " from (" + oldFame + ") to (" + newFame + ").");
		}
		else
			activeChar.sendMessage("You must enter a value for fame greater than or equal to 0.");
	}

	private void adminModifyCharacter(L2Player activeChar, String modifications)
	{
		L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(Msg.SELECT_TARGET);
			return;
		}

		L2Player player = (L2Player) target;
		String[] strvals = modifications.split("&");
		Integer[] vals = new Integer[strvals.length];
		for(int i = 0; i < strvals.length; i++)
		{
			strvals[i] = strvals[i].trim();
			vals[i] = strvals[i].isEmpty() ? null : Integer.valueOf(strvals[i]);
		}

		if(vals[0] != null)
			player.setCurrentHp(vals[0], false);

		if(vals[1] != null)
			player.setCurrentMp(vals[1]);

		if(vals[2] != null)
			player.setKarma(vals[2]);

		if(vals[3] != null)
			player.setPvpFlag(vals[3]);

		if(vals[4] != null)
			player.setPvpKills(vals[4]);

		if(vals[5] != null)
			player.setClassId(vals[5], true);

		player.sendChanges();
		editCharacter(activeChar); // Back to start
		player.broadcastUserInfo(true);
		player.decayMe();
		player.spawnMe(activeChar.getLoc());
	}

	private void editCharacter(L2Player activeChar)
	{
		L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(Msg.SELECT_TARGET);
			return;
		}

		L2Player player = (L2Player) target;
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing character: " + player.getName() + "</center><br>");
		replyMSG.append("<table width=250>");
		replyMSG.append("<tr><td width=40></td><td width=70>Curent:</td><td width=70>Max:</td><td width=70></td></tr>");
		replyMSG.append("<tr><td width=40>HP:</td><td width=70>" + player.getCurrentHp() + "</td><td width=70>" + player.getMaxHp() + "</td><td width=70>Karma: " + player.getKarma() + "</td></tr>");
		replyMSG.append("<tr><td width=40>MP:</td><td width=70>" + player.getCurrentMp() + "</td><td width=70>" + player.getMaxMp() + "</td><td width=70>Pvp Kills: " + player.getPvpKills() + "</td></tr>");
		replyMSG.append("<tr><td width=40>Load:</td><td width=70>" + player.getCurrentLoad() + "</td><td width=70>" + player.getMaxLoad() + "</td><td width=70>Pvp Flag: " + player.getPvpFlag() + "</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<table width=270><tr><td>Class<?> Template Id: " + player.getClassId() + "/" + player.getClassId().getId() + "</td></tr></table><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td>Note: Fill all values before saving the modifications.</td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=50>Hp:</td><td><edit var=\"hp\" width=50></td><td width=50>Mp:</td><td><edit var=\"mp\" width=50></td></tr>");
		replyMSG.append("<tr><td width=50>Pvp Flag:</td><td><edit var=\"pvpflag\" width=50></td><td width=50>Karma:</td><td><edit var=\"karma\" width=50></td></tr>");
		replyMSG.append("<tr><td width=50>Class<?> Id:</td><td><edit var=\"classid\" width=50></td><td width=50>Pvp Kills:</td><td><edit var=\"pvpkills\" width=50></td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<center><button value=\"Save Changes\" action=\"bypass -h admin_save_modifications $hp & $mp & $karma & $pvpflag & $pvpkills & $classid &\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center><br>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showCharacterActions(L2Player activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2Player player;
		if(target != null && target.isPlayer())
			player = (L2Player) target;
		else
			return;

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table><br><br>");
		replyMSG.append("<center>Admin Actions for: " + player.getName() + "</center><br>");
		replyMSG.append("<center><table width=200><tr>");
		replyMSG.append("<td width=100>Argument(*):</td><td width=100><edit var=\"arg\" width=100></td>");
		replyMSG.append("</tr></table><br></center>");
		replyMSG.append("<table width=270>");

		replyMSG.append("<tr><td width=90><button value=\"Teleport\" action=\"bypass -h admin_teleportto " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=90><button value=\"Recall\" action=\"bypass -h admin_recall " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=90><button value=\"Quests\" action=\"bypass -h admin_quests " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");

		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void findCharacter(L2Player activeChar, String CharacterToFind)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		int CharactersFound = 0;

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");

		for(L2Player element : L2ObjectsStorage.getAllPlayersForIterate())
			if(element.getName().startsWith(CharacterToFind))
			{
				CharactersFound = CharactersFound + 1;
				replyMSG.append("<table width=270>");
				replyMSG.append("<tr><td width=80>Name</td><td width=110>Class</td><td width=40>Level</td></tr>");
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + element.getName() + "\">" + element.getName() + "</a></td><td width=110>" + element.getTemplate().className + "</td><td width=40>" + element.getLevel() + "</td></tr>");
				replyMSG.append("</table>");
			}

		if(CharactersFound == 0)
		{
			replyMSG.append("<table width=270>");
			replyMSG.append("<tr><td width=270>Your search did not find any characters.</td></tr>");
			replyMSG.append("<tr><td width=270>Please try again.<br></td></tr>");
			replyMSG.append("</table><br>");
			replyMSG.append("<center><table><tr><td>");
			replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			replyMSG.append("</td></tr></table></center>");
		}
		else
		{
			replyMSG.append("<center><br>Found " + CharactersFound + " character");

			if(CharactersFound == 1)
				replyMSG.append(".");
			else if(CharactersFound > 1)
				replyMSG.append("s.");
		}

		replyMSG.append("</center></body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void addExpSp(final L2Player activeChar)
	{
		final L2Object target = activeChar.getTarget();
		L2Player player;
		if(target != null && target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll))
			player = (L2Player) target;
		else
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}

		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		final StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<table width=270><tr><td>Name: " + player.getName() + "</td></tr>");
		replyMSG.append("<tr><td>Lv: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr>");
		replyMSG.append("<tr><td>Exp: " + player.getExp() + "</td></tr>");
		replyMSG.append("<tr><td>Sp: " + player.getSp() + "</td></tr></table>");
		replyMSG.append("<br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr>");
		replyMSG.append("<tr><td>ruin the game...</td></tr></table><br>");
		replyMSG.append("<table width=270><tr><td>Note: Fill all values before saving the modifications.,</td></tr>");
		replyMSG.append("<tr><td>Note: Use 0 if no changes are needed.</td></tr></table><br>");
		replyMSG.append("<center><table><tr>");
		replyMSG.append("<td>Exp: <edit var=\"exp_to_add\" width=50></td>");
		replyMSG.append("<td>Sp:  <edit var=\"sp_to_add\" width=50></td>");
		replyMSG.append("<td>&nbsp;<button value=\"Save Changes\" action=\"bypass -h admin_add_exp_sp $exp_to_add & $sp_to_add &\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table></center>");
		replyMSG.append("<center><table><tr>");
		replyMSG.append("<td>LvL: <edit var=\"lvl\" width=50></td>");
		replyMSG.append("<td>&nbsp;<button value=\"Set Level\" action=\"bypass -h admin_setlevel $lvl\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table></center>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void adminAddExpSp(final L2Player activeChar, final String ExpSp)
	{
		if(!activeChar.getPlayerAccess().CanEditCharAll)
		{
			activeChar.sendMessage("You have not enough privileges, for use this function.");
			return;
		}

		final L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(Msg.SELECT_TARGET);
			return;
		}

		L2Player player = (L2Player) target;
		String[] strvals = ExpSp.split("&");
		long[] vals = new long[strvals.length];
		for(int i = 0; i < strvals.length; i++)
		{
			strvals[i] = strvals[i].trim();
			vals[i] = strvals[i].isEmpty() ? 0 : Long.parseLong(strvals[i]);
		}

		player.addExpAndSp(vals[0], vals[1], false, false);
		player.sendMessage("Admin is adding you " + vals[0] + " exp and " + vals[1] + " SP.");
		activeChar.sendMessage("Added " + vals[0] + " exp and " + vals[1] + " SP to " + player.getName() + ".");
	}

	private void setSubclass(final L2Player activeChar, final L2Player player)
	{
		StringBuffer content = new StringBuffer("<html><body>");
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		Set<PlayerClass> subsAvailable;
		subsAvailable = getAvailableSubClasses(player);

		if(subsAvailable != null && !subsAvailable.isEmpty())
		{
			content.append("Add Subclass:<br>Which subclass do you wish to add?<br>");

			for(PlayerClass subClass : subsAvailable)
				content.append("<a action=\"bypass -h admin_setsubclass " + subClass.ordinal() + "\">" + formatClassForDisplay(subClass) + "</a><br>");
		}
		else
		{
			activeChar.sendMessage(new CustomMessage("l2rt.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime", activeChar));
			return;
		}
		content.append("</body></html>");
		html.setHtml(content.toString());
		activeChar.sendPacket(html);
	}

	private Set<PlayerClass> getAvailableSubClasses(L2Player player)
	{
		final int charClassId = player.getBaseClassId();

		PlayerClass currClass = PlayerClass.values()[charClassId];// .valueOf(charClassName);

		/**
		 * If the race of your main class is Elf or Dark Elf, you may not select
		 * each class as a subclass to the other class, and you may not select
		 * Overlord and Warsmith class as a subclass.
		 *
		 * You may not select a similar class as the subclass. The occupations
		 * classified as similar classes are as follows:
		 *
		 * Treasure Hunter, Plainswalker and Abyss Walker Hawkeye, Silver Ranger
		 * and Phantom Ranger Paladin, Dark Avenger, Temple Knight and Shillien
		 * Knight Warlocks, Elemental Summoner and Phantom Summoner Elder and
		 * Shillien Elder Swordsinger and Bladedancer Sorcerer, Spellsinger and
		 * Spellhowler
		 *
		 * Kamael могут брать только сабы Kamael
		 * Другие классы не могут брать сабы Kamael
		 *
		 */
		Set<PlayerClass> availSubs = currClass.getAvailableSubclasses();
		if(availSubs == null)
			return null;

		// Из списка сабов удаляем мейн класс игрока
		availSubs.remove(currClass);

		for(PlayerClass availSub : availSubs)
		{
			// Удаляем из списка возможных сабов, уже взятые сабы и их предков
			for(L2SubClass subClass : player.getSubClasses().values())
			{
				if(availSub.ordinal() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				// Удаляем из возможных сабов их родителей, если таковые есть у чара
				ClassId parent = ClassId.values()[availSub.ordinal()].getParent(player.getSex());
				if(parent != null && parent.getId() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				// Удаляем из возможных сабов родителей текущих сабклассов, иначе если взять саб berserker
				// и довести до 3ей профы - doombringer, игроку будет предложен berserker вновь (дежавю)
				ClassId subParent = ClassId.values()[subClass.getClassId()].getParent(player.getSex());
				if(subParent != null && subParent.getId() == availSub.ordinal())
					availSubs.remove(availSub);
			}

			// Особенности саб классов камаэль
			if(availSub.isOfRace(Race.kamael))
			{
				// Для Soulbreaker-а и SoulHound не предлагаем Soulbreaker-а другого пола
				if((currClass == PlayerClass.MaleSoulHound || currClass == PlayerClass.FemaleSoulHound || currClass == PlayerClass.FemaleSoulbreaker || currClass == PlayerClass.MaleSoulbreaker) && (availSub == PlayerClass.FemaleSoulbreaker || availSub == PlayerClass.MaleSoulbreaker))
					availSubs.remove(availSub);

				// Для Berserker(doombringer) и Arbalester(trickster) предлагаем Soulbreaker-а только своего пола
				if(currClass == PlayerClass.Berserker || currClass == PlayerClass.Doombringer || currClass == PlayerClass.Arbalester || currClass == PlayerClass.Trickster)
					if(player.getSex() == 1 && availSub == PlayerClass.MaleSoulbreaker || player.getSex() == 0 && availSub == PlayerClass.FemaleSoulbreaker)
						availSubs.remove(availSub);

				// Inspector доступен, только когда вкачаны 2 возможных первых саба камаэль(+ мейн класс):
				// doombringer(berserker), soulhound(maleSoulbreaker, femaleSoulbreaker), trickster(arbalester)
				if(availSub == PlayerClass.Inspector)
					// doombringer(berserker)
					if(!(player.getSubClasses().containsKey(131) || player.getSubClasses().containsKey(127)))
						availSubs.remove(availSub);
					// soulhound(maleSoulbreaker, femaleSoulbreaker)
					else if(!(player.getSubClasses().containsKey(132) || player.getSubClasses().containsKey(133) || player.getSubClasses().containsKey(128) || player.getSubClasses().containsKey(129)))
						availSubs.remove(availSub);
					// trickster(arbalester)
					else if(!(player.getSubClasses().containsKey(134) || player.getSubClasses().containsKey(130)))
						availSubs.remove(availSub);
			}
		}
		return availSubs;
	}

	private String formatClassForDisplay(PlayerClass className)
	{
		String classNameStr = className.toString();
		char[] charArray = classNameStr.toCharArray();

		for(int i = 1; i < charArray.length; i++)
			if(Character.isUpperCase(charArray[i]))
				classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);

		return classNameStr;
	}

	@Override
	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}