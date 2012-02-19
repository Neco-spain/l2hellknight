package handlers.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;

import l2.brick.L2DatabaseFactory;
import l2.brick.gameserver.handler.IVoicedCommandHandler;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.ConfirmDlg;

/**
 * 
 * @author JaJa
 *
 */

public class BindingIP implements IVoicedCommandHandler {
	
	private static final String[] _voicedCommands = { "bind_ip", "bind_process" };
	
	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String param)
	{
		
		//_log.info( "command \"" + command + "\", param : \"" + param + "\"" );
		
		String userIP = null;
		Connection con = null;
		
		try {
			
			con = L2DatabaseFactory.getInstance().getConnection();
	
			if (command.equalsIgnoreCase("bind_ip"))
			{


				String lastIP = null;				
				final PreparedStatement statement = con.prepareStatement("SELECT lastIP, userIP FROM accounts WHERE login=?");
				statement.setString(1, activeChar.getAccountName());
				final ResultSet rset = statement.executeQuery();
				if (rset.next())
				{
					lastIP = rset.getString("lastIP");
					userIP = rset.getString("userIP");
				}
				rset.close();
				statement.close();
				
				ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1);
				if( userIP == null )
				{
					activeChar.setVoiceConfirmCmd("bind_process,on " + lastIP );
					dlg.addString( "Are you sure, for bind your account for ip " + lastIP );
				}
				else
				{
					activeChar.setVoiceConfirmCmd("bind_process,off" );
					dlg.addString("Are you sure, for unbind ip your account ?");
				}
				activeChar.sendPacket(dlg);

			}
			else if (command.equalsIgnoreCase("bind_process"))
			{
				int ipUpdated = 0;
				final StringTokenizer st = new StringTokenizer(param);
				String mode = null;
				
				if (st.hasMoreTokens())
					mode = st.nextToken();
				if ( mode.equalsIgnoreCase("on") && st.hasMoreTokens())
					userIP = st.nextToken();
			

				final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET userIP=? WHERE login=?");
				ps.setString(1, ( mode.equalsIgnoreCase("on") ? userIP : null ) );
				ps.setString(2, activeChar.getAccountName());
				ipUpdated = ps.executeUpdate();
				ps.close();

				
				_log.info("Character " + activeChar.getName() + " has " + ( mode.equalsIgnoreCase("on") ? " bind account ip " + userIP : " unbind account on ip" ) );
				
				if (ipUpdated > 0)
					activeChar.sendMessage("Your account bind change successfully!");
				else
					activeChar.sendMessage("Your account bind change troubles!");		
			}
		
		} catch (SQLException e1) 
		{
			_log.info(e1.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}

}
