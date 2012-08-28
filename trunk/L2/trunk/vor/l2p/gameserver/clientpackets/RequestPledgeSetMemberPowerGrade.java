package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.UnitMember;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.components.CustomMessage;

public class RequestPledgeSetMemberPowerGrade extends L2GameClientPacket
{
  private int _powerGrade;
  private String _name;

  protected void readImpl()
  {
    _name = readS(16);
    _powerGrade = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if ((_powerGrade < 1) || (_powerGrade > 9)) {
      return;
    }
    Clan clan = activeChar.getClan();
    if (clan == null) {
      return;
    }
    if ((activeChar.getClanPrivileges() & 0x10) == 16)
    {
      UnitMember member = activeChar.getClan().getAnyMember(_name);
      if (member != null)
      {
        if (Clan.isAcademy(member.getPledgeType()))
        {
          activeChar.sendMessage("You cannot change academy member grade.");
          return;
        }
        if (_powerGrade > 5)
          member.setPowerGrade(clan.getAffiliationRank(member.getPledgeType()));
        else
          member.setPowerGrade(_powerGrade);
        if (member.isOnline())
          member.getPlayer().sendUserInfo();
      }
      else {
        activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeSetMemberPowerGrade.NotBelongClan", activeChar, new Object[0]));
      }
    } else {
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeSetMemberPowerGrade.HaveNotAuthority", activeChar, new Object[0]));
    }
  }
}