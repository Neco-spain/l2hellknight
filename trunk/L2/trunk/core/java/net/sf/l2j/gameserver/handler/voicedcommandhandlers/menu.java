package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2DonateInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.events.CTF;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

public class menu
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "menu", "eon_menu_" };
  public static boolean _vsc = Config.VIEW_SKILL_CHANCE;
  private boolean _ipblock = false;
  private long time;
  String str = "";
  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if (command.equalsIgnoreCase("menu"))
    {
      showHtm(activeChar);
    }
    else if (command.startsWith("eon_menu_"))
    {
      String addcmd = command.substring(9).trim();
      if (addcmd.startsWith("exp"))
      {
        int flag = Integer.parseInt(addcmd.substring(3).trim());
        if (flag == 0)
        {
        	activeChar.setExpOn(false);
            activeChar.sendMessage("- Получение опыта отключено. Помни! Для отмытия кармы нужен опыт!");
        }
        else 
        {
        	activeChar.setExpOn(true);
            activeChar.sendMessage("- Получение опыта возобновлено");
        }
        showHtm(activeChar);
        return true;
      }
      
      if (addcmd.startsWith("loot"))
      {
        int flag = Integer.parseInt(addcmd.substring(4).trim());
        if (flag == 0)
        {
        	activeChar.setAutoLoot(false);
            activeChar.sendMessage("- Автолут отключен!");
        }
        else 
        {
        	activeChar.setAutoLoot(true);
            activeChar.sendMessage("- Автолут включен!");
        }
        showHtm(activeChar);
        return true;
      }
      
      if (addcmd.startsWith("offline"))
      {
    	  L2Character player = (activeChar);
    	  
    	  if (!player.isInsideZone(L2Character.ZONE_PEACE))
    	  {
    		  activeChar.sendMessage("Вы не можите использовать офлайн трейд в не города");
    		  return false;
    	  }
        if ((Config.OFFLINE_TRADE_ENABLE && (activeChar.getPrivateStoreType() == 1 
        		|| activeChar.getPrivateStoreType() == 3
				|| activeChar.getPrivateStoreType() == 8))
				|| (Config.OFFLINE_CRAFT_ENABLE && activeChar.getPrivateStoreType() == 5))
        {
        	activeChar.store();
        	activeChar.closeNetConnection(true);
        }
        else
        {
        	activeChar.sendMessage("Вы не можете использовать оффлайн торговлю/крафт в данный момент");
            showHtm(activeChar);
        }
        return true;
      }
      
      if (addcmd.startsWith("repair"))
      {
    	String nick = addcmd.substring(6);
  	    if(nick != null && nick.length() > 0)
  		  {
  		    repair(activeChar, nick);
  		  }
  	    else
  	      {
		    activeChar.sendMessage("Введите ник");
  	      }
  	    showHtm(activeChar);
        return true;
      }
      
      if (addcmd.startsWith("trade"))
      {
    	  int flag = Integer.parseInt(addcmd.substring(5).trim());
          if (flag == 0)
          {
          	  activeChar.setTradeRefusal(false);
              activeChar.sendMessage("- Возможность использовать трейд включена");
          }
          else 
          {
          	  activeChar.setTradeRefusal(true);
              activeChar.sendMessage("- Возможность использовать трейд отключена");
          }
          showHtm(activeChar);
          return true;
      }
      
      if (addcmd.startsWith("clientkey"))
      {
    	  int flag = Integer.parseInt(addcmd.substring(9).trim());
          if (flag == 0)
          {
          	  hwidblockadd(activeChar);
          	  activeChar.setClientKey(true);
              activeChar.sendMessage("- Вы привязали свой аккаунт по железу компьютера");
          }
          else 
          {
        	  hwidblockDel(activeChar);
        	  activeChar.setClientKey(false);
              activeChar.sendMessage("- Вы отвязали свой аккаунт по железу компьютер");
          }
          showHtm(activeChar);
          return true;
      }
      
      if (addcmd.startsWith("pm"))
      {
    	  int flag = Integer.parseInt(addcmd.substring(2).trim());
    	  if (flag == 0)
			{
				activeChar.setMessageRefusal(false);
				activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_ACCEPTANCE_MODE));
			}
			else
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));
			}
          showHtm(activeChar);
          return true;
      }
      
      if (addcmd.startsWith("skillchance"))
      {
    	  int flag = Integer.parseInt(addcmd.substring(11).trim());
    	  if (flag == 0)
			{
				_vsc = true;
				activeChar.sendMessage("- Включено отображение шанса прохождения скиллов");
			}
			else
			{
				_vsc = false;
				activeChar.sendMessage("- Отображение шанса прохождения скиллов отключено");
			}
          showHtm(activeChar);
          return true;
      }
      
      if (addcmd.startsWith("showanim"))
      {
    	  int flag = Integer.parseInt(addcmd.substring(8).trim());
    	  if (flag == 0)
			{
				activeChar.setShowAnim(true);
				activeChar.sendMessage("Включены Анимированые бафы");
			}
			else
			{
				activeChar.setShowAnim(false);
				activeChar.sendMessage("Включены Анимированые бафы");
			}
          showHtm(activeChar);
          return true;
      }
      
      if (addcmd.startsWith("prem"))
      {
    	  int flag = Integer.parseInt(addcmd.substring(4).trim());
    	  if (flag == 0)
			{
    		  showPremHtm(activeChar);
			}
			else if (flag == 1)
			{
				if(activeChar.getInventory().getItemByItemId(Config.DON_ITEM_ID) == null || activeChar.getInventory().getItemByItemId(Config.DON_ITEM_ID).getCount() < Config.COL_PREM1)
				{
					activeChar.sendMessage("Не хватает монет");
					showPremHtm(activeChar);
					return false;
				}
				if (activeChar.getPremiumService() > 0)
				{
					activeChar.sendMessage("У вас уже есть премиум, дождитесь его окончания");
					return false;
				}
				activeChar.destroyItemByItemId("Consume", Config.DON_ITEM_ID, Config.COL_PREM1, activeChar, false);
				MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 2023, 1, 1, 0);
				activeChar.sendPacket(MSU);
				activeChar.broadcastPacket(MSU);
				L2DonateInstance.addPremiumServices(1, activeChar.getAccountName());
				showPremHtm(activeChar);
				activeChar.sendMessage("Вы получили премиум аккаунт на 1 день. Перезайдите в игру");
				activeChar.store();
			}
			else if (flag == 2)
			{
				if(activeChar.getInventory().getItemByItemId(Config.DON_ITEM_ID) == null || activeChar.getInventory().getItemByItemId(Config.DON_ITEM_ID).getCount() < Config.COL_PREM2)
				{
					activeChar.sendMessage("Не хватает монет");
					showPremHtm(activeChar);
					return false;
				}
				if (activeChar.getPremiumService() > 0)
				{
					activeChar.sendMessage("У вас уже есть премиум, дождитесь его окончания");
					return false;
				}
				activeChar.destroyItemByItemId("Consume", Config.DON_ITEM_ID, Config.COL_PREM2, activeChar, false);
				MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 2023, 1, 1, 0);
				activeChar.sendPacket(MSU);
				activeChar.broadcastPacket(MSU);
				L2DonateInstance.addPremiumServices(3, activeChar.getAccountName());
				showPremHtm(activeChar);
				activeChar.sendMessage("Вы получили премиум аккаунт на 3 дня. Перезайдите в игру");
				activeChar.store();
			}
			else if (flag == 3)
			{
				if(activeChar.getInventory().getItemByItemId(Config.DON_ITEM_ID) == null || activeChar.getInventory().getItemByItemId(Config.DON_ITEM_ID).getCount() < Config.COL_PREM3)
				{
					activeChar.sendMessage("Не хватает монет");
					showPremHtm(activeChar);
					return false;
				}
				if (activeChar.getPremiumService() > 0)
				{
					activeChar.sendMessage("У вас уже есть премиум, дождитесь его окончания");
					return false;
				}
				activeChar.destroyItemByItemId("Consume", Config.DON_ITEM_ID, Config.COL_PREM3, activeChar, false);
				MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 2023, 1, 1, 0);
				activeChar.sendPacket(MSU);
				activeChar.broadcastPacket(MSU);
				L2DonateInstance.addPremiumServices(7, activeChar.getAccountName());
				showPremHtm(activeChar);
				activeChar.sendMessage("Вы получили премиум аккаунт на 7 дней. Перезайдите в игру");
				activeChar.store();
			}
			else if (flag == 4)
			{
				showHtm(activeChar);
			}
          return true;
      }
      
      if (addcmd.startsWith("ip"))
      {
    	  int flag = Integer.parseInt(addcmd.substring(2).trim());
    	  if (flag == 0)
			{
				ipblockadd(activeChar);
				_ipblock = true;
			}
			else
			{
				ipblockdel(activeChar);
				_ipblock = false;
			}
          showHtm(activeChar);
          return true;
      }
      
      
      if (addcmd.startsWith("evt"))
      {
    	  int flag = Integer.parseInt(addcmd.substring(3).trim());
    	  if (flag == 0)
			{
    		  showEvtHtm(activeChar);
			}
			else if (flag == 1)
			{
				if (TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
				{
					activeChar.sendMessage("Вы уже зарегистрированы на участие в ТвТ");
					showEvtHtm(activeChar);
					return false;
				}
				else if (TvTEvent.isParticipating())
				{
					TvTEvent.addParticipant(activeChar);
					activeChar.sendMessage("Вы зарегистрированы на участие в ТвТ");
					showEvtHtm(activeChar);
				}
				else
				{
						activeChar.sendMessage("ТвТ не активен или уже идет турнир");
						showHtm(activeChar);
						return false;
				}
			}
			else if (flag == 2)
			{
				
				if (TvTEvent.isParticipating())
				{
					if (!TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
					{
						activeChar.sendMessage("Вы не были зарегистрированы на участие в ТвТ");
						showEvtHtm(activeChar);
						return false;
					}
					TvTEvent.removeParticipant(activeChar.getObjectId());
					if (Config.TVT_SAME_IP)
				      {
				        if (activeChar != null)
				        {
				          String host = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
				          if (TvTEvent._listedIps.contains(host)) 
				          {
				        	  TvTEvent._listedIps.remove(host);
				          }
				        }
				      }
					activeChar.sendMessage("Вы больше не зарегистрированы на участие в ТвТ");
					showEvtHtm(activeChar);
				}
				else
				{
						activeChar.sendMessage("ТвТ не активен или уже идет турнир");
						showHtm(activeChar);
						return false;
				}
			}
			else if (flag == 3)
			{
			  if (CTF._joining)
				{
				    CTF.addPlayer(activeChar, "eventShuffle");
				    activeChar.sendMessage("Вы зарегистрированы на участие в CTF");
					showEvtHtm(activeChar);
				}
			 else
				{
					activeChar.sendMessage("CTF не активен или уже идет турнир");
					showHtm(activeChar);
					return false;
				}
			}
			else if (flag == 4)
			{
			  if (CTF._joining)
				{
				    CTF.removePlayer(activeChar);
				    activeChar.sendMessage("Вы больше не зарегистрированы на участие в CTF");
					showEvtHtm(activeChar);
				}
			 else
				{
					activeChar.sendMessage("CTF не активен или уже идет турнир");
					showHtm(activeChar);
					return false;
				}
			}
    	  if (flag == 9)
			{
				showHtm(activeChar);
			}
          return true;
      }
      
      
      return false;
    }
    return true;
  }

  private void showHtm(L2PcInstance activeChar)
  {
	    NpcHtmlMessage htm = new NpcHtmlMessage(activeChar.getLastQuestNpcObject());
		String text = HtmCache.getInstance().getHtm("data/html/menu/menu.htm");
		htm.setHtml(text);
		activeChar.sendPacket(htm);
		L2World.getInstance();
		int realonl = L2World.getAllPlayersCount();
		int nakrutka = realonl*(Config.NAKRUTKA_ONLINE / 100);
		String online = str+(realonl + nakrutka);
		htm.replace("%online%", online );
		if (activeChar.getExpOn())
	        {
				htm.replace("%gainexp%", "ON");
	        }
		else
			{
				htm.replace("%gainexp%", "OFF");
			}
		if (activeChar.isAutoLoot())
        	{
				htm.replace("%autoloot%", "ON");
        	}
		else
			{
				htm.replace("%autoloot%", "OFF");
			}
		if (activeChar.getTradeRefusal())
    		{
				htm.replace("%trade%", "OFF");
    		}
		else
			{
				htm.replace("%trade%", "ON");
			}
		if (activeChar.getMessageRefusal())
			{
				htm.replace("%pm%", "OFF");
			}
		else
			{
				htm.replace("%pm%", "ON");
			}
		if (_vsc)
			{
				htm.replace("%skillchance%", "ON");
			}
		else
			{
				htm.replace("%skillchance%", "OFF");
			}
		if (activeChar.getShowAnim())
		{
			htm.replace("%showanim%", "ON");
		}
		else
		{
			htm.replace("%showanim%", "OFF");
		}
		if (_ipblock)
			{
				htm.replace("%ip%", "ON");
			}
		else
			{
				htm.replace("%ip%", "OFF");
			}
		if (activeChar.getClientKey())
		{
			htm.replace("%clientkey%", "ON");
		}
		else
		{
			htm.replace("%clientkey%", "OFF");
		}
  }
  
  
  private void repair(L2PcInstance activeChar, String nick)
  {
			for(Entry<Integer, String> entry : activeChar.getAccountChars().entrySet())
			{
				int obj_id = entry.getKey();
				int karma = 0;
				if(!activeChar.getAccountChars().containsValue(nick.substring(1)))
				{
					activeChar.sendMessage("Вы не можете восстановить персонажа с другого аккаунта или сами себя!");
					return;
				}
				Connection con = null;
				PreparedStatement statement = null;
				ResultSet rset = null;
				try
				{
					// Retrieve the L2PcInstance from the characters table of the database
					con = L2DatabaseFactory.getInstance().getConnection();

					statement = con.prepareStatement("SELECT karma FROM characters where char_name='"+nick.substring(1)+"';");
					rset = statement.executeQuery();
					while (rset.next())
					{
						karma = rset.getInt("karma");
					}
					rset.close();
					statement.close();
					if(karma > 0)
					{
						statement = con.prepareStatement("UPDATE `characters` SET `x`='17144', `y`='170156', `z`='-3502', `heading`='0' WHERE `obj_Id`='" + obj_id + "' LIMIT 1");
						statement.executeUpdate();
						statement.close();
					}
					else
					{
						statement = con.prepareStatement("UPDATE `characters` SET `x`='82698', `y`='148638', `z`='-3470', `heading`='0' WHERE `obj_Id`='" + obj_id + "' LIMIT 1");
						statement.executeUpdate();
						statement.close();
					}
					activeChar.sendMessage("Восстановление успешно!");
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Ошибка.");
				}
				finally
				{
					try {
						con.close();
					} catch (SQLException e) {
						
						e.printStackTrace();
					}
				}
				break;
			}
			
  }
  
  public void ipblockdel(L2PcInstance l2pcinstance)
  {
      try
      {
          Connection connection = L2DatabaseFactory.getInstance().getConnection();
          PreparedStatement preparedstatement = connection.prepareStatement("SELECT * FROM accounts WHERE login=?");
          preparedstatement.setString(1, l2pcinstance.getAccountName());
          ResultSet resultset = preparedstatement.executeQuery();
          resultset.next();
          PreparedStatement preparedstatement1 = connection.prepareStatement("UPDATE accounts SET IPBlock = 0 WHERE login=?");
          preparedstatement1.setString(1, l2pcinstance.getAccountName());
          preparedstatement1.execute();
          l2pcinstance.sendMessage("Привязка аккаунта к IP снята");
           
      }
      catch(SQLException sqlexception)
      {}
  }
  public void ipblockadd(L2PcInstance l2pcinstance)
  {
	  try
      {
	  Connection connection = L2DatabaseFactory.getInstance().getConnection();
	  PreparedStatement preparedstatement = connection.prepareStatement("SELECT * FROM accounts WHERE login=?");
      preparedstatement.setString(1, l2pcinstance.getAccountName());
      ResultSet resultset = preparedstatement.executeQuery();
      resultset.next();
	  PreparedStatement preparedstatement2 = connection.prepareStatement("UPDATE accounts SET IPBlock = 1 WHERE login=?");
      preparedstatement2.setString(1, l2pcinstance.getAccountName());
      preparedstatement2.execute();
      l2pcinstance.sendMessage((new StringBuilder()).append("Ваш аккаунт привязан к вашему текущему IP: ").append(resultset.getString("lastIP")).toString());
      }
      catch(SQLException sqlexception)
      {}
	  
  }
  public void hwidblockadd(L2PcInstance l2pcinstance)
  {
	  try
      {
		 
		  Connection connection = L2DatabaseFactory.getInstance().getConnection();
		  PreparedStatement preparedstatement = connection.prepareStatement("SELECT * FROM accounts WHERE login=?");
		  preparedstatement.setString(1, l2pcinstance.getAccountName());
		  ResultSet resultset = preparedstatement.executeQuery();
		  resultset.next();
		  //add key
		  PreparedStatement preparedstatement2 = connection.prepareStatement("UPDATE accounts SET HWIDBlock = ? WHERE login=?");
		  preparedstatement2.setInt(1, l2pcinstance.getClient().getSessionId().clientKey);
		  preparedstatement2.setString(2, l2pcinstance.getAccountName());
		  preparedstatement2.execute();
		  //add parametres
		  PreparedStatement preparedstatement3 = connection.prepareStatement("UPDATE accounts SET HWIDBlockON = 1 WHERE login=?");
		  preparedstatement3.setString(1, l2pcinstance.getAccountName());
		  preparedstatement3.execute();
      }
      	catch(SQLException sqlexception)
      {
      		
      }
	  
  }
  
  public void hwidblockDel(L2PcInstance l2pcinstance)
  {
	  try
      {
		  Connection connection = L2DatabaseFactory.getInstance().getConnection();
		  PreparedStatement preparedstatement = connection.prepareStatement("SELECT * FROM accounts WHERE login=?");
		  preparedstatement.setString(1, l2pcinstance.getAccountName());
		  ResultSet resultset = preparedstatement.executeQuery();
		  resultset.next();
		  //add parametres
		  PreparedStatement preparedstatement3 = connection.prepareStatement("UPDATE accounts SET HWIDBlockON = 0 WHERE login=?");
		  preparedstatement3.setString(1, l2pcinstance.getAccountName());
		  preparedstatement3.execute();

      }
      	catch(SQLException sqlexception)
      {
      		
      }
	  
  }
  protected StatsSet _StateSet;
  private void showPremHtm(L2PcInstance activeChar)
  {
	    NpcHtmlMessage htm = new NpcHtmlMessage(activeChar.getLastQuestNpcObject());
		String text = HtmCache.getInstance().getHtm("data/html/menu/menu-pa.htm");
		htm.setHtml(text);
		activeChar.sendPacket(htm);
		htm.replace("%price1%", str+Config.COL_PREM1 );
		htm.replace("%price2%", str+Config.COL_PREM2 );
		htm.replace("%price3%", str+Config.COL_PREM3 );
		if (activeChar.getPremiumService() == 0)
			htm.replace("%exptime%", "не активирован");
		else if (activeChar.getPremiumService() == 1)
		{
			getExpTime(activeChar.getAccountName());
			String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date (time));
			htm.replace("%exptime%", date);
		}
  }
  
  
  private void showEvtHtm(L2PcInstance activeChar)
  {
	    NpcHtmlMessage htm = new NpcHtmlMessage(activeChar.getLastQuestNpcObject());
		String text = HtmCache.getInstance().getHtm("data/html/menu/menu-evt.htm");
		htm.setHtml(text);
		activeChar.sendPacket(htm);
		//htm.replace("%price1%", str+Config.COL_PREM1 );
		//htm.replace("%price2%", str+Config.COL_PREM2 );
		//htm.replace("%price3%", str+Config.COL_PREM3 );
		if (TvTEvent.isInactive() || TvTEvent.isInactivating())
		{
			htm.replace("%tvt%", "<font color=ff0000>Не активен</font>");
		}
		else if (TvTEvent.isParticipating())
		{
			htm.replace("%tvt%", "<font color=00ff00>Регистрация</font>");
		}
		else if (TvTEvent.isStarted() || TvTEvent.isStarting() || TvTEvent.isRewarding())
		{
			htm.replace("%tvt%", "<font color=0000ff>Активен</font>");
		}
		if (CTF._joining)
		{
			htm.replace("%ctf%", "<font color=00ff00>Регистрация</font>");
		}
		else if (CTF._started || CTF._teleport)
		{
			htm.replace("%ctf%", "<font color=0000ff>Активен</font>");
		}
		else
		{
			htm.replace("%ctf%", "<font color=ff0000>Не активен</font>");
		}
  }
  
  private void getExpTime(String accName)
  {
	  Connection con = null;
	  try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT enddate FROM account_premium WHERE account_name=?");
		     statement.setString(1, accName);
		      ResultSet rset = statement.executeQuery();
		      while (rset.next())
		      {
		        this.time = rset.getLong("enddate");
		      }
		      rset.close();
		      statement.close();
		    }
		    catch (Exception e)
		    {
		      try
		      {
		        con.close(); } catch (Exception e1) {
		        return;
		      }
		    }
		    finally
		    {
		      try
		      {
		        con.close(); } catch (Exception e) {
		      }
		    }
  }
  
  public String[] getVoicedCommandList()
  {
    return VOICED_COMMANDS;
  }
}