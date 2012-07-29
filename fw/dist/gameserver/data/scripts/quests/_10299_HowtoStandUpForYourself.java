package quests;

import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.scripts.ScriptFile;


//By Evil_dnk dev.fairytale-world.ru

public class _10299_HowtoStandUpForYourself extends Quest implements ScriptFile {

    private static final int reins = 30288;  //Human Warrior
    private static final int raimon = 30289;  //Human Mag
    private static final int tobias = 30297;  //   Dark Elf
    private static final int Drikus = 30505;  //  Orc
    private static final int mendius = 30504;  //  Dwarf
    private static final int gershfin = 32196;  // Kamael
    private static final int elinia = 30155;  //  Elf mag
    private static final int ershandel = 30158;  // Elf warrior


    private static final int Joan = 30718;
    private static final int Guid = 33463;
    private static final int Batis = 30332;

    // Mobs to hunt
    private static final int Manashen = 20563;
    private static final int Monstereye = 20564;
    private static final int StoneGolem = 20565;
    private static final int IronGolem = 20566;
    private static final int Gargoyle = 20567;
    private static final int Delivery = 19482;

    private static final String Manashen_item = "Manashen";
    private static final String Monstereye_item = "Monstereye";
    private static final String StoneGolem_item = "StoneGolem";
    private static final String IronGolem_item = "IronGolem";
    private static final String Gargoyle_item = "Gargoyle";

    @Override
    public void onLoad() {
    }

    @Override
    public void onReload() {
    }

    @Override
    public void onShutdown() {
    }

    public _10299_HowtoStandUpForYourself() {
        super(false);
        addTalkId(Guid);
        addStartNpc(gershfin);
        addTalkId(Joan);
        addTalkId(Batis);
        addStartNpc(reins);
        addStartNpc(raimon);
        addStartNpc(tobias);
        addStartNpc(Drikus);
        addStartNpc(mendius);
        addStartNpc(elinia);
        addStartNpc(ershandel);

        addKillNpcWithLog(4, Manashen_item, 5, Manashen);
        addKillNpcWithLog(4, Monstereye_item, 5, Monstereye);
        addKillNpcWithLog(4, StoneGolem_item, 5, StoneGolem);
        addKillNpcWithLog(4, IronGolem_item, 5, IronGolem);
        addKillNpcWithLog(4, Gargoyle_item, 5, Gargoyle);

        addLevelCheck(40, 49);
    }

    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc) {
        String htmltext = event;
        if (event.equalsIgnoreCase("quest_ac")) {
            st.setState(STARTED);
            st.setCond(1);
            st.playSound(SOUND_ACCEPT);
            htmltext = "0-4.htm";
        } else if (event.equalsIgnoreCase("0-3.htm")) {
            st.setState(STARTED);
            st.setCond(2);
            st.playSound(SOUND_ACCEPT);
        } else if (event.equalsIgnoreCase("2-2.htm")) {
            st.takeAllItems(Delivery);
            st.setState(STARTED);
            st.setCond(4);
            st.playSound(SOUND_ACCEPT);
        }
        return htmltext;
    }

    @Override
    public String onTalk(NpcInstance npc, QuestState st) {
        int cond = st.getCond();
        int npcId = npc.getNpcId();
        String htmltext = "noquest";

        if (npcId == raimon && st.getPlayer().getRace() == Race.human && st.getPlayer().isMageClass()) {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 0 && isAvailableFor(st.getPlayer())) {
                htmltext = "0-1re.htm";
                st.setState(STARTED);
                st.setCond(1);
                st.playSound(SOUND_ACCEPT);
            } else if (cond == 1)
                htmltext = "0-1re.htm";
        } else if (npcId == reins && st.getPlayer().getRace() == Race.human && !st.getPlayer().isMageClass()) {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 0 && isAvailableFor(st.getPlayer())) {
                htmltext = "0-1r.htm";
                st.setState(STARTED);
                st.setCond(1);
                st.playSound(SOUND_ACCEPT);
            } else if (cond == 1)
                htmltext = "0-1r.htm";
        } else if (npcId == tobias && st.getPlayer().getRace() == Race.darkelf) {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 0 && isAvailableFor(st.getPlayer())) {
                htmltext = "0-1t.htm";
                st.setState(STARTED);
                st.setCond(1);
                st.playSound(SOUND_ACCEPT);
            } else if (cond == 1)
                htmltext = "0-1t.htm";
        } else if (npcId == Drikus && st.getPlayer().getRace() == Race.orc) {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 0 && isAvailableFor(st.getPlayer())) {
                htmltext = "0-1d.htm";
                st.setState(STARTED);
                st.setCond(1);
                st.playSound(SOUND_ACCEPT);
            } else if (cond == 1)
                htmltext = "0-1d.htm";
        } else if (npcId == gershfin && st.getPlayer().getRace() == Race.kamael) {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 0 && isAvailableFor(st.getPlayer())) {
                htmltext = "0-1g.htm";
                st.setState(STARTED);
                st.setCond(1);
                st.playSound(SOUND_ACCEPT);
            } else if (cond == 1)
                htmltext = "0-1g.htm";
        } else if (npcId == elinia && st.getPlayer().getRace() == Race.elf && !st.getPlayer().isMageClass()) {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 0 && isAvailableFor(st.getPlayer())) {
                htmltext = "0-1e.htm";
                st.setState(STARTED);
                st.setCond(1);
                st.playSound(SOUND_ACCEPT);
            } else if (cond == 1)
                htmltext = "0-1e.htm";
        } else if (npcId == ershandel && st.getPlayer().getRace() == Race.elf && st.getPlayer().isMageClass()) {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 0 && isAvailableFor(st.getPlayer())) {
                htmltext = "0-1ew.htm";
                st.setState(STARTED);
                st.setCond(1);
                st.playSound(SOUND_ACCEPT);
            } else if (cond == 1)
                htmltext = "0-1ew.htm";
        } else if (npcId == mendius && st.getPlayer().getRace() == Race.dwarf) {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 0 && isAvailableFor(st.getPlayer())) {
                htmltext = "0-1m.htm";
                st.setState(STARTED);
                st.setCond(1);
                st.playSound(SOUND_ACCEPT);
            } else if (cond == 1)
                htmltext = "0-1m.htm";
        } else if (npcId == Guid) {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 1)
                htmltext = "0-1.htm";
            else if (cond == 2)
                htmltext = "0-4.htm";
        } else if (npcId == Batis) {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 2) {
                htmltext = "1-1.htm";
                st.giveItems(Delivery, 1, false);
                st.setCond(3);
            } else if (cond == 3)
                htmltext = "1-2.htm";
        } else if (npcId == Joan) {
            if (st.isCompleted())
                htmltext = "2-c.htm";
            else if (cond == 3)
                htmltext = "2-1.htm";
            else if (cond == 4)
                htmltext = "2-3.htm";
            else if (cond == 5) {
                htmltext = "2-4.htm";
                st.getPlayer().addExpAndSp(1020660, 692135);
                st.giveItems(57, 287266);
                st.exitCurrentQuest(false);
                st.playSound(SOUND_FINISH);
            }
        }
        return htmltext;

    }

    @Override
    public String onKill(NpcInstance npc, QuestState st) {
        boolean doneKill = updateKill(npc, st);

        if (doneKill) {
            st.unset(Manashen_item);
            st.unset(Monstereye_item);
            st.unset(StoneGolem_item);
            st.unset(IronGolem_item);
            st.unset(Gargoyle_item);
            st.setCond(5);
        }
        return null;
    }
}