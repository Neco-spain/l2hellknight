package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.database.mysql;
import l2p.gameserver.loginservercon.SessionKey;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.CharacterDeleteFail;
import l2p.gameserver.serverpackets.CharacterDeleteSuccess;
import l2p.gameserver.serverpackets.CharacterSelectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterDelete extends L2GameClientPacket
{
  private static final Logger _log = LoggerFactory.getLogger(CharacterDelete.class);
  private int _charSlot;

  protected void readImpl()
  {
    _charSlot = readD();
  }

  protected void runImpl()
  {
    int clan = clanStatus();
    int online = onlineStatus();
    if ((clan > 0) || (online > 0))
    {
      if (clan == 2)
        sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
      else if (clan == 1)
        sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
      else if (online > 0)
        sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_DELETION_FAILED));
      return;
    }

    GameClient client = (GameClient)getClient();
    try
    {
      if (Config.DELETE_DAYS == 0)
        client.deleteChar(_charSlot);
      else
        client.markToDeleteChar(_charSlot);
    }
    catch (Exception e)
    {
      _log.error("Error:", e);
    }

    sendPacket(new CharacterDeleteSuccess());

    CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
    sendPacket(cl);
    client.setCharSelection(cl.getCharInfo());
  }

  private int clanStatus()
  {
    int obj = ((GameClient)getClient()).getObjectIdForSlot(_charSlot);
    if (obj == -1)
      return 0;
    if (mysql.simple_get_int("clanid", "characters", "obj_Id=" + obj) > 0)
    {
      if (mysql.simple_get_int("leader_id", "clan_subpledges", "leader_id=" + obj + " AND type = " + 0) > 0)
        return 2;
      return 1;
    }
    return 0;
  }

  private int onlineStatus()
  {
    int obj = ((GameClient)getClient()).getObjectIdForSlot(_charSlot);
    if (obj == -1)
      return 0;
    if (mysql.simple_get_int("online", "characters", "obj_Id=" + obj) > 0)
      return 1;
    return 0;
  }
}