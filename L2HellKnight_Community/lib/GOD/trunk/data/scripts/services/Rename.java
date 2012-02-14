package services;

import java.util.Map.Entry;

import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.database.mysql;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2SubClass;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.base.PlayerClass;
import l2rt.gameserver.model.base.Race;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.ClanTable;
import l2rt.util.GArray;
import l2rt.util.Log;
import l2rt.util.Util;

public class Rename extends Functions implements ScriptFile
{
	public void rename_page()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		String append = "!Rename";
		append += "<br>";
		append += "<font color=\"LEVEL\">" + new CustomMessage("scripts.services.Rename.RenameFor", getSelf()).addString(Util.formatAdena(Config.SERVICES_CHANGE_NICK_PRICE)).addItemName(Config.SERVICES_CHANGE_NICK_ITEM) + "</font>";
		append += "<table>";
		append += "<tr><td>" + new CustomMessage("scripts.services.Rename.NewName", getSelf()) + " <edit var=\"new_name\" width=80></td></tr>";
		append += "<tr><td></td></tr>";
		append += "<tr><td><button value=\"" + new CustomMessage("scripts.services.Rename.RenameButton", getSelf()) + "\" action=\"bypass -h scripts_services.Rename:rename $new_name\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
		append += "</table>";
		show(append, player);
	}

	public void changesex_page()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(!player.isInPeaceZone())
		{
			show("You must be in peace zone to use this service.", player);
			return;
		}

		String append = "Sex changing";
		append += "<br>";
		append += "<font color=\"LEVEL\">" + new CustomMessage("scripts.services.SexChange.SexChangeFor", player).addString(Util.formatAdena(Config.SERVICES_CHANGE_SEX_PRICE)).addItemName(Config.SERVICES_CHANGE_SEX_ITEM) + "</font>";
		append += "<table>";
		append += "<tr><td><button value=\"" + new CustomMessage("scripts.services.SexChange.Button", player) + "\" action=\"bypass -h scripts_services.Rename:changesex\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
		append += "</table>";
		show(append, player);
	}

	public void separate_page()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(player.isHero())
		{
			show("Not available for heroes.", player);
			return;
		}

		if(player.getSubClasses().size() == 1)
		{
			show("You must have at least 1 subclass.", player);
			return;
		}

		if(!player.getActiveClass().isBase())
		{
			show("You must be at main class.", player);
			return;
		}

		if(player.getActiveClass().getLevel() < 75)
		{
			show("You must have at least 75 level.", player);
			return;
		}

		String append = "Subclass separation";
		append += "<br>";
		append += "<font color=\"LEVEL\">" + new CustomMessage("scripts.services.Separate.Price", player).addString(Util.formatAdena(Config.SERVICES_SEPARATE_SUB_PRICE)).addItemName(Config.SERVICES_SEPARATE_SUB_ITEM) + "</font>&nbsp;";
		append += "<edit var=\"name\" width=80 height=15 /><br>";
		append += "<table>";

		for(L2SubClass s : player.getSubClasses().values())
			if(!s.isBase() && s.getClassId() != ClassId.inspector.getId() && s.getClassId() != ClassId.judicator.getId())
				append += "<tr><td><button value=\"" + new CustomMessage("scripts.services.Separate.Button", player).addString(ClassId.values()[s.getClassId()].toString()) + "\" action=\"bypass -h scripts_services.Rename:separate " + s.getClassId() + " $name\" width=200 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";

		append += "</table>";
		show(append, player);
	}

	public void separate(String[] param)
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(player.isHero())
		{
			show("Not available for heroes.", player);
			return;
		}

		if(player.getSubClasses().size() == 1)
		{
			show("You must have at least 1 subclass.", player);
			return;
		}

		if(!player.getActiveClass().isBase())
		{
			show("You must be at main class.", player);
			return;
		}

		if(player.getActiveClass().getLevel() < 75)
		{
			show("You must have at least 75 level.", player);
			return;
		}

		if(param.length < 2)
		{
			show("You must specify target.", player);
			return;
		}

		if(getItemCount(player, Config.SERVICES_SEPARATE_SUB_ITEM) < Config.SERVICES_SEPARATE_SUB_PRICE)
		{
			if(Config.SERVICES_SEPARATE_SUB_ITEM == 57)
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			return;
		}

		int classtomove = Integer.parseInt(param[0]);
		int newcharid = 0;
		for(Entry<Integer, String> e : player.getAccountChars().entrySet())
			if(e.getValue().equalsIgnoreCase(param[1]))
				newcharid = e.getKey();

		if(newcharid == 0)
		{
			show("Target not exists.", player);
			return;
		}

		if(mysql.simple_get_int("level", "character_subclasses", "char_obj_id=" + newcharid + " AND level > 1") > 1)
		{
			show("Target must have level 1.", player);
			return;
		}

		mysql.set("DELETE FROM character_subclasses WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_skills WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_skills_save WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_effects_save WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_hennas WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_shortcuts WHERE char_obj_id=" + newcharid);
		mysql.set("DELETE FROM character_variables WHERE obj_id=" + newcharid);

		mysql.set("UPDATE character_subclasses SET char_obj_id=" + newcharid + ", isBase=1, skills='' WHERE char_obj_id=" + player.getObjectId() + " AND class_id=" + classtomove);
		mysql.set("UPDATE character_skills SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
		mysql.set("UPDATE character_skills_save SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
		mysql.set("UPDATE character_effects_save SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
		mysql.set("UPDATE character_hennas SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);
		mysql.set("UPDATE character_shortcuts SET char_obj_id=" + newcharid + " WHERE char_obj_id=" + player.getObjectId() + " AND class_index=" + classtomove);

		mysql.set("UPDATE character_variables SET obj_id=" + newcharid + " WHERE obj_id=" + player.getObjectId() + " AND name like 'TransferSkills%'");

		player.modifySubClass(classtomove, 0);

		removeItem(player, Config.SERVICES_CHANGE_BASE_ITEM, Config.SERVICES_CHANGE_BASE_PRICE);
		player.logout(false, false, false, true);
		//Log.add("Character " + player + " base changed to " + target, "services");
	}

	public void changebase_page()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(!player.isInPeaceZone())
		{
			show("You must be in peace zone to use this service.", player);
			return;
		}

		if(player.isHero())
		{
			sendMessage("Not available for heroes.", player);
			return;
		}

		String append = "Base class changing";
		append += "<br>";
		append += "<font color=\"LEVEL\">" + new CustomMessage("scripts.services.BaseChange.Price", player).addString(Util.formatAdena(Config.SERVICES_CHANGE_BASE_PRICE)).addItemName(Config.SERVICES_CHANGE_BASE_ITEM) + "</font>";
		append += "<table>";

		GArray<L2SubClass> possible = new GArray<L2SubClass>();
		if(player.getActiveClass().isBase())
		{
			possible.addAll(player.getSubClasses().values()); 			
			possible.remove(player.getSubClasses().get(player.getBaseClassId()));

			for(L2SubClass s : player.getSubClasses().values())
				for(L2SubClass s2 : player.getSubClasses().values())
					if(s != s2 && !PlayerClass.areClassesComportable(PlayerClass.values()[s.getClassId()], PlayerClass.values()[s2.getClassId()]) || s2.getLevel() < 75)
						possible.remove(s2);
		}

		if(possible.isEmpty())
			append += "<tr><td width=300>" + new CustomMessage("scripts.services.BaseChange.NotPossible", player) + "</td></tr>";
		else
			for(L2SubClass s : possible)
				append += "<tr><td><button value=\"" + new CustomMessage("scripts.services.BaseChange.Button", player).addString(ClassId.values()[s.getClassId()].toString()) + "\" action=\"bypass -h scripts_services.Rename:changebase " + s.getClassId() + "\" width=200 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
		append += "</table>";
		show(append, player);
	}

	public void changebase(String[] param)
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(!player.isInPeaceZone())
		{
			show("You must be in peace zone to use this service.", player);
			return;
		}

		if(!player.getActiveClass().isBase())
		{
			show("You must be on your base class to use this service.", player);
			return;
		}

		if(player.isHero())
		{
			show("Not available for heroes.", player);
			return;
		}

		if(getItemCount(player, Config.SERVICES_CHANGE_BASE_ITEM) < Config.SERVICES_CHANGE_BASE_PRICE)
		{
			if(Config.SERVICES_CHANGE_BASE_ITEM == 57)
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			return;
		}

		player.getActiveClass().setBase(false);
		if(player.getActiveClass().getLevel() > Experience.getMaxSubLevel())
			player.setLevel(Experience.getMaxSubLevel());
		player.checkSkills(0);

		int target = Integer.parseInt(param[0]);
		player.getSubClasses().get(target).setBase(true);
		player.getSubClasses().get(target).setSkills("");
		player.setBaseClass(target);

		player.setHairColor(0);
		player.setHairStyle(0);
		player.setFace(0);
		Olympiad.unRegisterNoble(player);
		removeItem(player, Config.SERVICES_CHANGE_BASE_ITEM, Config.SERVICES_CHANGE_BASE_PRICE);
		player.logout(false, false, false, true);
		//Log.add("Character " + player + " base changed to " + target, "services");
	}

	public void rename(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(args.length != 1)
		{
			show(new CustomMessage("scripts.services.Rename.incorrectinput", player), player);
			return;
		}

		if(player.getSiegeState() != 0)
		{
			show(new CustomMessage("scripts.services.Rename.SiegeNow", player), player);
			return;
		}

		String name = args[0];
		if(!Util.isMatchingRegexp(name, Config.CNAME_TEMPLATE))
		{
			show(new CustomMessage("scripts.services.Rename.incorrectinput", player), player);
			return;
		}

		if(getItemCount(player, Config.SERVICES_CHANGE_NICK_ITEM) < Config.SERVICES_CHANGE_NICK_PRICE)
		{
			if(Config.SERVICES_CHANGE_NICK_ITEM == 57)
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			return;
		}

		if(!Util.isCharNameAvail(name))
		{
			show(new CustomMessage("scripts.services.Rename.Thisnamealreadyexists", player), player);
			return;
		}

		removeItem(player, Config.SERVICES_CHANGE_NICK_ITEM, Config.SERVICES_CHANGE_NICK_PRICE);

		String oldName = player.getName();
		player.reName(name, true);
		Log.add("Character " + oldName + " renamed to " + name, "renames");
		show(new CustomMessage("scripts.services.Rename.changedname", player).addString(oldName).addString(name), player);
	}

	public void changesex()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(player.getRace() == Race.kamael)
		{
			show("Not available for Kamael.", player);
			return;
		}

		if(!player.isInPeaceZone())
		{
			show("You must be in peace zone to use this service.", player);
			return;
		}

		if(getItemCount(player, Config.SERVICES_CHANGE_SEX_ITEM) < Config.SERVICES_CHANGE_SEX_PRICE)
		{
			if(Config.SERVICES_CHANGE_SEX_ITEM == 57)
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement offline = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("UPDATE characters SET sex = ? WHERE obj_Id = ?");
			offline.setInt(1, player.getSex() == 1 ? 0 : 1);
			offline.setInt(2, player.getObjectId());
			offline.executeUpdate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			show(new CustomMessage("common.Error", player), player);
			return;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, offline);
		}

		player.setHairColor(0);
		player.setHairStyle(0);
		player.setFace(0);
		removeItem(player, Config.SERVICES_CHANGE_SEX_ITEM, Config.SERVICES_CHANGE_SEX_PRICE);
		player.logout(false, false, false, true);
		Log.add("Character " + player + " sex changed to " + (player.getSex() == 1 ? "male" : "female"), "renames");
	}

	public void onLoad()
	{
		System.out.println("Loaded Service: Nick change");
	}

	public void rename_clan_page()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(player.getClan() == null || !player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_A_CLAN_LEADER).addName(player));
			return;
		}

		String append = "!Rename clan";
		append += "<br>";
		append += "<font color=\"LEVEL\">" + new CustomMessage("scripts.services.Rename.RenameFor", getSelf()).addString(Util.formatAdena(Config.SERVICES_CHANGE_CLAN_NAME_PRICE)).addItemName(Config.SERVICES_CHANGE_CLAN_NAME_ITEM) + "</font>";
		append += "<table>";
		append += "<tr><td>" + new CustomMessage("scripts.services.Rename.NewName", getSelf()) + ": <edit var=\"new_name\" width=80></td></tr>";
		append += "<tr><td></td></tr>";
		append += "<tr><td><button value=\"" + new CustomMessage("scripts.services.Rename.RenameButton", getSelf()) + "\" action=\"bypass -h scripts_services.Rename:rename_clan $new_name\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
		append += "</table>";
		show(append, player);
	}

	public void rename_clan(String[] param)
	{
		L2Player player = (L2Player) getSelf();
		if(player == null || param == null || param.length == 0)
			return;

		if(player.getClan() == null || !player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_A_CLAN_LEADER).addName(player));
			return;
		}

		if(player.getSiegeState() != 0)
		{
			show(new CustomMessage("scripts.services.Rename.SiegeNow", player), player);
			return;
		}

		if(!Util.isMatchingRegexp(param[0], Config.CLAN_NAME_TEMPLATE))
		{
			player.sendPacket(Msg.CLAN_NAME_IS_INCORRECT);
			return;
		}
		if(ClanTable.getInstance().getClanByName(param[0]) != null)
		{
			player.sendPacket(Msg.THIS_NAME_ALREADY_EXISTS);
			return;
		}

		if(getItemCount(player, Config.SERVICES_CHANGE_CLAN_NAME_ITEM) < Config.SERVICES_CHANGE_CLAN_NAME_PRICE)
		{
			if(Config.SERVICES_CHANGE_CLAN_NAME_ITEM == 57)
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			return;
		}

		show(new CustomMessage("scripts.services.Rename.changedname", player).addString(player.getClan().getName()).addString(param[0]), player);
		player.getClan().setName(param[0]);
		player.getClan().updateClanInDB();
		removeItem(player, Config.SERVICES_CHANGE_CLAN_NAME_ITEM, Config.SERVICES_CHANGE_CLAN_NAME_PRICE);
		player.getClan().broadcastClanStatus(true, true, false);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}