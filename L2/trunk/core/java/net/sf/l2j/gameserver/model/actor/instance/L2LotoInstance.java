package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Random;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
// 41101-1 = ok.htm
// 41101-5 = ok.htm
// 41101-2 = no.htm
// 41101-3 = win.htm
// 41101-4 = lose.htm
public class L2LotoInstance extends L2FolkInstance
{
	int RND;
	@SuppressWarnings("unused")
	private String _curHtm = null;
	
	 @Override
		public String getHtmlPath(int npcId, int val)
		{
			String pom;

			if (val == 0)
				pom = "" + npcId;
			else
				pom = npcId + "-" + val;

			return "data/html/l2js/loto/" + pom + ".htm";
		}
	 
	 @Override
	 public void onBypassFeedback(L2PcInstance player, String command)
	 {
	    	StringTokenizer st = new StringTokenizer(command, " ");
	    	String cmd = st.nextToken(); //получаем команду
	    	if (cmd.startsWith("chat"))
	    	{
	    		String file = "data/html/l2js/loto/"+getNpcId()+".htm";
	    		int cmdChoice = Integer.parseInt(command.substring(5,7).trim());
	    		if(cmdChoice>0)
				{
					file = "data/html/l2js/loto/"+getNpcId()+"-"+cmdChoice+".htm";
				}
	            NpcHtmlMessage html = new NpcHtmlMessage(1);
	            _curHtm = file;
			    html.setFile(file);
	            sendHtmlMessage(player,html);
			    player.sendPacket(new ActionFailed());
	    	}
	    	if (cmd.startsWith("loto1"))
	    	{
	    		//ѕровер€ем есть ли у игрока монеты
	    		if(player.getInventory().getItemByItemId(Config.STAWKAID_COIN) == null || player.getInventory().getItemByItemId(Config.STAWKAID_COIN).getCount() < Config.STAWKA_COIN_AMOUNT)
				{
					player.sendMessage("Ќе хватает монет");
		            NpcHtmlMessage html = new NpcHtmlMessage(1);
		            html.setFile("data/html/l2js/loto/"+getNpcId()+".htm");
		            sendHtmlMessage(player,html);
					return;
				}
	    		NpcHtmlMessage html = new NpcHtmlMessage(1);
			    html.setFile("data/html/l2js/loto/41101-1.htm"); // говорим игроку что ставка прин€та и предлогаем бросить кости
	            sendHtmlMessage(player,html);
			    player.sendPacket(new ActionFailed());
			   
			    
	    	}
	    	if (cmd.startsWith("loto2"))
	    	{
	    		//ѕровер€ем есть ли у игрока монеты
	    		if(player.getInventory().getItemByItemId(Config.STAWKAID_ADENA) == null || player.getInventory().getItemByItemId(Config.STAWKAID_ADENA).getCount() < Config.STAWKA_ADENA_AMOUNT)
				{
					player.sendMessage("Ќе хватает монет");
		            NpcHtmlMessage html = new NpcHtmlMessage(1);
		            html.setFile("data/html/l2js/loto/"+getNpcId()+".htm");
		            sendHtmlMessage(player,html);
					return;
				}
	    		NpcHtmlMessage html = new NpcHtmlMessage(1);
			    html.setFile("data/html/l2js/loto/41101-5.htm"); // говорим игроку что ставка прин€та и предлогаем бросить кости
	            sendHtmlMessage(player,html);
			    player.sendPacket(new ActionFailed());
			   
			    
	    	}
	    	
	    	if (cmd.startsWith("loto3"))
	    	{
	    		
            	Random random = new Random();
            	
            	RND = random.nextInt(100); // получаем случайный ответ с шансом 50/50
	    		if(player.getInventory().getItemByItemId(Config.STAWKAID_COIN) == null || player.getInventory().getItemByItemId(Config.STAWKAID_COIN).getCount() < Config.STAWKA_COIN_AMOUNT)
				{
					player.sendMessage("Ќе хватает монет");
		            NpcHtmlMessage html = new NpcHtmlMessage(1);
		            html.setFile("data/html/l2js/loto/"+getNpcId()+".htm");
		            sendHtmlMessage(player,html);
					return;
				}
            	
            	if (RND<Config.CHANCE_WIN)
            	{  		
            		player.addItem("ItemsOnCreate", Config.WIN_COIN, Config.WIN_COIN_AMOUNT, this, true);
            		player.getInventory().updateDatabase();
    	    		NpcHtmlMessage html = new NpcHtmlMessage(1);
    			    html.setFile("data/html/l2js/loto/41101-3.htm"); // говорим игроку что он выиграл
    	            sendHtmlMessage(player,html);
    			    player.sendPacket(new ActionFailed());
            		return;
            	}
            	else
            	{
            		
            		player.destroyItemByItemId("Consume", Config.STAWKAID_COIN, Config.STAWKA_COIN_AMOUNT, player, true); // забираем из инвентар€ монеты
            		NpcHtmlMessage html = new NpcHtmlMessage(1);
    			    html.setFile("data/html/l2js/loto/41101-4.htm"); // говорим игроку что ѕроиграл
    	            sendHtmlMessage(player,html);
    			    player.sendPacket(new ActionFailed());
            		return;
            	}
            	
	    	}
	    	if (cmd.startsWith("loto4"))
	    	{
	    		
            	Random random = new Random();
	    		if(player.getInventory().getItemByItemId(Config.STAWKAID_ADENA) == null || player.getInventory().getItemByItemId(Config.STAWKAID_ADENA).getCount() < Config.STAWKA_ADENA_AMOUNT)
				{
					player.sendMessage("Ќе хватает монет");
		            NpcHtmlMessage html = new NpcHtmlMessage(1);
		            html.setFile("data/html/l2js/loto/"+getNpcId()+".htm");
		            sendHtmlMessage(player,html);
					return;
				}
            	
	    		RND = random.nextInt(100); // получаем случайный ответ с шансом 50/50
            	
            	if (RND<Config.CHANCE_WIN)
            	{  		
            		player.addItem("ItemsOnCreate", Config.WIN_ADENA, Config.WIN_ADENA_AMOUNT, this, true);
            		player.getInventory().updateDatabase();
    	    		NpcHtmlMessage html = new NpcHtmlMessage(1);
    			    html.setFile("data/html/l2js/loto/41101-3.htm"); // говорим игроку что он выиграл
    	            sendHtmlMessage(player,html);
    			    player.sendPacket(new ActionFailed());
            		return;
            	}
            	else
            	{
            		
            		player.destroyItemByItemId("Consume", Config.STAWKAID_ADENA, Config.STAWKA_ADENA_AMOUNT, player, true); // забираем из инвентар€ монеты
            		NpcHtmlMessage html = new NpcHtmlMessage(1);
    			    html.setFile("data/html/l2js/loto/41101-4.htm"); // говорим игроку что ѕроиграл
    	            sendHtmlMessage(player,html);
    			    player.sendPacket(new ActionFailed());
            		return;
            	}
            	
	    	}
	}
	
	
	
	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}
	
    public L2LotoInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
    }
}