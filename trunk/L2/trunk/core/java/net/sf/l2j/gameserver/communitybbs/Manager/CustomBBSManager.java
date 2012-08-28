package net.sf.l2j.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.events.Heroes;
import net.sf.l2j.gameserver.network.clientpackets.MultiSellChoose;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;


public class CustomBBSManager extends BaseBBSManager
{
	private static int HERO = Config.COL_HERO;
	public static int ITEM_ID = Config.DON_ITEM_ID;
	public static HtmCache _hc = HtmCache.getInstance();
	MultiSellChoose multisellchose = new MultiSellChoose();

	@SuppressWarnings("unused")
	@Override
	public void parsecmd(String command, L2PcInstance activeChar) 
	{
		  String content;
	      String[] tmp;
	      if (command.startsWith("_bbsmultisell"))
	      {
	        tmp = command.substring(14).split(" ");
	        L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(tmp[1]), activeChar, false, 1);

	        content = getSwHtm(tmp[0]);
	        if (content == null)
	        {
	          content = "<html><body><br><br><center>Страница: " + tmp[0] + ".htm не найдена.</center></body></html>";
	        }
	        separateAndSend(content,activeChar);
	      }
	      else if (command.startsWith("_bbsteleto"))
	      {
  
    		content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/soft/41001.htm");
	        tmp = command.substring(11).trim().split("_");
	        int type = Integer.parseInt(tmp[0]);
	        int x = Integer.parseInt(tmp[1]);
	        int y = Integer.parseInt(tmp[2]);
	        int z = Integer.parseInt(tmp[3]);
	        separateAndSend(content,activeChar);
	        activeChar.teleToLocation(x, y, z, false);
	      }
			else if(command.startsWith("_bbshero"))
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				setHero(activeChar,Integer.parseInt(st.nextToken()));
				return;
			}
			else if(command.startsWith("_bbscolor"))
			{
				content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/soft/400081.htm");
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
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
			
					activeChar.sendMessage("Вы успешно изменили цвет имени!");
	                activeChar.getAppearance().setNameColor(Integer.decode("0x"+newcolor));
					activeChar.broadcastUserInfo();
					activeChar.store();     
					separateAndSend(content,activeChar);
	          return;
			}      
			else if(command.startsWith("_bbstitlecolor"))
			{
				content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/soft/400082.htm");
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
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
			
					activeChar.sendMessage("Вы успешно изменили цвет Титула");
	                activeChar.getAppearance().setTitleColor(Integer.decode("0x"+newcolor));
					activeChar.broadcastUserInfo();
					activeChar.store();  
					separateAndSend(content,activeChar);
	          return;
			}
	      
			else if(command.startsWith("_bbsmult;"))
			{
				StringTokenizer st = new StringTokenizer(command, ";");
				st.nextToken();
				int idp = Integer.parseInt(st.nextToken());
				 content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/soft/"+idp+".htm");
				if (content == null)
				{
					content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/buff/"+idp+".htm' </center></body></html>";
				}
			
				separateAndSend(content,activeChar);
			}
			else
			{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: "+command+" is not implemented yet</center><br><br></body></html>","101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null,"102"));
			activeChar.sendPacket(new ShowBoard(null,"103"));
			}
		
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4,
			String ar5, L2PcInstance activeChar) {
		// TODO Auto-generated method stub
		
	}
	  public static String getSwHtm(String page)
	  {
	    return _hc.getHtm("data/html/CommunityBoard/soft/" + page + ".htm");
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

		private static CustomBBSManager _instance = new CustomBBSManager();

		/**
		 * @return
		 */
		public static CustomBBSManager getInstance()
		{
			return _instance;
		}
	
}
