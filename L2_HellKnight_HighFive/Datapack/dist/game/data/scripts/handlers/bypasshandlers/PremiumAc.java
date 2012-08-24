package handlers.bypasshandlers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import l2.hellknight.ExternalConfig;
import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.handler.IBypassHandler;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;

public class PremiumAc implements IBypassHandler
{
   private static final String[] COMMANDS =
   {
       "BuyPa",
   };

   private static final Logger _log = Logger.getLogger(PremiumAc.class.getName());

	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target) 
	{
		if (!(target instanceof L2Npc))
			return false;
		
		if (command.startsWith("BuyPa")) 
		{
			try 
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int type = Integer.parseInt(st.nextToken());
				String acc = activeChar.getAccountName();
				L2ItemInstance spbs = activeChar.getInventory().getItemByItemId(ExternalConfig.PREMIUM_COIN);
				switch (type) 
				{
				case 1:
					if (spbs == null) 
					{
						activeChar.sendMessage("There is not enough Coin.");
						return false;
					}
					activeChar.destroyItem("Premium", spbs, 10, activeChar, true);
					setPremium(1, acc);
					activeChar.sendMessage("Premium account for 1 day is activated. Please relogin!");
					break;
				case 2:
					if (spbs == null || spbs.getCount() < 80) 
					{
						activeChar.sendMessage("There is not enough Coin.");
						return false;
					}
					activeChar.destroyItem("Premium", spbs, 80, activeChar, true);
					setPremium(10, acc);
					activeChar.sendMessage("Premium account for 10 day is activated. Please relogin!");
					break;
				case 3:
					if (spbs == null || spbs.getCount() < 150) 
					{
						activeChar.sendMessage("There is not enough Coin.");
						return false;
					}
					activeChar.destroyItem("Premium", spbs, 150, activeChar, true);
					setPremium(30, acc);
					activeChar.sendMessage("Premium account for 30 day is activated. Please relogin!");
					break;
				}

				return true;
			} catch (Exception e) {
				_log.info("Exception in " + getClass().getSimpleName());
			}
		} 
		return false;
	}

   private void setPremium(int day, String account)
   {
       Connection con = null;
       try
       {
           Calendar finishtime = Calendar.getInstance();
           finishtime.setTimeInMillis(System.currentTimeMillis());
           finishtime.set(Calendar.SECOND, 0);
           finishtime.add(Calendar.DAY_OF_MONTH, day);
          
           con = L2DatabaseFactory.getInstance().getConnection();
           PreparedStatement statement = con.prepareStatement("UPDATE account_premium SET premium_service=?,enddate=? WHERE account_name=?");
           statement.setInt(1, 1);
           statement.setLong(2, finishtime.getTimeInMillis());
           statement.setString(3, account);
           statement.execute();
           statement.close();
       }
       catch (SQLException e)
       {
           _log.info("PremiumService:  Could not increase data");
       }
       finally
       {
           try
           {
               if (con != null)
                   con.close();
           }
           catch (SQLException e)
           {
               e.printStackTrace();
           }
       }      
      
   }

   public String[] getBypassList()
   {
       return COMMANDS;
   }
}