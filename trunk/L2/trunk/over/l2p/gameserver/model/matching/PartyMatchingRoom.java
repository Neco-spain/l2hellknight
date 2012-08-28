package l2p.gameserver.model.matching;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ExClosePartyRoom;
import l2p.gameserver.serverpackets.ExPartyRoomMember;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.PartyRoomInfo;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class PartyMatchingRoom extends MatchingRoom
{
  public PartyMatchingRoom(Player leader, int minLevel, int maxLevel, int maxMemberSize, int lootType, String topic)
  {
    super(leader, minLevel, maxLevel, maxMemberSize, lootType, topic);

    leader.broadcastCharInfo();
  }

  public SystemMsg notValidMessage()
  {
    return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIREMENTS_TO_ENTER_THAT_PARTY_ROOM;
  }

  public SystemMsg enterMessage()
  {
    return SystemMsg.C1_HAS_ENTERED_THE_PARTY_ROOM;
  }

  public SystemMsg exitMessage(boolean toOthers, boolean kick)
  {
    if (toOthers) {
      return kick ? SystemMsg.C1_HAS_BEEN_KICKED_FROM_THE_PARTY_ROOM : SystemMsg.C1_HAS_LEFT_THE_PARTY_ROOM;
    }
    return kick ? SystemMsg.YOU_HAVE_BEEN_OUSTED_FROM_THE_PARTY_ROOM : SystemMsg.YOU_HAVE_EXITED_THE_PARTY_ROOM;
  }

  public SystemMsg closeRoomMessage()
  {
    return SystemMsg.THE_PARTY_ROOM_HAS_BEEN_DISBANDED;
  }

  public L2GameServerPacket closeRoomPacket()
  {
    return ExClosePartyRoom.STATIC;
  }

  public L2GameServerPacket infoRoomPacket()
  {
    return new PartyRoomInfo(this);
  }

  public L2GameServerPacket addMemberPacket(Player $member, Player active)
  {
    return membersPacket($member);
  }

  public L2GameServerPacket removeMemberPacket(Player $member, Player active)
  {
    return membersPacket($member);
  }

  public L2GameServerPacket updateMemberPacket(Player $member, Player active)
  {
    return membersPacket($member);
  }

  public L2GameServerPacket membersPacket(Player active)
  {
    return new ExPartyRoomMember(this, active);
  }

  public int getType()
  {
    return PARTY_MATCHING;
  }

  public int getMemberType(Player member)
  {
    return (member.getParty() != null) && (_leader.getParty() == member.getParty()) ? PARTY_MEMBER : member.equals(_leader) ? ROOM_MASTER : WAIT_PLAYER;
  }
}