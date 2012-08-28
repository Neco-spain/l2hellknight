package l2m.gameserver.network.clientpackets;

import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.residence.Residence;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.CastleSiegeAttackerList;

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