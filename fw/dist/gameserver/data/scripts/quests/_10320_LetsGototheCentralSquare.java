package quests;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.serverpackets.TutorialShowHtml;

//By Evil_dnk dev.fairytale-world.ru
public class _10320_LetsGototheCentralSquare extends Quest implements ScriptFile {
    private static final int teodor = 32975;
    private static final int panteleon = 32972;

    @Override
    public void onLoad() {
    }

    @Override
    public void onReload() {
    }

    @Override
    public void onShutdown() {
    }

    public _10320_LetsGototheCentralSquare() {
        super(false);
        addStartNpc(panteleon);
        addTalkId(panteleon);
        addTalkId(teodor);

        addLevelCheck(1, 20);
    }

    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc) {
        String htmltext = event;
        if (event.equalsIgnoreCase("quest_ac")) {
            st.setState(STARTED);
            st.setCond(1);
            st.playSound(SOUND_ACCEPT);
            st.showTutorialHTML(TutorialShowHtml.QT_001, TutorialShowHtml.TYPE_WINDOW);
            htmltext = "0-3.htm";
        }
        if (event.equalsIgnoreCase("qet_rev")) {
            htmltext = "1-2.htm";
            st.getPlayer().addExpAndSp(30, 100);
            st.giveItems(57, 3000);
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

        if (npcId == panteleon) {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 0 && isAvailableFor(st.getPlayer()))
                htmltext = "0-1.htm";
            else if (cond == 1)
                htmltext = "0-4.htm";

        } else if (npcId == teodor) {
            if (st.isCompleted())
                htmltext = "1-c.htm";
            else if (cond == 0)
                htmltext = "1-t.htm";
            else if (cond == 1)
                htmltext = "1-1.htm";
        }
        return htmltext;
    }
}