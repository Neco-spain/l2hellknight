package net.sf.l2j.gameserver.model.actor.instance;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2NpcKillerInstance extends L2FolkInstance
{
    public L2NpcKillerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		 
    }
    
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

		return "data/html/killer/" + pom + ".htm";
	}
    
    @Override
    public void onBypassFeedback(L2PcInstance activeChar, String command)
    {
    	String player = "";
       	
    	StringTokenizer st = new StringTokenizer(command);
    	st.nextToken(); //получаем команду
    	String addcmd = command.substring(4).trim();	 
    			 if (addcmd.startsWith("kill"))
    				{
    					
    					try
    					{
    						player = st.nextToken();
    						L2PcInstance playerObj = L2World.getInstance().getPlayer(player);

    						if (playerObj != null)
    						{
    							kill(activeChar, playerObj);
    							activeChar.setKarma(0);
        		  	    		NpcHtmlMessage html = new NpcHtmlMessage(1);
        		  	    		html.setFile("data/html/killer/9437.htm");
        		  	    		sendHtmlMessage(activeChar,html);
        		  	    		activeChar.sendPacket(new ActionFailed());
    						}
    						else
    						{
    							activeChar.sendMessage("Персонажа нет в игре");
    						}
    					}
    					catch (NoSuchElementException nsee)
    					{
    						activeChar.sendMessage("Specify a character name.");
    					}
    					catch (Exception e)
    					{
    					}
    					GMAudit.auditGMAction(activeChar.getName(), command, player, "");
    					
    				}
    	
	
    }
    
	private void kill(L2PcInstance activeChar, L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			if(!((L2PcInstance)target).isGM())
				target.stopAllEffects(); 
			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
		}
		else
		{
			target.reduceCurrentHp(target.getMaxHp() + 1, activeChar);
		}

	}
    private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}
}