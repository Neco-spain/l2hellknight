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

public final class RequestSetPledgeCrest extends L2GameClientPacket
{
  private static final String _C__53_REQUESTSETPLEDGECREST = "[C] 53 RequestSetPledgeCrest";
  static Logger _log = Logger.getLogger(RequestSetPledgeCrest.class.getName());
  private int _length;
  private byte[] _data;

  protected void readImpl()
  {
    _length = readD();
    if ((_length < 0) || (_length > 256)) {
      return;
    }
    _data = new byte[_length];
    readB(_data);
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    L2Clan clan = activeChar.getClan();
    if (clan == null) {
      return;
    }
    if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS));
      return;
    }

    if (_length < 0)
    {
      activeChar.sendMessage("File transfer error.");
      return;
    }
    if (_length > 256)
    {
      activeChar.sendMessage("The clan crest file size was too big (max 256 bytes).");
      return;
    }
    if ((_length == 0) || (_data.length == 0))
    {
      CrestCache.getInstance().removePledgeCrest(clan.getCrestId());

      clan.setHasCrest(false);
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED));

      for (L2PcInstance member : clan.getOnlineMembers("")) {
        member.broadcastUserInfo();
      }
      return;
    }

    if ((activeChar.getClanPrivileges() & 0x80) == 128)
    {
      if (clan.getLevel() < 3)
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_LVL_3_NEEDED_TO_SET_CREST));
        return;
      }

      CrestCache crestCache = CrestCache.getInstance();

      int newId = IdFactory.getInstance().getNextId();

      if (clan.hasCrest())
      {
        crestCache.removePledgeCrest(newId);
      }

      if (!crestCache.savePledgeCrest(newId, _data))
      {
        _log.log(Level.INFO, "Error loading crest of clan:" + clan.getName());
        return;
      }

      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?");
        statement.setInt(1, newId);
        statement.setInt(2, clan.getClanId());
        statement.executeUpdate();
        statement.close();
      }
      catch (SQLException e)
      {
        _log.warning("could not update the crest id:" + e.getMessage());
      }
      finally {
        try {
          con.close(); } catch (Exception e) {
        }
      }
      clan.setCrestId(newId);
      clan.setHasCrest(true);

      for (L2PcInstance member : clan.getOnlineMembers(""))
        member.broadcastUserInfo();
    }
  }

  public String getType()
  {
    return "[C] 53 RequestSetPledgeCrest";
  }
}