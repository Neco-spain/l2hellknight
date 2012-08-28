package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestOustPledgeMember extends L2GameClientPacket
{
  private static final String _C__27_REQUESTOUSTPLEDGEMEMBER = "[C] 27 RequestOustPledgeMember";
  static Logger _log = Logger.getLogger(RequestOustPledgeMember.class.getName());
  private String _target;

  protected void readImpl()
  {
    _target = readS();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
    {
      return;
    }
    if (activeChar.getClan() == null)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER));
      return;
    }
    if ((activeChar.getClanPrivileges() & 0x40) != 64)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
      return;
    }
    if (activeChar.getName().equalsIgnoreCase(_target))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_DISMISS_YOURSELF));
      return;
    }

    L2Clan clan = activeChar.getClan();

    L2ClanMember member = clan.getClanMember(_target);
    if (member == null)
    {
      _log.warning("Target (" + _target + ") is not member of the clan");
      return;
    }
    if ((member.isOnline()) && (member.getPlayerInstance().isInCombat()))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_MEMBER_CANNOT_BE_DISMISSED_DURING_COMBAT));
      return;
    }

    clan.removeClanMember(_target, System.currentTimeMillis() + Config.ALT_CLAN_JOIN_DAYS * 86400000L);
    clan.setCharPenaltyExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_JOIN_DAYS * 86400000L);
    clan.updateClanInDB();

    SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
    sm.addString(member.getName());
    clan.broadcastToOnlineMembers(sm);
    sm = null;
    activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER));
    activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER));

    clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(_target));

    if (member.isOnline())
    {
      L2PcInstance player = member.getPlayerInstance();
      player.sendPacket(new SystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED));
    }
  }

  public String getType()
  {
    return "[C] 27 RequestOustPledgeMember";
  }
}