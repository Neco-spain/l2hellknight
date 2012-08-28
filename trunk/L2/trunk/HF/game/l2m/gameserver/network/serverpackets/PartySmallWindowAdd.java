package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Player;

public class PartySmallWindowAdd extends L2GameServerPacket
{
  private int objectId;
  private final PartySmallWindowAll.PartySmallWindowMemberInfo member;

  public PartySmallWindowAdd(Player player, Player member)
  {
    objectId = player.getObjectId();
    this.member = new PartySmallWindowAll.PartySmallWindowMemberInfo(member);
  }

  protected final void writeImpl()
  {
    writeC(79);
    writeD(objectId);
    writeD(0);
    writeD(member._id);
    writeS(member._name);
    writeD(member.curCp);
    writeD(member.maxCp);
    writeD(member.curHp);
    writeD(member.maxHp);
    writeD(member.curMp);
    writeD(member.maxMp);
    writeD(member.level);
    writeD(member.class_id);
    writeD(0);
    writeD(member.race_id);
    writeD(0);
    writeD(0);
  }
}