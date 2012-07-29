package quests;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.scripts.ScriptFile;


//By Evil_dnk dev.fairytale-world.ru

public class _10326_RespectYourElders extends Quest implements ScriptFile {
    private static final int panteleon = 32972;
    private static final int galint = 32980;

    @Override
    public void onLoad() {
    }

    @Override
    public void onReload() {
    }

    @Override
    public void onShutdown() {
    }

    public _10326_RespectYourElders() {
        super(false);
        addStartNpc(galint);
        addTalkId(panteleon);

        addLevelCheck(1, 20);
        addQuestCompletedCheck(_10325_SearchingForNewPower.class);
    }

    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc) {
        String htmltext = event;

        if (event.equalsIgnoreCase("quest_ac")) {
            st.setState(STARTED);
            st.setCond(1);
            st.playSound(SOUND_ACCEPT);
            htmltext = "0-3.htm";
        }

        if (event.equalsIgnoreCase("qet_rev")) {
            htmltext = "1-2.htm";
            st.getPlayer().addExpAndSp(5300, 2800);
            st.giveItems(57, 14000);
            st.exitCurrentQuest(false);
            st.playSound(SOUND_FINISH);
        }
        return htmltext;
    }

    @Override
    public String onTalk(NpcInstance npc, QuestState st) {
        int cond = st.getCond();
        int npcId = npc.getNpcId();
        String htmltext = "noquest";


        if (npcId == galint) {
            if (st.isCompleted())
                htmltext = "0-nc.htm";
            else if (cond == 0 && isAvailableFor(st.getPlayer()))
                htmltext = "start.htm";
            else if (cond == 1)
                htmltext = "0-4.htm";
            else
                htmltext = "0-nc.htm";
        } else if (npcId == panteleon) {
            if (st.isCompleted())
                htmltext = "1-c.htm";
            else if (cond == 0)
                htmltext = TODO_FIND_HTML;
            else if (cond == 1)
                htmltext = "1-1.htm";
        }
        return htmltext;
    }
}
	
	
