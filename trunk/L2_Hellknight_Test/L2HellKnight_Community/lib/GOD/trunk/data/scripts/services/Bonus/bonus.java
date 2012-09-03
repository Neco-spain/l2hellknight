package services.Bonus;

import java.sql.ResultSet;
import java.util.Calendar;

import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.util.Log;

public class bonus extends Functions implements ScriptFile
{
	private boolean HaveBonus(String type)
	{
		L2Player player = (L2Player) getSelf();
		ResultSet rs = null;
		ThreadConnection con = null;
		FiltredPreparedStatement offline = null;
		Calendar end_;
		end_ = Calendar.getInstance();
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT bonus_expire_time FROM bonus WHERE obj_id LIKE ? AND bonus_name = ?");
			offline.setInt(1, player.getObjectId());
			offline.setString(2, type);
			rs = offline.executeQuery();
			if(rs.next())
			{
				end_.setTimeInMillis(rs.getLong(1) * 1000L);
				show(new CustomMessage("scripts.services.Bonus.bonus.alreadychar", player).addString(String.valueOf(end_.get(Calendar.DAY_OF_MONTH)) + "." + String.valueOf(end_.get(Calendar.MONTH) + 1) + "." + String.valueOf(end_.get(Calendar.YEAR))), player);
				return true;
			}
			return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			show(new CustomMessage("common.Error", player), player);
			return false;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, offline, rs);
		}
	}

	public String DialogAppend_31757(Integer val)
	{
		if(val == 0 && Config.SERVICES_RATE_SPECIAL_ENABLED)
		{
			String append = "";
			append += "[scripts_services.Bonus.bonus:buy" + " " + "1|";
			append += new CustomMessage("scripts.services.Bonus.bonus.buybonuscharexp", getSelf()).addNumber(Config.SERVICES_RATE_SPECIAL_DAYS).addNumber(Config.SERVICES_RATE_SPECIAL_ITEM_COUNT).addItemName(Config.SERVICES_RATE_SPECIAL_ITEM_ID).addString(String.valueOf(Config.SERVICES_RATE_SPECIAL_RATE));
			append += "]<br>";
			append += "[scripts_services.Bonus.bonus:buy" + " " + "2|";
			append += new CustomMessage("scripts.services.Bonus.bonus.buybonuscharsp", getSelf()).addNumber(Config.SERVICES_RATE_SPECIAL_DAYS).addNumber(Config.SERVICES_RATE_SPECIAL_ITEM_COUNT).addItemName(Config.SERVICES_RATE_SPECIAL_ITEM_ID).addString(String.valueOf(Config.SERVICES_RATE_SPECIAL_RATE));
			append += "]<br>";
			append += "[scripts_services.Bonus.bonus:buy" + " " + "3|";
			append += new CustomMessage("scripts.services.Bonus.bonus.buybonuschardrop", getSelf()).addNumber(Config.SERVICES_RATE_SPECIAL_DAYS).addNumber(Config.SERVICES_RATE_SPECIAL_ITEM_COUNT).addItemName(Config.SERVICES_RATE_SPECIAL_ITEM_ID).addString(String.valueOf(Config.SERVICES_RATE_SPECIAL_RATE));
			append += "]<br>";
			append += "[scripts_services.Bonus.bonus:buy" + " " + "4|";
			append += new CustomMessage("scripts.services.Bonus.bonus.buybonuscharadena", getSelf()).addNumber(Config.SERVICES_RATE_SPECIAL_DAYS).addNumber(Config.SERVICES_RATE_SPECIAL_ITEM_COUNT).addItemName(Config.SERVICES_RATE_SPECIAL_ITEM_ID).addString(String.valueOf(Config.SERVICES_RATE_SPECIAL_RATE));
			append += "]<br>";
			append += "[scripts_services.Bonus.bonus:buy" + " " + "5|";
			append += new CustomMessage("scripts.services.Bonus.bonus.buybonuscharspoil", getSelf()).addNumber(Config.SERVICES_RATE_SPECIAL_DAYS).addNumber(Config.SERVICES_RATE_SPECIAL_ITEM_COUNT).addItemName(Config.SERVICES_RATE_SPECIAL_ITEM_ID).addString(String.valueOf(Config.SERVICES_RATE_SPECIAL_RATE));
			append += "]<br>";
			append += "[scripts_services.Bonus.bonus:buy" + " " + "6|";
			append += new CustomMessage("scripts.services.Bonus.bonus.buybonuscharquest", getSelf()).addNumber(Config.SERVICES_RATE_SPECIAL_DAYS).addNumber(Config.SERVICES_RATE_SPECIAL_ITEM_COUNT).addItemName(Config.SERVICES_RATE_SPECIAL_ITEM_ID).addString(String.valueOf(Config.SERVICES_RATE_SPECIAL_RATE));
			append += "]";
			return append;
		}
		return "";
	}

	public void buy(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		int bonus_type;
		bonus_type = Integer.valueOf(args[0]);
		String bonus_class = "";
		switch(bonus_type)
		{
			case 1:
				// exp
				bonus_class = "RATE_XP";
				break;
			case 2:
				// sp
				bonus_class = "RATE_SP";
				break;
			case 3:
				//drop
				bonus_class = "RATE_DROP_ITEMS";
				break;
			case 4:
				//adena
				bonus_class = "RATE_DROP_ADENA";
				break;
			case 5:
				//spoil
				bonus_class = "RATE_DROP_SPOIL";
				break;
			case 6:
				//quest
				bonus_class = "RATE_QUESTS_REWARD";
				break;
		}
		if(HaveBonus(bonus_class))
			return;
		ThreadConnection con = null;
		FiltredPreparedStatement offline = null;
		Calendar bonus_expire = Calendar.getInstance();
		if(getItemCount(player, Config.SERVICES_RATE_SPECIAL_ITEM_ID) < Config.SERVICES_RATE_SPECIAL_ITEM_COUNT)
		{
			if(Config.SERVICES_RATE_SPECIAL_ITEM_ID == 57)
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			return;
		}
		bonus_expire.add(Calendar.DAY_OF_MONTH, Config.SERVICES_RATE_SPECIAL_DAYS);
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("INSERT INTO bonus(obj_id, bonus_name, bonus_value, bonus_expire_time) VALUES (?,?,?,?)");
			offline.setInt(1, player.getObjectId());
			offline.setString(2, bonus_class);
			offline.setInt(3, Config.SERVICES_RATE_SPECIAL_RATE);
			offline.setLong(4, bonus_expire.getTimeInMillis() / 1000);
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
		removeItem(player, Config.SERVICES_RATE_SPECIAL_ITEM_ID, Config.SERVICES_RATE_SPECIAL_ITEM_COUNT);
		Log.add("Character " + player.getName() + " buy bonus for char " + player.getName(), "bonus_event");
		show(new CustomMessage("scripts.services.Bonus.bonus.buyokchar", player).addString(bonus_class), player);
	}

	public void onLoad()
	{
		if(Config.SERVICES_RATE_SPECIAL_ENABLED)
			System.out.println("Loaded Service: Bonus [state: activated]");
		else
			System.out.println("Loaded Service: Bonus [state: deactivated]");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}