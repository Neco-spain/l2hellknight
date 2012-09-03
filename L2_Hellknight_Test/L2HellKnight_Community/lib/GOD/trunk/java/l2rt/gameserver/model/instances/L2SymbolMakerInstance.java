package l2rt.gameserver.model.instances;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.HennaEquipList;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.tables.HennaTreeTable;
import l2rt.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 *
 * @version $Revision$ $Date$
 */
public class L2SymbolMakerInstance extends L2NpcInstance
{

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equals("Draw"))
		{
			L2HennaInstance[] henna = HennaTreeTable.getInstance().getAvailableHenna(player.getClassId(), player.getSex());
			HennaEquipList hel = new HennaEquipList(player, henna);
			player.sendPacket(hel);
		}
		else if(command.equals("RemoveList"))
			showRemoveChat(player);
		else if(command.startsWith("Remove "))
		{
			int slot = Integer.parseInt(command.substring(7));
			player.removeHenna(slot);
		}
		else
			super.onBypassFeedback(player, command);
	}

	private void showRemoveChat(L2Player player)
	{
		StringBuffer html1 = new StringBuffer("<html><body>");
		html1.append("Select symbol you would like to remove:<br><br>");
		boolean hasHennas = false;
		for(int i = 1; i <= 3; i++)
		{
			L2HennaInstance henna = player.getHenna(i);
			if(henna != null)
			{
				hasHennas = true;
				html1.append("<a action=\"bypass -h npc_%objectId%_Remove " + i + "\">" + henna.getName() + "</a><br>");
			}
		}
		if(!hasHennas)
			html1.append("You don't have any symbol to remove!");
		html1.append("</body></html>");

		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setHtml(html1.toString());
		player.sendPacket(html);
	}

	public L2SymbolMakerInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "data/html/symbolmaker/" + pom + ".htm";
	}
}