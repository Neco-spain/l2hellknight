package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Calendar;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.L2GameClient;

public class SiegeInfo extends L2GameServerPacket
{
  private static final String _S__C9_SIEGEINFO = "[S] c9 SiegeInfo";
  private static Logger _log = Logger.getLogger(SiegeInfo.class.getName());
  private Castle _castle;

  public SiegeInfo(Castle castle)
  {
    _castle = castle;
  }

  protected final void writeImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    writeC(201);
    writeD(_castle.getCastleId());
    writeD((_castle.getOwnerId() == activeChar.getClanId()) && (activeChar.isClanLeader()) ? 1 : 0);
    writeD(_castle.getOwnerId());
    if (_castle.getOwnerId() > 0)
    {
      L2Clan owner = ClanTable.getInstance().getClan(_castle.getOwnerId());
      if (owner != null)
      {
        writeS(owner.getName());
        writeS(owner.getLeaderName());
        writeD(owner.getAllyId());
        writeS(owner.getAllyName());
      }
      else {
        _log.warning("Null owner for castle: " + _castle.getName());
      }
    }
    else {
      writeS("NPC");
      writeS("");
      writeD(0);
      writeS("");
    }

    writeD((int)(Calendar.getInstance().getTimeInMillis() / 1000L));
    writeD((int)(_castle.getSiege().getSiegeDate().getTimeInMillis() / 1000L));
    writeD(0);
  }

  public String getType()
  {
    return "[S] c9 SiegeInfo";
  }
}