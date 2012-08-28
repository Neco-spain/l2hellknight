package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;



public class L2GmShopInstance extends L2FolkInstance
{
	private String _curHtm = null;

    public L2GmShopInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		 
    }
    
    @Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom;

		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "data/html/gmshop/" + pom + ".htm";
	}
    
    public void onBypassFeedback(L2PcInstance player, String command)
    {
    	StringTokenizer st = new StringTokenizer(command, " ");
    	String cmd = st.nextToken(); //получаем команду
    	
    	if (cmd.startsWith("chat"))
    	{
    		String file = "data/html/gmshop/"+getNpcId()+".htm";
    		int cmdChoice = Integer.parseInt(command.substring(5,7).trim());
    		if(cmdChoice>0)
			{
				file = "data/html/gmshop/"+getNpcId()+"-"+cmdChoice+".htm";
			}
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            _curHtm = file;
		    html.setFile(file);
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
    	}
    	else if (cmd.startsWith("cfpa"))
    	{
    		if(player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null || player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_3)
			{
				player.sendMessage("Не хватает монет");
	            NpcHtmlMessage html = new NpcHtmlMessage(1);
			    html.setFile(_curHtm);
				return;
			}
    		player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_3, player, false);
    		//Plate
    		player.getInventory().addItem("ItemsOnCreate", 356, 1, player, null);
    		//boots
    		player.getInventory().addItem("ItemsOnCreate", 2438, 1, player, null);
    		//gloves
    		player.getInventory().addItem("ItemsOnCreate", 2462, 1, player, null);
    		//helmet
    		player.getInventory().addItem("ItemsOnCreate", 2414, 1, player, null);
    		player.sendMessage("Вы получили FPA сет");
            NpcHtmlMessage html = new NpcHtmlMessage(1);
		    html.setFile(_curHtm);
            sendHtmlMessage(player,html);
		    player.sendPacket(new ActionFailed());
		    player.getInventory().updateDatabase();
    	}
    	

        
    }
    
    private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}
	
}