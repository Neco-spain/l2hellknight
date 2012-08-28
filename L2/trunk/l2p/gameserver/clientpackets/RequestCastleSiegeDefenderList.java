package l2p.gameserver.clientpackets;

import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.CastleSiegeDefenderList;

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