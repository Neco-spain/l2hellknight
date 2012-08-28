package net.sf.l2j.gameserver.handler.usercommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class ClanWarsList
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 88, 89, 90 };

  public boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if ((id != COMMAND_IDS[0]) && (id != COMMAND_IDS[1]) && (id != COMMAND_IDS[2])) {
      return false;
    }
    L2Clan clan = activeChar.getClan();

    if (clan == null)
    {
      activeChar.sendMessage("You are not in a clan.");
      return false;
    }

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement;
      if (id == 88)
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.CLANS_YOU_DECLARED_WAR_ON));
        PreparedStatement statement = con.prepareStatement("select clan_name,clan_id,ally_id,ally_name from clan_data,clan_wars where clan1=? and clan_id=clan2 and clan2 not in (select clan1 from clan_wars where clan2=?)");
        statement.setInt(1, clan.getClanId());
        statement.setInt(2, clan.getClanId());
      }
      else if (id == 89)
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU));
        PreparedStatement statement = con.prepareStatement("select clan_name,clan_id,ally_id,ally_name from clan_data,clan_wars where clan2=? and clan_id=clan1 and clan1 not in (select clan2 from clan_wars where clan1=?)");
        statement.setInt(1, clan.getClanId());
        statement.setInt(2, clan.getClanId());
      }
      else
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.WAR_LIST));
        statement = con.prepareStatement("select clan_name,clan_id,ally_id,ally_name from clan_data,clan_wars where clan1=? and clan_id=clan2 and clan2 in (select clan1 from clan_wars where clan2=?)");
        statement.setInt(1, clan.getClanId());
        statement.setInt(2, clan.getClanId());
      }

      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        String clanName = rset.getString("clan_name");
        int ally_id = rset.getInt("ally_id");
        SystemMessage sm;
        if (ally_id > 0)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2_ALLIANCE);
          sm.addString(clanName);
          sm.addString(rset.getString("ally_name"));
        }
        else
        {
          sm = new SystemMessage(SystemMessageId.S1_NO_ALLI_EXISTS);
          sm.addString(clanName);
        }

        activeChar.sendPacket(sm);
      }

      activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOT));

      rset.close();
      statement.close();
    } catch (Exception e) {
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return true;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}