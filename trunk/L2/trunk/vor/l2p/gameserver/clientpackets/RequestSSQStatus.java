package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.SevenSigns;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.SSQStatus;

public class RequestSSQStatus extends L2GameClientPacket
{
  private int _page;

  protected void readImpl()
  {
    _page = readC();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (((SevenSigns.getInstance().isSealValidationPeriod()) || (SevenSigns.getInstance().isCompResultsPeriod())) && (_page == 4)) {
      return;
    }
    activeChar.sendPacket(new SSQStatus(activeChar, _page));
  }
}