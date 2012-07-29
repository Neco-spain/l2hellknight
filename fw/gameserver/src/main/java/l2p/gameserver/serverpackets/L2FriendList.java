package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.actor.instances.player.Friend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class L2FriendList extends L2GameServerPacket {
    private List<FriendInfo> _list = Collections.emptyList();

    public L2FriendList(Player player) {
        Map<Integer, Friend> list = player.getFriendList().getList();
        _list = new ArrayList<FriendInfo>(list.size());
        for (Map.Entry<Integer, Friend> entry : list.entrySet()) {
            FriendInfo f = new FriendInfo();
            f._objectId = entry.getKey();
            f._name = entry.getValue().getName();
            f._online = entry.getValue().isOnline();
            f._lvl = entry.getValue().getLevel();
            f._classId = entry.getValue().getClassId();
            _list.add(f);
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0x75);
        writeD(_list.size());
        for (FriendInfo friendInfo : _list) {
            writeD(friendInfo._objectId);
            writeS(friendInfo._name);
            writeD(friendInfo._online);
            writeD(friendInfo._online ? friendInfo._objectId : 0);
            writeD(friendInfo._lvl);
            writeD(friendInfo._classId);
            writeH(0);
        }
    }

    private static class FriendInfo {
        private int _objectId;
        private String _name;
        private boolean _online;
        private int _lvl;
        private int _classId;
    }
}