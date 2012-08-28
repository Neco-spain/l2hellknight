package l2m.gameserver.network.clientpackets;

import java.util.List;
import l2m.gameserver.instancemanager.MatchingRoomManager;
import l2m.gameserver.model.CommandChannel;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.matching.CCMatchingRoom;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ListPartyWaiting;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class RequestPartyMatchConfig extends L2GameClientPacket
{
  private int _page;
  private int _region;
  private int _allLevels;

  protected void readImpl()
  {
    _page = readD();
    _region = readD();
    _allLevels = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Party party = player.getParty();
    CommandChannel channel = party != null ? party.getCommandChannel() : null;

    if ((channel != null) && (channel.getChannelLeader() == player))
    {
      if (channel.getMatchingRoom() == null)
      {
        CCMatchingRoom room = new CCMatchingRoom(player, 1, player.getLevel(), 50, party.getLootDistribution(), player.getName());
        channel.setMatchingRoom(room);
      }
    }
    else if ((channel != null) && (!channel.getParties().contains(party))) {
      player.sendPacket(SystemMsg.THE_COMMAND_CHANNEL_AFFILIATED_PARTYS_PARTY_MEMBER_CANNOT_USE_THE_MATCHING_SCREEN);
    } else if ((party != null) && (!party.isLeader(player))) {
      player.sendPacket(SystemMsg.THE_LIST_OF_PARTY_ROOMS_CAN_ONLY_BE_VIEWED_BY_A_PERSON_WHO_IS_NOT_PART_OF_A_PARTY);
    }
    else {
      MatchingRoomManager.getInstance().addToWaitingList(player);
      player.sendPacket(new ListPartyWaiting(_region, _allLevels == 1, _page, player));
    }
  }
}