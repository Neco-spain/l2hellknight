package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.HennaTreeTable;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.HennaEquipList;

public final class RequestHennaList extends L2GameClientPacket
{
  private static final String _C__BA_RequestHennaList = "[C] ba RequestHennaList";
  private int _unknown;

  protected void readImpl()
  {
    _unknown = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    L2HennaInstance[] henna = HennaTreeTable.getInstance().getAvailableHenna(activeChar.getClassId());
    HennaEquipList he = new HennaEquipList(activeChar, henna);
    activeChar.sendPacket(he);
  }

  public String getType()
  {
    return "[C] ba RequestHennaList";
  }
}