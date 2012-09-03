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
import bosses.FreyaManager;

/**
 * @author DarkShadow74
 *         AI for Jinia
 */

public class L2JiniaNpcInstance extends L2NpcInstance implements ScriptFile
{
	private static final int ICE_QUEEN_CASTLE = 139; //Ice Queen's Castle
	private static final int ICE_QUEEN_CASTLE_ULTIMATE = 144; //Ice Queen's Castle (ultimate battle)
	private static final int FrozenCore = 15469;
	private static final int BlackFrozenCore = 15470;
	
	public L2JiniaNpcInstance(int objectId, L2NpcTemplate template)
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
			QuestState q10286 = player.getQuestState("_10286_ReunionwithSirra");
			switch(val)
			{
				case 1:
					FreyaManager.enterInstance(player, ICE_QUEEN_CASTLE, "data/html/default/32781-07.htm");
				    if(q10286 == null || q10286.getCond() != 5)
					    return;
					else
					{
	    				q10286.setCond(6);
						q10286.playSound("ItemSound.quest_middle");
					}
					break;
				case 2:
					//Notdone Yet
					//Timer On Freya Ultimate Start TODO
	    			/**FreyaManager.enterInstance(player, ICE_QUEEN_CASTLE_ULTIMATE, "data/html/default/32781-07.htm"); */
					break;
				case 3:
				    L2Item item1 = ItemTemplates.getInstance().getTemplate(FrozenCore);
				    L2Item item2 = ItemTemplates.getInstance().getTemplate(BlackFrozenCore);
				    L2ItemInstance fCore = player.getInventory().getItemByItemId(item1.getItemId());
				    L2ItemInstance BlackFCore = player.getInventory().getItemByItemId(item2.getItemId());
					if(fCore != null && fCore.getCount() >= 1 || BlackFCore != null && BlackFCore.getCount() >= 1)
					{
			    		showHtmlFile(player, "32781-09.htm");
						return;
					}
					if(q10286 == null || !q10286.isCompleted())
					{
			    		showHtmlFile(player, "32781-08.htm");
						Functions.addItem(player, BlackFrozenCore, 1);
					}
					else
					{
			    		showHtmlFile(player, "32781-08.htm");
						Functions.addItem(player, FrozenCore, 1);
					}
					break;
				case 4:
			    	showHtmlFile(player, "32781-03.htm");
					break;
				case 5:
			    	showHtmlFile(player, "32781-04.htm");
					break;
				case 6:
			    	showHtmlFile(player, "32781-05.htm");
					break;
				case 7:
			    	showHtmlFile(player, "32781-06.htm");
					break;
				default:
					showChatWindow(player, 0);
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
		if(player.getLevel() < 82)
		{
	    	showHtmlFile(player, "32781.htm");
		}
		else
		{
	    	showHtmlFile(player, "32781-02.htm");
		}
	}
}