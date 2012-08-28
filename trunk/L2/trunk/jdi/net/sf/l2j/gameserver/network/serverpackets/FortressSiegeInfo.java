package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Calendar;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.model.entity.FortSiege;
import net.sf.l2j.gameserver.network.L2GameClient;

public class FortressSiegeInfo extends L2GameServerPacket
{
  private static final String _S__C9_SIEGEINFO = "[S] c9 SiegeInfo";
  private static Logger _log = Logger.getLogger(FortressSiegeInfo.class.getName());
  private Fort _fort;

  public FortressSiegeInfo(Fort fort)
  {
    _fort = fort;
  }

  protected final void writeImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    writeC(201);
    writeD(_fort.getFortId());
    writeD((_fort.getOwnerId() == activeChar.getClanId()) && (activeChar.isClanLeader()) ? 1 : 0);
    writeD(_fort.getOwnerId());
    if (_fort.getOwnerId() > 0)
    {
      L2Clan owner = ClanTable.getInstance().getClan(_fort.getOwnerId());
      if (owner != null)
      {
        writeS(owner.getName());
        writeS(owner.getLeaderName());
        writeD(owner.getAllyId());
        writeS(owner.getAllyName());
      }
      else
      {
        _log.warning("Null owner for fort: " + _fort.getName());
      }
    }
    else
    {
      writeS("NPC");
      writeS("");
      writeD(0);
      writeS("");
    }

    writeD((int)(Calendar.getInstance().getTimeInMillis() / 1000L));
    writeD((int)(_fort.getSiege().getSiegeDate().getTimeInMillis() / 1000L));
    writeD(0);
  }

  public String getType()
  {
    return "[S] c9 SiegeInfo";
  }
}