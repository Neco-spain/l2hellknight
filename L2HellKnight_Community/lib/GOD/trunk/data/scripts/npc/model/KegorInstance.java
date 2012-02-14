package npc.model;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.model.L2CommandChannel;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.network.serverpackets.ExStartScenePlayer;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author DarkShadow74
 *         AI for Kegor
 */

public class KegorInstance extends L2NpcInstance implements ScriptFile
{	
	public KegorInstance(int objectId, L2NpcTemplate template)
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
			
		L2CommandChannel cc = player.getParty().getCommandChannel();
		final Reflection r = cc.getReflection();
		
		if(command.startsWith("ask"))
		{
			int val = Integer.parseInt(command.substring(4));
			switch(val)
			{
				case 1:
				    QuestState qs = player.getQuestState("_10286_ReunionwithSirra");
               		if(cc.getChannelLeader() == player)
            		{
						if(qs != null || !qs.isCompleted())
						{
    						qs.setCond(7);
    						qs.playSound("ItemSound.quest_middle");
						}
    					for(L2Player pl : r.getPlayers())
    	            		pl.showQuestMovie(ExStartScenePlayer.SCENE_SC_BOSS_FREYA_ENDING_B);
						
     	    			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() 
    	    			{
    		    			@Override
    		     			public void run() 
    		    			{
    		    				r.collapse();
    		    			}
    		    		}, 58000);
            		}
					else
					{
			    		showHtmlFile(player, "18846-01.htm");
					}
					break;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	public void showHtmlFile(L2Player player, String file)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		if(!player.getLang().equalsIgnoreCase("ru"))
	    	html.setFile("data/html/default/" + file);
		else
	    	html.setFile("data/html-ru/default/" + file);
			
		player.sendPacket(html);
	}

	@Override
	public void showChatWindow(L2Player player, int val)
	{
        for(L2NpcInstance freya : L2World.getAroundNpc(this, 3000, 3000))
            if(freya.getNpcId() == 29179 || freya.getNpcId() == 29180)
            {
                if(!freya.isDead())
                    return;
				showChatWindow(player, val);
            }
	}
}