package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastTable;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public final class RequestSetAllyCrest extends L2GameClientPacket
{
  static Logger _log = Logger.getLogger(RequestSetAllyCrest.class.getName());
  private int _length;
  private byte[] _data;

  protected void readImpl()
  {
    _length = readD();
    if ((_length < 0) || (_length > 192)) {
      return;
    }
    _data = new byte[_length];
    readB(_data);
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPAB() < 1000L) {
      return;
    }
    player.sCPAB();

    if (_length < 0)
    {
      player.sendMessage("File transfer error.");
      return;
    }
    if (_length > 192)
    {
      player.sendMessage("The crest file size was too big (max 192 bytes).");
      return;
    }
    int newId;
    if (player.getAllyId() != 0)
    {
      L2Clan leaderclan = ClanTable.getInstance().getClan(player.getAllyId());

      if ((player.getClanId() != leaderclan.getClanId()) || (!player.isClanLeader()))
      {
        return;
      }

      CrestCache crestCache = CrestCache.getInstance();

      newId = IdFactory.getInstance().getNextId();

      if (!crestCache.saveAllyCrest(newId, _data))
      {
        _log.log(Level.INFO, "Error loading crest of ally:" + leaderclan.getAllyName());
        return;
      }

      if (leaderclan.getAllyCrestId() != 0)
      {
        crestCache.removeAllyCrest(leaderclan.getAllyCrestId());
      }

      Connect con = null;
      PreparedStatement statement = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        statement = con.prepareStatement("UPDATE clan_data SET ally_crest_id = ? WHERE ally_id = ?");
        statement.setInt(1, newId);
        statement.setInt(2, leaderclan.getAllyId());
        statement.executeUpdate();
      }
      catch (SQLException e)
      {
        _log.warning("could not update the ally crest id:" + e.getMessage());
      }
      finally
      {
        Close.CS(con, statement);
      }

      FastTable cn = new FastTable();
      cn.addAll(ClanTable.getInstance().getClans());
      for (L2Clan clan : cn)
      {
        if (clan.getAllyId() == player.getAllyId())
        {
          clan.setAllyCrestId(newId);
          for (L2PcInstance member : clan.getOnlineMembers(""))
            member.broadcastUserInfo();
        }
      }
    }
  }
}