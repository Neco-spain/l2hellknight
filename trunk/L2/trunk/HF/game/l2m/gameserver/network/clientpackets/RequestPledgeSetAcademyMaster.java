package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.UnitMember;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.PledgeReceiveMemberInfo;
import l2m.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.CustomMessage;

public class RequestPledgeSetAcademyMaster extends L2GameClientPacket
{
  private int _mode;
  private String _sponsorName;
  private String _apprenticeName;

  protected void readImpl()
  {
    _mode = readD();
    _sponsorName = readS(16);
    _apprenticeName = readS(16);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Clan clan = activeChar.getClan();
    if (clan == null) {
      return;
    }
    if ((activeChar.getClanPrivileges() & 0x100) == 256)
    {
      UnitMember sponsor = activeChar.getClan().getAnyMember(_sponsorName);
      UnitMember apprentice = activeChar.getClan().getAnyMember(_apprenticeName);
      if ((sponsor != null) && (apprentice != null))
      {
        if ((apprentice.getPledgeType() != -1) || (sponsor.getPledgeType() == -1)) {
          return;
        }
        if (_mode == 1)
        {
          if (sponsor.hasApprentice())
          {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.MemberAlreadyHasApprentice", activeChar, new Object[0]));
            return;
          }
          if (apprentice.hasSponsor())
          {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.ApprenticeAlreadyHasSponsor", activeChar, new Object[0]));
            return;
          }
          sponsor.setApprentice(apprentice.getObjectId());
          clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new PledgeShowMemberListUpdate(apprentice) });
          clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage(1755).addString(sponsor.getName()).addString(apprentice.getName()) });
        }
        else
        {
          if (!sponsor.hasApprentice())
          {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.MemberHasNoApprentice", activeChar, new Object[0]));
            return;
          }
          sponsor.setApprentice(0);
          clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new PledgeShowMemberListUpdate(apprentice) });
          clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage(1763).addString(sponsor.getName()).addString(apprentice.getName()) });
        }
        if (apprentice.isOnline())
          apprentice.getPlayer().broadcastCharInfo();
        activeChar.sendPacket(new PledgeReceiveMemberInfo(sponsor));
      }
    }
    else {
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.NoMasterRights", activeChar, new Object[0]));
    }
  }
}