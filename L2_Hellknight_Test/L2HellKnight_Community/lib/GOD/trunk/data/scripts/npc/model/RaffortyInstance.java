package npc.model;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Files;

/**
 * @author DarkShadow74
 *         AI for Rafforty
 */

public class RaffortyInstance extends L2NpcInstance implements ScriptFile
{
	public static final int Freya_Necklace = 16025;
	public static final int Blessed_Freya_Necklace = 16026;
	public static final int Bottle_Of_Freya_Soul = 16027;
	
	public RaffortyInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;
		
		if(command.startsWith("ask"))
		{
			int val = Integer.parseInt(command.substring(4));
			L2Item item1 = ItemTemplates.getInstance().getTemplate(Freya_Necklace);
			L2Item item2 = ItemTemplates.getInstance().getTemplate(Bottle_Of_Freya_Soul);
			L2ItemInstance Necklace = player.getInventory().getItemByItemId(item1.getItemId());
			L2ItemInstance Bottle = player.getInventory().getItemByItemId(item2.getItemId());
			switch(val)
			{
				case 1:
					if(Necklace != null && Necklace.getCount() > 0)
			    		showHtmlFile(player, "32020-004.htm");
					else
			    		showHtmlFile(player, "32020-006.htm");
					break;
				case 2:
					if(Bottle != null && Bottle.getCount() > 0)
			    		showHtmlFile(player, "32020-008.htm");
					else
			    		showHtmlFile(player, "32020-007.htm");
					break;
				case 3:
				    if(Necklace != null && Bottle != null)
					{
		    			if(Necklace.getCount() > 0 &&  Bottle.getCount() > 0)
		    			{
	    		    		Functions.removeItem(player, Freya_Necklace, 1);
	    		    		Functions.removeItem(player, Bottle_Of_Freya_Soul, 1);
	    		    		Functions.addItem(player, Blessed_Freya_Necklace, 1);
		    			}
		    			else
	    		    		showHtmlFile(player, "32020-011.htm");
					}
					break;
				case 4:
			    	showHtmlFile(player, "32020-005.htm");
					break;
				case 5:
			    	showHtmlFile(player, "32020-010.htm");
					break;
				default:
					showHtmlFile(player, "32020-003.htm");
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	public void showHtmlFile(L2Player player, String file)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		
		html.setFile("data/html/default/" + file);
			
		player.sendPacket(html);
	}

	@Override
	public void showChatWindow(L2Player player, int val)
	{
		final QuestState q10285 = player.getQuestState("_10285_MeetingSirra");
		String html;
		
		if(q10285 != null && q10285.isCompleted())
		{
	    	html = Files.read("data/html/default/32020-003.htm", player);
		}
		else
		{
	    	html = Files.read("data/html/default/32020.htm", player);
		}
		
		player.sendPacket(new NpcHtmlMessage(player, this, html, val));
	}
}