package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;

public class SiegeDefenderList extends L2GameServerPacket
{
  private Castle _castle;

  public SiegeDefenderList(Castle castle)
  {
    _castle = castle;
  }

  protected final void writeImpl()
  {
    writeC(203);
    writeD(_castle.getCastleId());
    writeD(0);
    writeD(1);
    writeD(0);
    int size = _castle.getSiege().getDefenderClans().size() + _castle.getSiege().getDefenderWaitingClans().size();
    if (size > 0)
    {
      writeD(size);
      writeD(size);

      for (L2SiegeClan siegeclan : _castle.getSiege().getDefenderClans())
      {
        L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
        if (clan == null)
          continue;
        writeD(clan.getClanId());
        writeS(clan.getName());
        writeS(clan.getLeaderName());
        writeD(clan.getCrestId());
        writeD(0);
        switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2SiegeClan$SiegeClanType[siegeclan.getType().ordinal()])
        {
        case 1:
          writeD(1);
          break;
        case 2:
          writeD(2);
          break;
        case 3:
          writeD(3);
          break;
        default:
          writeD(0);
        }

        writeD(clan.getAllyId());
        writeS(clan.getAllyName());
        writeS("");
        writeD(clan.getAllyCrestId());
      }
      for (L2SiegeClan siegeclan : _castle.getSiege().getDefenderWaitingClans())
      {
        L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
        writeD(clan.getClanId());
        writeS(clan.getName());
        writeS(clan.getLeaderName());
        writeD(clan.getCrestId());
        writeD(0);
        writeD(2);
        writeD(clan.getAllyId());
        writeS(clan.getAllyName());
        writeS("");
        writeD(clan.getAllyCrestId());
      }
    }
    else
    {
      writeD(0);
      writeD(0);
    }
  }

  public String getType()
  {
    return "S.SiegeDefenderList";
  }
}