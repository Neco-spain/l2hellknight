package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;

/**
 * @author PaInKiLlEr
 */
public class Account extends Functions implements ScriptFile
{
	
	public void CharToAcc()
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		if (!Config.ACC_MOVE_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		String append = "Перенос персонажей между аккаунтами.<br>";
		append += "Цена: " + Config.ACC_MOVE_PRICE + " " + ItemHolder.getInstance().getTemplate(Config.ACC_MOVE_ITEM).getName() + ".<br>";
		append += "Внимание !!! При переносе персонажа на другой аккаунт, убедитесь что персонажей там меньше чем 7, иначе могут возникнуть непредвиденные ситуации за которые Администрация не отвечает.<br>";
		append += "Внимательно вводите логин куда переносите, администрация не возвращает персонажей.";
		append += "Вы переносите персонажа " + player.getName() + ", на какой аккаунт его перенести ?";
		append += "<edit var=\"new_acc\" width=150>";
		append += "<button value=\"Перенести\" action=\"bypass -h scripts_services.Account:NewAccount $new_acc\" width=150 height=15><br>";
		show(append, player, null);
		
	}

	public void NewAccount(String[] name)
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		if (!Config.ACC_MOVE_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		if(player.getInventory().getCountOf(Config.ACC_MOVE_ITEM) < Config.ACC_MOVE_PRICE)
		{
			player.sendMessage("У вас нету " + Config.ACC_MOVE_PRICE + " " + ItemHolder.getInstance().getTemplate(Config.ACC_MOVE_ITEM));
			CharToAcc();
			return;
		}
		String _name = name[0];
		Connection con = null;
        Connection conGS = null;
		PreparedStatement offline = null;
        Statement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT `login` FROM `accounts` WHERE `login` = ?");
			offline.setString(1, _name);
			rs = offline.executeQuery();
			if(rs.next())
			{
				removeItem(player, Config.ACC_MOVE_ITEM, Config.ACC_MOVE_PRICE);
                conGS = DatabaseFactory.getInstance().getConnection();
			    statement = conGS.createStatement();
				statement.executeUpdate("UPDATE `characters` SET `account_name` = '" + _name + "' WHERE `char_name` = '" + player.getName() + "'");
				player.sendMessage("Персонаж успешно перенесен.");
				player.logout();
			}
			else
			{
				player.sendMessage("Введенный аккаунт не найден.");
				CharToAcc();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
            DbUtils.closeQuietly(conGS, statement);
		}
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
}