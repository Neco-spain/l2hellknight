package net.sf.l2j.gameserver.network;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Excellion
 */
public class HwidDisconnection implements Runnable
{
	private L2PcInstance _activeChar;
	public int baseresult;
	public int clientresult;
	
	public HwidDisconnection (L2PcInstance activeChar)
	{
		_activeChar = activeChar;
	}
	public void run()
	{
		int ishwidguard = 0;
		
		 Connection con = null;  
		 try  
			{ 
			        con = L2DatabaseFactory.getInstance().getConnection(); 
			        PreparedStatement preparedstatement1 = con.prepareStatement("SELECT HWIDBlock FROM accounts WHERE login=?");   
			        preparedstatement1.setString(1, _activeChar.getAccountName()); 
			        ResultSet resultset1 = preparedstatement1.executeQuery();   
			        resultset1.next();
			        
			        PreparedStatement preparedstatement2 = con.prepareStatement("SELECT HWIDBlockON FROM accounts WHERE login=?");   
			        preparedstatement2.setString(1, _activeChar.getAccountName()); 
			        ResultSet resultset2 = preparedstatement2.executeQuery();   
			        resultset2.next();
			        
			        baseresult =  resultset1.getInt(1);
			        clientresult =  _activeChar.getClient().getSessionId().clientKey;
			        ishwidguard = resultset2.getInt(1);
			       
			  
			 } 
			 catch (Exception e)  
			 { 
			      
			 } 
			 
			 if (ishwidguard == 1)
			 {
				if (baseresult == clientresult) 
				{
					_activeChar.setClientKey(true);
				}
				else
				{
	        		_activeChar.sendMessage("Это аккаунт привязан к другому компьютеру");
	        		_activeChar.closeNetConnection(false);
				}
				 
				 
			 }
			 
	}
}