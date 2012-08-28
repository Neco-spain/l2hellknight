package l2p.gameserver.clientpackets;

import java.util.HashSet;
import java.util.Set;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.matching.MatchingRoom;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExMpccPartymasterList;

public class RequestExMpccPartymasterList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    MatchingRoom room = player.getMatchingRoom();
    if ((room == null) || (room.getType() != MatchingRoom.CC_MATCHING)) {
      return;
    }
    Set set = new HashSet();
    for (Player $member : room.getPlayers()) {
      if ($member.getParty() != null)
        set.add($member.getParty().getPartyLeader().getName());
    }
    player.sendPacket(new ExMpccPartymasterList(set));
  }
}