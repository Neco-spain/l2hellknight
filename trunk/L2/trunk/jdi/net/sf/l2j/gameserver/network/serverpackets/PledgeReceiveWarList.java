package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;

public class PledgeReceiveWarList extends L2GameServerPacket
{
  private static final String _S__FE_3E_PLEDGERECEIVEWARELIST = "[S] FE:3E PledgeReceiveWarList";
  private L2Clan _clan;
  private int _tab;

  public PledgeReceiveWarList(L2Clan clan, int tab)
  {
    _clan = clan;
    _tab = tab;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(62);

    writeD(_tab);
    writeD(0);
    writeD(_tab == 0 ? _clan.getWarList().size() : _clan.getAttackerList().size());
    for (Integer i : _tab == 0 ? _clan.getWarList() : _clan.getAttackerList())
    {
      L2Clan clan = ClanTable.getInstance().getClan(i.intValue());
      if (clan == null)
        continue;
      writeS(clan.getName());
      writeD(_tab);
      writeD(_tab);
    }
  }

  public String getType()
  {
    return "[S] FE:3E PledgeReceiveWarList";
  }
}