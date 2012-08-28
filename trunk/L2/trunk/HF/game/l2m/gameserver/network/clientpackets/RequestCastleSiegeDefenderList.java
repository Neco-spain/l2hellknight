package l2m.gameserver.network.clientpackets;

import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.residence.Castle;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.CastleSiegeDefenderList;

public class RequestCastleSiegeDefenderList extends L2GameClientPacket
{
  private int _unitId;

  protected void readImpl()
  {
    _unitId = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Castle castle = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, _unitId);
    if ((castle == null) || (castle.getOwner() == null)) {
      return;
    }
    player.sendPacket(new CastleSiegeDefenderList(castle));
  }
}