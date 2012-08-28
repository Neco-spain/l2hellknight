package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class RequestGiveNickName extends L2GameClientPacket
{
  private static final String _C__55_REQUESTGIVENICKNAME = "[C] 55 RequestGiveNickName";
  static Logger _log = Logger.getLogger(RequestGiveNickName.class.getName());
  private String _target;
  private String _title;

  protected void readImpl()
  {
    _target = readS();
    _title = readS();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }

    if ((activeChar.isNoble()) && (_target.equalsIgnoreCase(activeChar.getName())))
    {
      activeChar.setTitle(_title);
      SystemMessage sm = new SystemMessage(SystemMessageId.TITLE_CHANGED);
      activeChar.sendPacket(sm);
      activeChar.broadcastTitleInfo();
    }
    else if ((activeChar.getClanPrivileges() & 0x4) == 4)
    {
      if (activeChar.getClan().getLevel() < 3)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE);
        activeChar.sendPacket(sm);
        sm = null;
        return;
      }

      L2ClanMember member1 = activeChar.getClan().getClanMember(_target);
      if (member1 != null)
      {
        L2PcInstance member = member1.getPlayerInstance();
        if (member != null)
        {
          member.setTitle(_title);
          SystemMessage sm = new SystemMessage(SystemMessageId.TITLE_CHANGED);
          member.sendPacket(sm);
          member.broadcastTitleInfo();
          sm = null;
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
          sm.addString("Target needs to be online to get a title");
          activeChar.sendPacket(sm);
          sm = null;
        }
      }
      else
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
        sm.addString("Target does not belong to your clan");
        activeChar.sendPacket(sm);
        sm = null;
      }
    }
  }

  public String getType()
  {
    return "[C] 55 RequestGiveNickName";
  }
}