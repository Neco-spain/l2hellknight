package l2m.gameserver.network.clientpackets;

import java.util.HashSet;
import java.util.Set;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.matching.MatchingRoom;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExMpccPartymasterList;

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