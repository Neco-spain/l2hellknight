package l2m.gameserver.network.clientpackets;

import l2m.gameserver.data.xml.holder.HennaHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.HennaUnequipInfo;
import l2m.gameserver.templates.Henna;

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