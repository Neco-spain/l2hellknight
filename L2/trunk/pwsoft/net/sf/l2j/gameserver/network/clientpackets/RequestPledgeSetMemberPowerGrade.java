package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestPledgeSetMemberPowerGrade extends L2GameClientPacket
{
  private int _powerGrade;
  private String _member;

  protected void readImpl()
  {
    _member = readS();
    _powerGrade = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null)
      return;
    L2Clan clan = player.getClan();
    if (clan == null)
      return;
    L2ClanMember member = clan.getClanMember(_member);
    if (member == null)
      return;
    if (member.getPledgeType() == -1)
    {
      player.sendMessage("You cannot change academy member grade");
      return;
    }
    member.setPowerGrade(_powerGrade);
    clan.broadcastClanStatus();
  }

  public String getType()
  {
    return "C.PledgeSetMemberPowerGrade";
  }
}