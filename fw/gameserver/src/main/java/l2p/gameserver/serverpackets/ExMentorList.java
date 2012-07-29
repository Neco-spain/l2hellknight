package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.actor.instances.player.Mentee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ExMentorList extends L2GameServerPacket {
    private List<MenteeInfo> _list = Collections.emptyList();
    private int _mentor;

    public ExMentorList(Player player) {
        _mentor = player.getMenteeList().getMentor();
        Map<Integer, Mentee> list = player.getMenteeList().getList();
        _list = new ArrayList<MenteeInfo>(list.size());
        for (Map.Entry<Integer, Mentee> entry : list.entrySet()) {
            MenteeInfo m = new MenteeInfo();
            m.objectId = entry.getKey();
            m.name = entry.getValue().getName();
            m.online = entry.getValue().isOnline();
            m.level = entry.getValue().getLevel();
            m.classId = entry.getValue().getClassId();
            _list.add(m);
        }
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x120);
        writeD(_mentor == 0 ? 0x01 : 0x02); // 02 приходит ученику, 01 - наставнику
        writeD(_list.size()); // Размер  следующего списка
        for(MenteeInfo entry : _list)
        {
            writeD(entry.objectId); // objectId
            writeS(entry.name); // nickname
            writeD(entry.classId);//classId
            writeD(entry.level);// level
            writeD(entry.online); //online
        }
    }

    private class MenteeInfo {
        private String name;
        private int objectId;
        private boolean online;
        private int level;
        private int classId;
    }
}