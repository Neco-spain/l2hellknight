package l2p.gameserver.clientpackets;

import l2p.gameserver.data.xml.holder.HennaHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.HennaUnequipInfo;
import l2p.gameserver.templates.Henna;

public class RequestHennaUnequipInfo extends L2GameClientPacket
{
  private int _symbolId;

  protected void readImpl()
  {
    _symbolId = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Henna henna = HennaHolder.getInstance().getHenna(_symbolId);
    if (henna != null)
      player.sendPacket(new HennaUnequipInfo(henna, player));
  }
}