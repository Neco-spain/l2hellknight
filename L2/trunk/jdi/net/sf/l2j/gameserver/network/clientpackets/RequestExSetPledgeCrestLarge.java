package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestExSetPledgeCrestLarge extends L2GameClientPacket
{
  private static final String _C__D0_11_REQUESTEXSETPLEDGECRESTLARGE = "[C] D0:11 RequestExSetPledgeCrestLarge";
  static Logger _log = Logger.getLogger(RequestExSetPledgeCrestLarge.class.getName());
  private int _size;
  private byte[] _data;

  protected void readImpl()
  {
    _size = readD();
    if (_size > 2176)
      return;
    if (_size > 0)
    {
      _data = new byte[_size];
      readB(_data);
    }
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    L2Clan clan = activeChar.getClan();
    if (clan == null) return;

    if (_data == null)
    {
      CrestCache.getInstance().removePledgeCrestLarge(clan.getCrestId());

      clan.setHasCrestLarge(false);
      activeChar.sendMessage("The insignia has been removed.");

      for (L2PcInstance member : clan.getOnlineMembers("")) {
        member.broadcastUserInfo();
      }
      return;
    }

    if (_size > 2176)
    {
      activeChar.sendMessage("The insignia file size is greater than 2176 bytes.");
      return;
    }

    if ((activeChar.getClanPrivileges() & 0x80) == 128)
    {
      if ((clan.getHasCastle() == 0) && (clan.getHasHideout() == 0))
      {
        activeChar.sendMessage("Only a clan that owns a clan hall or a castle can get their emblem displayed on clan related items");
        return;
      }

      CrestCache crestCache = CrestCache.getInstance();

      int newId = IdFactory.getInstance().getNextId();

      if (!crestCache.savePledgeCrestLarge(newId, _data))
      {
        _log.log(Level.INFO, "Error loading large crest of clan:" + clan.getName());
        return;
      }

      if (clan.hasCrestLarge())
      {
        crestCache.removePledgeCrestLarge(clan.getCrestLargeId());
      }

      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_large_id = ? WHERE clan_id = ?");
        statement.setInt(1, newId);
        statement.setInt(2, clan.getClanId());
        statement.executeUpdate();
        statement.close();
      }
      catch (SQLException e)
      {
        _log.warning("could not update the large crest id:" + e.getMessage());
      }
      finally {
        try {
          con.close(); } catch (Exception e) {
        }
      }
      clan.setCrestLargeId(newId);
      clan.setHasCrestLarge(true);

      activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED));

      for (L2PcInstance member : clan.getOnlineMembers(""))
        member.broadcastUserInfo();
    }
  }

  public String getType()
  {
    return "[C] D0:11 RequestExSetPledgeCrestLarge";
  }
}