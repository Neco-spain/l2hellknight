package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

public class GMViewQuestList extends L2GameServerPacket
{

    public GMViewQuestList(L2Player cha)
    {
        _activeChar = cha;
    }

    protected final void writeImpl()
    {
        writeC(147);
        writeS(_activeChar.getName());
        Quest questList[] = _activeChar.getAllActiveQuests();
        if(questList.length == 0)
        {
            writeC(0);
            writeH(0);
            writeH(0);
            return;
        }
        writeH(questList.length);
        Quest arr$[] = questList;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            Quest q = arr$[i$];
            writeD(q.getQuestIntId());
            QuestState qs = _activeChar.getQuestState(q.getName());
            if(qs == null)
                writeD(0);
            else
                writeD(qs.getInt("cond"));
        }

    }

    private L2Player _activeChar;
}
