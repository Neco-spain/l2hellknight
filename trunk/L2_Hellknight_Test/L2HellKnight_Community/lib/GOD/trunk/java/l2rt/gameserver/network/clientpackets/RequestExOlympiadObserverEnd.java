package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.olympiad.CompType;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.entity.olympiad.OlympiadGame;
import l2rt.gameserver.model.entity.olympiad.OlympiadManager;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.util.Strings;

/**
 * format ch
 * c: (id) 0xD0
 * h: (subid) 0x2F
 */
public class RequestExOlympiadObserverEnd extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(!activeChar.inObserverMode())
			return;

		NpcHtmlMessage reply = new NpcHtmlMessage(0);
		StringBuffer msg = new StringBuffer("");
		msg.append("!Grand Olympiad Game View:<br>");

		OlympiadManager manager = Olympiad._manager;
		if(manager != null)
			for(int i = 0; i < Olympiad.STADIUMS.length; i++)
			{
				OlympiadGame game = manager.getOlympiadInstance(i);
				if(game != null && game.getState() > 0)
				{
					if(game.getType() == CompType.TEAM || game.getType() == CompType.TEAM_RANDOM)
					{
						msg.append("<br1>Arena " + i + ":&nbsp;<a action=\"bypass -h oly_" + (i + 1) + "\">Team vs Team:</a>");
						msg.append("<br1>- " + game.getTeam1Title() + "<br1>- " + game.getTeam2Title());
					}
					else
						msg.append("<br1>Arena " + i + ":&nbsp;<a action=\"bypass -h oly_" + (i + 1) + "\">" + manager.getOlympiadInstance(i).getTitle() + "</a>");
					msg.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
				}
			}

		reply.setHtml(Strings.bbParse(msg.toString()));
		activeChar.sendPacket(reply);
	}
}