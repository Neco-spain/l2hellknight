package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;

public class PledgeShowMemberListAdd extends L2GameServerPacket
{
  private static final String _S__55_PLEDGESHOWMEMBERLISTADD = "[S] 55 PledgeShowMemberListAdd";
  private String _name;
  private int _lvl;
  private int _classId;
  private int _isOnline;
  private int _pledgeType;

  public PledgeShowMemberListAdd(L2PcInstance player)
  {
    _name = player.getName();
    _lvl = player.getLevel();
    _classId = player.getClassId().getId();
    _isOnline = (player.isOnline() == 1 ? player.getObjectId() : 0);
    _pledgeType = player.getPledgeType();
  }

  public PledgeShowMemberListAdd(L2ClanMember cm)
  {
    try
    {
      _name = cm.getName();
      _lvl = cm.getLevel();
      _classId = cm.getClassId();
      _isOnline = (cm.isOnline() ? cm.getObjectId() : 0);
      _pledgeType = cm.getPledgeType();
    }
    catch (Exception e)
    {
    }
  }

  protected final void writeImpl()
  {
    writeC(85);
    writeS(_name);
    writeD(_lvl);
    writeD(_classId);
    writeD(0);
    writeD(1);
    writeD(_isOnline);
    writeD(_pledgeType);
  }

  public String getType()
  {
    return "[S] 55 PledgeShowMemberListAdd";
  }
}