package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.HennaTable;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.HennaItemInfo;
import net.sf.l2j.gameserver.templates.L2Henna;

public final class RequestHennaItemInfo extends L2GameClientPacket
{
  private int _symbolId;

  protected void readImpl()
  {
    _symbolId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);
    if (template == null) {
      return;
    }
    L2HennaInstance temp = new L2HennaInstance(template);

    player.sendPacket(new HennaItemInfo(temp, player));
  }
}