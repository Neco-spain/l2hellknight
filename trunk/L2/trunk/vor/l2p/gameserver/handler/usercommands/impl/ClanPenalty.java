package l2p.gameserver.handler.usercommands.impl;

import java.text.SimpleDateFormat;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.handler.usercommands.IUserCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.utils.Strings;

public class ClanPenalty
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 100, 114 };

  public boolean useUserCommand(int id, Player activeChar)
  {
    if (COMMAND_IDS[0] != id) {
      return false;
    }
    long leaveClan = 0L;
    if (activeChar.getLeaveClanTime() != 0L)
      leaveClan = activeChar.getLeaveClanTime() + 86400000L;
    long deleteClan = 0L;
    if (activeChar.getDeleteClanTime() != 0L)
      deleteClan = activeChar.getDeleteClanTime() + 864000000L;
    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
    String html = HtmCache.getInstance().getNotNull("command/penalty.htm", activeChar);

    if (activeChar.getClanId() == 0)
    {
      if ((leaveClan == 0L) && (deleteClan == 0L))
      {
        html = html.replaceFirst("%reason%", "No penalty is imposed.");
        html = html.replaceFirst("%expiration%", " ");
      }
      else if ((leaveClan > 0L) && (deleteClan == 0L))
      {
        html = html.replaceFirst("%reason%", "Penalty for leaving clan.");
        html = html.replaceFirst("%expiration%", format.format(Long.valueOf(leaveClan)));
      }
      else if (deleteClan > 0L)
      {
        html = html.replaceFirst("%reason%", "Penalty for dissolving clan.");
        html = html.replaceFirst("%expiration%", format.format(Long.valueOf(deleteClan)));
      }
    }
    else if (activeChar.getClan().canInvite())
    {
      html = html.replaceFirst("%reason%", "No penalty is imposed.");
      html = html.replaceFirst("%expiration%", " ");
    }
    else
    {
      html = html.replaceFirst("%reason%", "Penalty for expelling clan member.");
      html = html.replaceFirst("%expiration%", format.format(Long.valueOf(activeChar.getClan().getExpelledMemberTime())));
    }

    NpcHtmlMessage msg = new NpcHtmlMessage(5);
    msg.setHtml(Strings.bbParse(html));
    activeChar.sendPacket(msg);
    return true;
  }

  public final int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}