package services;

import java.sql.ResultSet;
import java.sql.SQLException;

import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.database.mysql;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.loginservercon.LSConnection;
import l2rt.gameserver.loginservercon.gspackets.ChangePassword;
import l2rt.gameserver.model.L2Player;
import l2rt.util.Files;
import l2rt.util.Util;

public class Activation extends Functions implements ScriptFile
{
	public void activation_page()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;
		player.block();
		player.setFlying(true); // хак позволяющий сделать логаут
		show(Files.read("data/scripts/services/activation.htm", player), player);
	}

	public void activation(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(args.length != 8)
		{
			sendMessage(new CustomMessage("scripts.services.Activation.InvalidArguments", player), player);
			activation_page();
			return;
		}

		// $oldpass $newmail11 $newmail12 $newmail21 $newmail22 $newpass1 $newpass2 conf

		String oldpass = args[0];
		String email1 = args[1] + "@" + args[2];
		String email2 = args[3] + "@" + args[4];
		String newpass1 = args[5];
		String newpass2 = args[6];

		if(!email1.equals(email2))
		{
			sendMessage(new CustomMessage("scripts.services.Activation.EmailAndConfirmationMustMatch", player), player);
			activation_page();
			return;
		}

		if(!newpass1.equals(newpass2))
		{
			sendMessage(new CustomMessage("scripts.services.Activation.NewPasswordAndConfirmationMustMatch", player), player);
			activation_page();
			return;
		}

		if(!Util.isMatchingRegexp(newpass1, Config.APASSWD_TEMPLATE))
		{
			sendMessage(new CustomMessage("scripts.services.Activation.InvalidNewPassword", player), player);
			activation_page();
			return;
		}

		if(!args[1].matches("[-a-zA-Z0-9_\\.]+") || !args[2].matches("[-a-zA-Z0-9_\\.]+"))
		{
			sendMessage(new CustomMessage("scripts.services.Activation.InvalidNewMail", player), player);
			activation_page();
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstanceLogin().getConnection();
			statement = con.prepareStatement("SELECT * FROM `accounts` WHERE email=?");
			statement.setString(1, email1);
			rset = statement.executeQuery();
			if(rset.next())
			{
				sendMessage(new CustomMessage("scripts.services.Activation.EmailAlreadyExists", player), player);
				activation_page();
				return;
			}
		}
		catch(SQLException e)
		{
			sendMessage(new CustomMessage("scripts.services.Activation.SomethingIsWrongTryAgain", player), player);
			e.printStackTrace();
			return;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		try
		{
			mysql.setEx(L2DatabaseFactory.getInstanceLogin(), "UPDATE `accounts` SET `email`=?,`activated`='1' WHERE `login`=?", email1, player.getAccountName());
			LSConnection.getInstance().sendPacket(new ChangePassword(player.getAccountName(), oldpass, newpass1, player.hasHWID() ? player.getHWID().Full : "null"));
		}
		catch(SQLException e)
		{
			sendMessage(new CustomMessage("scripts.services.Activation.SomethingIsWrongTryAgain", player), player);
			e.printStackTrace();
			return;
		}

		try
		{
			mysql.setEx(L2DatabaseFactory.getInstanceLogin(), "DELETE FROM `lock` WHERE login=?", player.getAccountName());
		}
		catch(SQLException e)
		{
			sendMessage(new CustomMessage("scripts.services.Activation.SomethingIsWrongTryAgain", player), player);
			e.printStackTrace();
			return;
		}

		player.logout(false, false, false, true);
	}

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}