package l2p.gameserver.clientpackets;

import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.residence.Residence;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.CastleSiegeAttackerList;

public class RequestCastleSiegeAttackerList extends L2GameClientPacket
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
    Residence residence = ResidenceHolder.getInstance().getResidence(_unitId);
    if (residence != null)
      sendPacket(new CastleSiegeAttackerList(residence));
  }
}