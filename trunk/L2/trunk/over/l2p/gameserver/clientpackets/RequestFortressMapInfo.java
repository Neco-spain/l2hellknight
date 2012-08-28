package l2p.gameserver.clientpackets;

import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.residence.Fortress;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExShowFortressMapInfo;

public class RequestFortressMapInfo extends L2GameClientPacket
{
  private int _fortressId;

  protected void readImpl()
  {
    _fortressId = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null)
      return;
    Fortress fortress = (Fortress)ResidenceHolder.getInstance().getResidence(Fortress.class, _fortressId);
    if (fortress != null)
      sendPacket(new ExShowFortressMapInfo(fortress));
  }
}