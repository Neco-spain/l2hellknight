package l2p.gameserver.clientpackets;

import l2p.gameserver.dao.CharacterPostFriendDAO;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.IntObjectMap.Entry;

public class RequestExDeletePostFriendForPostBox extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
    throws Exception
  {
    _name = readS();
  }

  protected void runImpl()
    throws Exception
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (StringUtils.isEmpty(_name)) {
      return;
    }
    int key = 0;
    IntObjectMap postFriends = player.getPostFriends();
    for (IntObjectMap.Entry entry : postFriends.entrySet())
    {
      if (((String)entry.getValue()).equalsIgnoreCase(_name)) {
        key = entry.getKey();
      }
    }
    if (key == 0)
    {
      player.sendPacket(SystemMsg.THE_NAME_IS_NOT_CURRENTLY_REGISTERED);
      return;
    }

    player.getPostFriends().remove(key);

    CharacterPostFriendDAO.getInstance().delete(player, key);
    player.sendPacket(new SystemMessage2(SystemMsg.S1_WAS_SUCCESSFULLY_DELETED_FROM_YOUR_CONTACT_LIST).addString(_name));
  }
}