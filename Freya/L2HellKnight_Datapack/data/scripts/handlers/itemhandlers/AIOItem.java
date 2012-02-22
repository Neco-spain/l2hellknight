package handlers.itemhandlers;

import java.util.logging.Logger;

import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.datatables.AIOItemTable;
import l2.hellknight.gameserver.handler.IItemHandler;
import l2.hellknight.gameserver.model.L2ItemInstance;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;

public class AIOItem implements IItemHandler 
{
	private static final Logger _log = Logger.getLogger(AIOItem.class.getName());
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse) 
	{
		/*
		 * Null pointer check
		 */
		if(playable == null)
		{
			return;
		}
		/*
		 * Only players can use it
		 */
		if(playable instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)playable;
			
			/*
			 * Minumun requirements to use it
			 */
			if(!AIOItemTable.getInstance().checkPlayerConditions(player))
			{
				return;
			}
			
			String html = HtmCache.getInstance().getHtm(null, "data/html/aioitem/main.htm");
			
			if(html == null)
			{
				_log.severe("AIOItem: The main file [data/html/aioitem/main.htm] does not exist or is corrupted!");
				return;
			}
			
			NpcHtmlMessage msg = new NpcHtmlMessage(5);
			msg.setHtml(html);
			player.sendPacket(msg);
		}
	}
}