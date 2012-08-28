package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.UnitMember;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.NickNameChanged;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.utils.Util;

public class RequestGiveNickName extends L2GameClientPacket
{
  private String _target;
  private String _title;

  protected void readImpl()
  {
    _target = readS(Config.CNAME_MAXLEN);
    _title = readS();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if ((!_title.isEmpty()) && (!Util.isMatchingRegexp(_title, Config.CLAN_TITLE_TEMPLATE)))
    {
      activeChar.sendMessage("Incorrect title.");
      return;
    }

    if ((activeChar.isNoble()) && (_target.matches(activeChar.getName())))
    {
      activeChar.setTitle(_title);
      activeChar.sendPacket(Msg.TITLE_HAS_CHANGED);
      activeChar.broadcastPacket(new L2GameServerPacket[] { new NickNameChanged(activeChar) });
      return;
    }

    if ((activeChar.getClanPrivileges() & 0x4) != 4) {
      return;
    }
    if (activeChar.getClan().getLevel() < 3)
    {
      activeChar.sendPacket(Msg.TITLE_ENDOWMENT_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3);
      return;
    }

    UnitMember member = activeChar.getClan().getAnyMember(_target);
    if (member != null)
    {
      member.setTitle(_title);
      if (member.isOnline())
      {
        member.getPlayer().sendPacket(Msg.TITLE_HAS_CHANGED);
        member.getPlayer().sendChanges();
      }
    }
    else {
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestGiveNickName.NotInClan", activeChar, new Object[0]));
    }
  }
}