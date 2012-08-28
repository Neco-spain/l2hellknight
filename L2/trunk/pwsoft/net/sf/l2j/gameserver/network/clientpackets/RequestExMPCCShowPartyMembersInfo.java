package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ExMPCCShowPartyMemberInfo;

public final class RequestExMPCCShowPartyMembersInfo extends L2GameClientPacket
{
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if ((player == null) || (!player.isInParty()) || (!player.getParty().isInCommandChannel())) {
      return;
    }
    L2PcInstance partyLeader = L2World.getInstance().getPlayer(_objectId);
    if ((partyLeader != null) && (partyLeader.getParty() != null))
      player.sendPacket(new ExMPCCShowPartyMemberInfo(partyLeader));
  }
}