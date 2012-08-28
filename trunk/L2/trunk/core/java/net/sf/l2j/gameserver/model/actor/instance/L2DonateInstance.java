package net.sf.l2j.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.entity.events.Heroes;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 *
 * @version $Revision: 1.10.4.9 $ $Date: 2005/04/11 10:06:08 $
 */
public class L2DonateInstance extends L2FolkInstance
{
	//private static Logger _log = Logger.getLogger(L2ArteiaGoldMerchantInstance.class.getName());

	private static int SETNAME = Config.COL_CHANGENAME;
	private static int SETNAMECOLOR =Config.COL_NICKCOLOR;
	private static int SETTITLECOLOR = Config.COL_TITLECOLOR;
	private static int SETCLANNAME = Config.COL_CHANGECLANNAME;
	private static int CLANLVL6 = Config.COL_6LVL_CLAN;
	private static int CLANLVL7 = Config.COL_7LVL_CLAN;
	private static int CLANLVL8 = Config.COL_8LVL_CLAN;
	private static int NOBLESS = Config.COL_NOBLESSE;
	private static int PREM1 = Config.COL_PREM1;
	private static int PREM2 = Config.COL_PREM2;
	private static int PREM3 = Config.COL_PREM3;
	private static int SEX = Config.COL_SEX;
	private static int PK = Config.COL_PK;
	private static int HERO = Config.COL_HERO;
	public static int CRP_ITEM_ID = Config.CRP_ITEM_ID;
	public static int ITEM_ID = Config.DON_ITEM_ID;
	String str = "";
    /**
	 * @param template
	 */
	public L2DonateInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0) pom = "" + npcId;
		else pom = npcId + "-" + val;
		return "data/html/donate/" + pom + ".htm";
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		if (actualCommand.equalsIgnoreCase("Multisell"))
		{
			if (st.countTokens() < 1) return;
			int val = Integer.parseInt(st.nextToken());
			L2Multisell.getInstance().SeparateAndSend(val, player, false, getCastle().getTaxRate());
		}
		else if (actualCommand.equalsIgnoreCase("premadd1"))
		{
			if(player.getInventory().getItemByItemId(ITEM_ID) == null || player.getInventory().getItemByItemId(ITEM_ID).getCount() < PREM1)
			{
				player.sendMessage("Не хватает монет");
				return;
			}
			player.destroyItemByItemId("Consume", ITEM_ID, PREM1, player, false);
			try
		      {
					addPremiumServices(1, player.getAccountName());
		      }
		      catch (StringIndexOutOfBoundsException e)
		      {
		    	  player.sendMessage("Ошибка");
		      }
		    MagicSkillUser MSU = new MagicSkillUser(player, player, 2023, 1, 1, 0);
		    player.sendPacket(MSU);
		    player.broadcastPacket(MSU);
			player.sendMessage("Вы получили премиум аккаунт на 1 месяц. Новое получение не добавляет время, а обновляет!");
			player.store();
		}
		
		else if (actualCommand.equalsIgnoreCase("premadd2"))
		{
			if(player.getInventory().getItemByItemId(ITEM_ID) == null || player.getInventory().getItemByItemId(ITEM_ID).getCount() < PREM2)
			{
				player.sendMessage("Не хватает монет");
				return;
			}
			player.destroyItemByItemId("Consume", ITEM_ID, PREM2, player, false);
			try
		      {
		        
		        addPremiumServices(1, player.getAccountName());
		      }
		      catch (StringIndexOutOfBoundsException e)
		      {
		        player.sendMessage("Ошибка");
		      }
		    MagicSkillUser MSU = new MagicSkillUser(player, player, 2023, 1, 1, 0);
		    player.sendPacket(MSU);
		    player.broadcastPacket(MSU);
			player.sendMessage("Вы получили премиум аккаунт на 2 месяца. Новое получение не добавляет время, а обновляет!");
			player.store();
		}
		else if (actualCommand.equalsIgnoreCase("premadd3"))
		{
			if(player.getInventory().getItemByItemId(ITEM_ID) == null || player.getInventory().getItemByItemId(ITEM_ID).getCount() < PREM1)
			{
				player.sendMessage("Не хватает монет");
				return;
			}
			player.destroyItemByItemId("Consume", ITEM_ID, PREM3, player, false);
			try
		      {
		        
		        addPremiumServices(1, player.getAccountName());
		      }
		      catch (StringIndexOutOfBoundsException e)
		      {
		        player.sendMessage("Ошибка");
		      }
		    MagicSkillUser MSU = new MagicSkillUser(player, player, 2023, 1, 1, 0);
		    player.sendPacket(MSU);
		    player.broadcastPacket(MSU);
			player.sendMessage("Вы получили премиум аккаунт на 3 месяца. Новое получение не добавляет время, а обновляет!");
			player.store();
		}
		else if (actualCommand.equalsIgnoreCase("Setname"))
		{
			if (st.countTokens() < 1) return;
			String newname = st.nextToken();
			if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < SETNAME)
            {
                player.sendMessage("У Вас не достаточное кол-во монет для проведения операции.");
                return;
            }
            else if ((newname.length() < 3) || (newname.length() > 16))
            {
                player.sendMessage("Это имя не может быть использовано.");
                return;
            }
            else if (CharNameTable.getInstance().doesCharNameExist(newname))
            {
                player.sendMessage("Это имя уже занято.");
                return;
            }
            else if (player.isClanLeader())
            {
                player.sendMessage("Передайте клан на время смены ника другому игроку");
                return;
            }
            L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Setname "+newname+" for "+player.getName(), ITEM_ID, SETNAME, player, player);
            if (destritem != null)
            {
                player.setName(newname);
				player.sendMessage("Вы успешно сменили свое имя!");
                player.setClan(player.getClan());
				player.broadcastUserInfo();
				player.store();

                InventoryUpdate iu = new InventoryUpdate();
                if (destritem.getCount() == 0) iu.addRemovedItem(destritem);
                else iu.addModifiedItem(destritem);
                player.sendPacket(iu);
            }
            else
            {
                player.sendMessage("Ошибка!");
            }
		}
        else if (actualCommand.equalsIgnoreCase("Setnamecolor"))
		{
			if (st.countTokens() < 1) return;
			String newcolor = st.nextToken();
            int color = 0;
            try
            {
                color = Integer.parseInt(newcolor);
            }
            catch (Exception e)
            {
                return;
            }
            newcolor = "";
            switch (color)
            {
                case 1: newcolor = "FFFF00";
                        break;
                case 2: newcolor = "000000";
                        break;
                case 3: newcolor = "FF0000";
                        break;
                case 4: newcolor = "FF00FF";
                        break;
                case 5: newcolor = "808080";
                        break;
                case 6: newcolor = "008000";
                        break;
                case 7: newcolor = "00FF00";
                        break;
                case 8: newcolor = "800000";
                        break;
                case 9: newcolor = "008080";
                        break;
                case 10: newcolor = "800080";
                        break;
                case 11: newcolor = "808000";
                        break;
                case 12: newcolor = "FFFFFF";
                        break;
                case 13: newcolor = "00FFFF";
                        break;
                case 14: newcolor = "C0C0C0";
                        break;
                case 15: newcolor = "17A0D4";
                        break;
                default:return;
            }
			if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < SETNAMECOLOR)
            {
                player.sendMessage("У Вас не достаточное кол-во монет для проведения операции.");
                showServices(player);
                return;
            }
            L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Setnamecolor "+newcolor+" for "+player.getName(), ITEM_ID, SETNAMECOLOR, player, player);
            if (destritem != null)
            {
				player.sendMessage("Вы успешно изменили цвет имени!");
                player.getAppearance().setNameColor(Integer.decode("0x"+newcolor));
				player.broadcastUserInfo();
				player.store();
				showServices(player);
                InventoryUpdate iu = new InventoryUpdate();
                if (destritem.getCount() == 0) iu.addRemovedItem(destritem);
                else iu.addModifiedItem(destritem);
                player.sendPacket(iu);
            }
            else
            {
                player.sendMessage("Ошибка!");
            }
		}
        else if (actualCommand.equalsIgnoreCase("Settitlecolor"))
		{
			if (st.countTokens() < 1) return;
			String newcolor = st.nextToken();
            int color = 0;
            try
            {
                color = Integer.parseInt(newcolor);
            }
            catch (Exception e)
            {
                return;
            }
            newcolor = "";
            switch (color)
            {
                case 1: newcolor = "FFFF00";
                        break;
                case 2: newcolor = "000000";
                        break;
                case 3: newcolor = "FF0000";
                        break;
                case 4: newcolor = "FF00FF";
                        break;
                case 5: newcolor = "808080";
                        break;
                case 6: newcolor = "008000";
                        break;
                case 7: newcolor = "00FF00";
                        break;
                case 8: newcolor = "800000";
                        break;
                case 9: newcolor = "008080";
                        break;
                case 10: newcolor = "800080";
                        break;
                case 11: newcolor = "808000";
                        break;
                case 12: newcolor = "FFFFFF";
                        break;
                case 13: newcolor = "00FFFF";
                        break;
                case 14: newcolor = "C0C0C0";
                        break;
                case 15: newcolor = "17A0D4";
                        break;
                default:return;
            }
			if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < SETTITLECOLOR)
            {
                player.sendMessage("У Вас не достаточное кол-во монет для проведения операции.");
                showServices(player);
                return;
            }
            L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Settitlecolor "+newcolor+" for "+player.getName(), ITEM_ID, SETTITLECOLOR, player, player);
            if (destritem != null)
            {
				player.sendMessage("Вы успешно изменили цвет титула!");
                player.getAppearance().setTitleColor(Integer.decode("0x"+newcolor));
				player.broadcastUserInfo();
				player.store();
				showServices(player);
                InventoryUpdate iu = new InventoryUpdate();
                if (destritem.getCount() == 0) iu.addRemovedItem(destritem);
                else iu.addModifiedItem(destritem);
                player.sendPacket(iu);
            }
            else
            {
                player.sendMessage("Ошибка!");
            }
		}
        else if (actualCommand.equalsIgnoreCase("Setclanname"))
		{
			if (st.countTokens() < 1) return;
			String newname = st.nextToken();
			if (!player.isClanLeader())
            {
                player.sendMessage("Вы не глава клана. Только глава клана может это сделать.");
                showServices(player);
                return;
            }
            if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < SETCLANNAME)
            {
                player.sendMessage("У Вас не достаточное кол-во монет для проведения операции.");
                showServices(player);
                return;
            }
            else if ((newname.length() < 3) || (newname.length() > 16))
            {
                player.sendMessage("Это имя не может быть использовано.");
                showServices(player);
                return;
            }
            else if (ClanTable.getInstance().getClanByName(newname) != null)
            {
                player.sendMessage("Это имя уже занято.");
                showServices(player);
                return;
            }
            L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Setclanname "+newname+" for "+player.getName(), ITEM_ID, SETCLANNAME, player, player);
            if (destritem != null)
            {
                player.getClan().setName(newname);
                player.getClan().updateClanInDB();
                player.getClan().broadcastClanStatus();
				player.sendMessage("Вы успешно сменили имя клана!");
				showServices(player);
                InventoryUpdate iu = new InventoryUpdate();
                if (destritem.getCount() == 0) iu.addRemovedItem(destritem);
                else iu.addModifiedItem(destritem);
                player.sendPacket(iu);
            }
            else
            {
                player.sendMessage("Ошибка!");
            }
		}
		
        else if (actualCommand.equalsIgnoreCase("Increaseclanlevel6"))
		{
			if (!player.isClanLeader())
            {
                player.sendMessage("Вы не глава клана. Только глава клана может это сделать.");
                showServices(player);
                return;
            }
            else if (player.getClan().getLevel() != 5)
            {
                player.sendMessage("У Вас не верный уровень клана. Можете только если уровень клана 5");
                showServices(player);
                return;
            }
            else if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < CLANLVL6)
            {
                player.sendMessage("У Вас не достаточное кол-во монет для проведения операции.");
                showServices(player);
                return;
            }
            L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Setclanlvl 6 for "+player.getName(), ITEM_ID, CLANLVL6, player, player);
            if (destritem != null)
            {
                player.getClan().changeLevel(6);
                player.sendMessage("Вы успешно повысили уровень клана!");
                showServices(player);
                InventoryUpdate iu = new InventoryUpdate();
                if (destritem.getCount() == 0) iu.addRemovedItem(destritem);
                else iu.addModifiedItem(destritem);
                player.sendPacket(iu);
            }
            else
            {
                player.sendMessage("Ошибка!");
            }
		}
        else if (actualCommand.equalsIgnoreCase("Increaseclanlevel7"))
		{
			if (!player.isClanLeader())
            {
                player.sendMessage("Вы не глава клана. Только глава клана может это сделать.");
                showServices(player);
                return;
            }
            else if (player.getClan().getLevel() != 6)
            {
                player.sendMessage("У Вас не верный уровень клана. Можете только если уровень клана 6");
                showServices(player);
                return;
            }
            else if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < CLANLVL7)
            {
                player.sendMessage("У Вас не достаточное кол-во монет для проведения операции.");
                showServices(player);
                return;
            }
            L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Setclanlvl 7 for "+player.getName(), ITEM_ID, CLANLVL7, player, player);
            if (destritem != null)
            {
                player.getClan().changeLevel(7);
                player.sendMessage("Вы успешно повысили уровень клана!");
                showServices(player);
                InventoryUpdate iu = new InventoryUpdate();
                if (destritem.getCount() == 0) iu.addRemovedItem(destritem);
                else iu.addModifiedItem(destritem);
                player.sendPacket(iu);
            }
            else
            {
                player.sendMessage("Ошибка!");
            }
		}
        else if (actualCommand.equalsIgnoreCase("Increaseclanlevel8"))
		{
			if (!player.isClanLeader())
            {
                player.sendMessage("Вы не глава клана. Только глава клана может это сделать.");
                showServices(player);
                return;
            }
            else if (player.getClan().getLevel() != 7)
            {
                player.sendMessage("У Вас не верный уровень клана. Можете только если уровень клана 7");
                showServices(player);
                return;
            }
            else if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < CLANLVL8)
            {
                player.sendMessage("У Вас не достаточное кол-во монет для проведения операции.");
                showServices(player);
                return;
            }
            L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Setclanlvl 8 for "+player.getName(), ITEM_ID, CLANLVL8, player, player);
            if (destritem != null)
            {
                player.getClan().changeLevel(8);
                player.sendMessage("Вы успешно повысили уровень клана!");
                showServices(player);

                InventoryUpdate iu = new InventoryUpdate();
                if (destritem.getCount() == 0) iu.addRemovedItem(destritem);
                else iu.addModifiedItem(destritem);
                player.sendPacket(iu);
            }
            else
            {
                player.sendMessage("Ошибка!");
            }
		}
        
		
		else if (actualCommand.equalsIgnoreCase("nobless"))
		{
			
			if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < NOBLESS)
            {
                player.sendMessage("У Вас не достаточное кол-во монет для проведения операции.");
                showServices(player);
                return;
            }
            L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: NOBLESS for "+player.getName(), ITEM_ID, NOBLESS, player, player);
            if (destritem != null)
            {
            	if(player.isNoble())
    			{
    				player.sendMessage("Вы уже ноблесс");
    				showServices(player);
    				return;
    			}
				player.sendMessage("Вы получили прохождения квестов для ноблесса! Поздравляем! Теперь Вы ноблесс!");
				player.setNoble(true);
				player.addItem(" GoldMerchant: NOBLESS", 7694, 1, this, true);
                InventoryUpdate iu = new InventoryUpdate();
                if (destritem.getCount() == 0) iu.addRemovedItem(destritem);
                else iu.addModifiedItem(destritem);
                player.sendPacket(iu);
            }
            else
            {
                player.sendMessage("Ошибка!");
                showServices(player);
            }
		}
		
		else if (actualCommand.equalsIgnoreCase("sex"))
		{
			if(player.getInventory().getItemByItemId(ITEM_ID) == null || player.getInventory().getItemByItemId(ITEM_ID).getCount() < SEX)
			{
				player.sendMessage("У Вас не достаточное кол-во монет для проведения операции");
				showServices(player);
				return;
			}
			player.destroyItemByItemId("Consume", ITEM_ID, SEX, player, false);
			player.getAppearance().setSex(player.getAppearance().getSex() ? false : true);
			player.decayMe();
			player.spawnMe(player.getX(), player.getY(), player.getZ());
			player.broadcastUserInfo();
			L2PcInstance.savePlayerSex(player, 1);
			player.sendMessage("Вы успешно сменили свой пол.");
			showServices(player);
		}
		
		else if (actualCommand.equalsIgnoreCase("pk"))
		{
			if(player.getInventory().getItemByItemId(ITEM_ID) != null && player.getInventory().getItemByItemId(ITEM_ID).getCount() >= PK)
			{
				int inipkKills = player.getPkKills();
				if(inipkKills == 0) 
				{
					player.sendMessage("У вас нет PK");
					return;
				}
				player.destroyItemByItemId("Consume", ITEM_ID, PK, player, false);
				player.setPkKills(0); 
				if (player.getKarma()>0)
				{
					player.setKarma(0);
				}
				player.sendPacket(new UserInfo(player));
				player.sendMessage("Ваш счетчик PK обнулен");
				showServices(player);				
			}
			else 
			{
				player.sendMessage("У Вас не достаточное кол-во монет для проведения операции");
			}
		}
		
		else if(actualCommand.startsWith("hero"))
		{
			setHero(player, Integer.parseInt(st.nextToken()));
		}
		else if (actualCommand.startsWith("usl"))
	      {
	    	  showServices(player);
	      }
		else if(actualCommand.startsWith("crp"))
		{
			
			   if (player.getClan() != null && (player.getObjectId() == player.getClan().getLeaderId()))
			   {
			   if(player.getInventory().getItemByItemId(CRP_ITEM_ID) != null && player.getInventory().getItemByItemId(CRP_ITEM_ID).getCount() >= Config.COL_CRP)
			   {

		  		    player.getClan().setReputationScore(player.getClan().getReputationScore()+Config.CRP_COUNT, true);
		  		    player.sendMessage("Ваш клан получил "+Config.CRP_COUNT+" очков репутации! Сделайте релог"); 
		  		    player.destroyItemByItemId("Consume", CRP_ITEM_ID, Config.COL_CRP, player, false);
		  		    showServices(player);
			   }
			   else 
			   {
					player.sendMessage("У Вас не достаточное кол-во монет для проведения операции");
					showServices(player);
			   }
			   }
			   else
			   {
				   player.sendMessage("Вы не являетесь лидером клана");
				   showServices(player);
			   }
		}
		else 
		{
			// this class dont know any other commands, let forward
			// the command to the parent class
			super.onBypassFeedback(player, command);
		}
	}

	/*@Override
	public void onActionShift(L2GameClient client)
	{
		L2PcInstance player = client.getActiveChar();
		if (player == null) return;
		player.sendPacket(new ActionFailed());
	}*/
	public void showServices(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		String file = "data/html/donate/"+getNpcId()+"-1.htm";
	    html.setFile(file);
        sendHtmlMessage(activeChar,html);
        activeChar.sendPacket(new ActionFailed());
		html.replace("%crpcount%", str+Config.CRP_COUNT);
		html.replace("%colcount%", str+Config.COL_CRP);
		html.replace("%5-6%", str+Config.COL_6LVL_CLAN);
		html.replace("%6-7%", str+Config.COL_7LVL_CLAN);
		html.replace("%7-8%", str+Config.COL_8LVL_CLAN);
		html.replace("%namechange%", str+Config.COL_CHANGENAME);
		html.replace("%namecolor%", str+Config.COL_NICKCOLOR);
		html.replace("%titlecolor%", str+Config.COL_TITLECOLOR);
		html.replace("%hero%", str+Config.COL_HERO);
		html.replace("%pk%", str+Config.COL_PK);
		html.replace("%sex%", str+Config.COL_SEX);
		html.replace("%nooble%", str+Config.COL_NOBLESSE);
	}
	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}
	public static Calendar finishtime = Calendar.getInstance();
	public static void addPremiumServices(int Day, String AccName)
	  {
	    Connection con = null;
	    try
	    {
	      
	      finishtime.setTimeInMillis(System.currentTimeMillis());
	      finishtime.set(13, 0);
	      finishtime.add(5, Day);

	      con = L2DatabaseFactory.getInstance().getConnection();
	      PreparedStatement statement = con.prepareStatement("UPDATE account_premium SET premium_service=?,enddate=? WHERE account_name=?");
	      statement.setInt(1, 1);
	      statement.setLong(2, finishtime.getTimeInMillis());
	      statement.setString(3, AccName);
	      statement.execute();
	      statement.close();
	    }
	    catch (SQLException e)
	    {
	      _log.info("PremiumService: Could not increase data");
	    }
	    finally
	    {
	      try
	      {
	        con.close();
	      }
	      catch (SQLException e)
	      {
	      }
	    }
	  }
	
	private void setHero(L2PcInstance player, int days)
	{
	   if(player.getInventory().getItemByItemId(ITEM_ID) != null && player.getInventory().getItemByItemId(ITEM_ID).getCount() >= days*HERO)
	   {
		if(days != 0 && days > 0)
  		  {
			if(player.isHero()) 
			{
				player.sendMessage("Вы уже герой");
				return;
			}
  		    Heroes.getInstance().addHero(player, days);
  		    player.sendMessage("Вы получили статус героя на "+days+" дней!");
  		  }
		else
		{
			player.sendMessage("Вы не ввели кол-во дней!");
		}
		player.destroyItemByItemId("Consume", ITEM_ID, days*HERO, player, false);
	   }
		else 
		{
			player.sendMessage("У Вас не достаточное кол-во монет для проведения операции");
		}
	}
	
}