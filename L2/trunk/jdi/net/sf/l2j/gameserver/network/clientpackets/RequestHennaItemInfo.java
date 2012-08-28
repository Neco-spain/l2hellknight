package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.HennaTable;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.HennaItemInfo;
import net.sf.l2j.gameserver.templates.L2Henna;

public final class RequestHennaItemInfo extends L2GameClientPacket
{
  private static final String _C__BB_RequestHennaItemInfo = "[C] bb RequestHennaItemInfo";
  private int _symbolId;

  protected void readImpl()
  {
    _symbolId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);
    if (template == null)
    {
      return;
    }
    L2HennaInstance temp = new L2HennaInstance(template);

    HennaItemInfo hii = new HennaItemInfo(temp, activeChar);
    activeChar.sendPacket(hii);
  }

  public String getType()
  {
    return "[C] bb RequestHennaItemInfo";
  }
}