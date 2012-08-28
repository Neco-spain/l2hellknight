package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;

public class PledgeReceiveWarList extends L2GameServerPacket
{
  private L2Clan _clan;
  private int _tab;
  private ConcurrentLinkedQueue<PledgeInfo> _list = new ConcurrentLinkedQueue();

  public PledgeReceiveWarList(L2Clan clan, int tab)
  {
    _clan = clan;
    _tab = tab;
    _list.clear();

    L2Clan pledge = null;
    ClanTable ct = ClanTable.getInstance();
    for (Iterator i$ = (_tab == 0 ? _clan.getWarList() : _clan.getAttackerList()).iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();

      pledge = ct.getClan(i);
      if (pledge == null) {
        continue;
      }
      _list.add(new PledgeInfo(pledge.getName(), _tab, _tab));
    }
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(62);

    writeD(_tab);
    writeD(0);
    writeD(_list.size());

    for (PledgeInfo pledge : _list)
    {
      if (pledge == null) {
        continue;
      }
      writeS(pledge.name);
      writeD(pledge.tab1);
      writeD(pledge.tab2);
    }
  }
  static class PledgeInfo {
    public String name;
    public int tab1;
    public int tab2;

    public PledgeInfo(String name, int tab1, int tab2) {
      this.name = name;
      this.tab1 = tab1;
      this.tab2 = tab2;
    }
  }
}