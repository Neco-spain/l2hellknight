package l2m.gameserver.network.clientpackets;

import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.residence.Fortress;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExShowFortressMapInfo;

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