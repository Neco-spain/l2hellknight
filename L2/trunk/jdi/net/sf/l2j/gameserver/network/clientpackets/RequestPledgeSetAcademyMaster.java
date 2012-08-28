package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestPledgeSetAcademyMaster extends L2GameClientPacket
{
  private static final String _C__D0_19_REQUESTSETPLEADGEACADEMYMASTER = "[C] D0:19 RequestPledgeSetAcademyMaster";
  private String _currPlayerName;
  private int _set;
  private String _targetPlayerName;

  protected void readImpl()
  {
    _set = readD();
    _currPlayerName = readS();
    _targetPlayerName = readS();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    L2Clan clan = activeChar.getClan();
    if (clan == null) return;

    if ((activeChar.getClanPrivileges() & 0x100) != 256)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_DISMISS_AN_APPRENTICE));
      return;
    }

    L2ClanMember currentMember = clan.getClanMember(_currPlayerName);
    L2ClanMember targetMember = clan.getClanMember(_targetPlayerName);
    if ((currentMember == null) || (targetMember == null)) return;
    L2ClanMember sponsorMember;
    L2ClanMember apprenticeMember;
    L2ClanMember sponsorMember;
    if (currentMember.getPledgeType() == -1)
    {
      L2ClanMember apprenticeMember = currentMember;
      sponsorMember = targetMember;
    }
    else
    {
      apprenticeMember = targetMember;
      sponsorMember = currentMember;
    }

    L2PcInstance apprentice = apprenticeMember.getPlayerInstance();
    L2PcInstance sponsor = sponsorMember.getPlayerInstance();

    SystemMessage sm = null;
    if (_set == 0)
    {
      if (apprentice != null) apprentice.setSponsor(0);
      else {
        apprenticeMember.initApprenticeAndSponsor(0, 0);
      }
      if (sponsor != null) sponsor.setApprentice(0);
      else {
        sponsorMember.initApprenticeAndSponsor(0, 0);
      }
      apprenticeMember.saveApprenticeAndSponsor(0, 0);
      sponsorMember.saveApprenticeAndSponsor(0, 0);

      sm = new SystemMessage(SystemMessageId.S2_CLAN_MEMBER_S1_S_APPRENTICE_HAS_BEEN_REMOVED);
    }
    else
    {
      if ((apprenticeMember.getSponsor() != 0) || (sponsorMember.getApprentice() != 0) || (apprenticeMember.getApprentice() != 0) || (sponsorMember.getSponsor() != 0))
      {
        activeChar.sendMessage("Remove previous connections first.");
        return;
      }
      if (apprentice != null)
        apprentice.setSponsor(sponsorMember.getObjectId());
      else {
        apprenticeMember.initApprenticeAndSponsor(0, sponsorMember.getObjectId());
      }
      if (sponsor != null)
        sponsor.setApprentice(apprenticeMember.getObjectId());
      else {
        sponsorMember.initApprenticeAndSponsor(apprenticeMember.getObjectId(), 0);
      }

      apprenticeMember.saveApprenticeAndSponsor(0, sponsorMember.getObjectId());
      sponsorMember.saveApprenticeAndSponsor(apprenticeMember.getObjectId(), 0);

      sm = new SystemMessage(SystemMessageId.S2_HAS_BEEN_DESIGNATED_AS_APPRENTICE_OF_CLAN_MEMBER_S1);
    }
    sm.addString(sponsorMember.getName());
    sm.addString(apprenticeMember.getName());
    if ((sponsor != activeChar) && (sponsor != apprentice)) activeChar.sendPacket(sm);
    if (sponsor != null) sponsor.sendPacket(sm);
    if (apprentice != null) apprentice.sendPacket(sm);
  }

  public String getType()
  {
    return "[C] D0:19 RequestPledgeSetAcademyMaster";
  }
}