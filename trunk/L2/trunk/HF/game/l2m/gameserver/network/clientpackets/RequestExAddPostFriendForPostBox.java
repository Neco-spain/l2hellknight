package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.data.dao.CharacterDAO;
import l2m.gameserver.data.dao.CharacterPostFriendDAO;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExConfirmAddingPostFriend;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import org.napile.primitive.maps.IntObjectMap;

public class RequestExAddPostFriendForPostBox extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
    throws Exception
  {
    _name = readS(Config.CNAME_MAXLEN);
  }

  protected void runImpl()
    throws Exception
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    int targetObjectId = CharacterDAO.getInstance().getObjectIdByName(_name);
    if (targetObjectId == 0)
    {
      player.sendPacket(new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.NAME_IS_NOT_EXISTS));
      return;
    }

    if (_name.equalsIgnoreCase(player.getName()))
    {
      player.sendPacket(new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.NAME_IS_NOT_REGISTERED));
      return;
    }

    IntObjectMap postFriend = player.getPostFriends();
    if (postFriend.size() >= 100)
    {
      player.sendPacket(new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.LIST_IS_FULL));
      return;
    }

    if (postFriend.containsKey(targetObjectId))
    {
      player.sendPacket(new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.ALREADY_ADDED));
      return;
    }

    CharacterPostFriendDAO.getInstance().insert(player, targetObjectId);
    postFriend.put(targetObjectId, CharacterDAO.getInstance().getNameByObjectId(targetObjectId));

    player.sendPacket(new IStaticPacket[] { new SystemMessage2(SystemMsg.S1_WAS_SUCCESSFULLY_ADDED_TO_YOUR_CONTACT_LIST).addString(_name), new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.SUCCESS) });
  }
}