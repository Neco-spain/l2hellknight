package quests;

import instances.HarnakUndergroundRuins;
import instances.MemoryOfDisaster;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.scripts.ScriptFile;

import l2p.gameserver.utils.ReflectionUtils;



public class _10342_DayofDestinyElvenFate extends Quest implements ScriptFile {
                                       //NPC
    private static final int Winonin = 30856;
    private static final int Quartermaster = 33407;
    private static final int Vanguardmember = 33165;
    private static final int Adolph = 33170;

    private static final int VanguardCorpse1 = 33168;
    private static final int VanguardCorpse2 = 33166;
    private static final int VanguardCorpse3 = 33167;
    private static final int VanguardCorpse4 = 33169;
                                       //Select party
    private static final int Alice = 33171;
    private static final int Barton = 33172;
    private static final int Hayuk = 33173;
    private static final int Eliyah = 33174;
                                      //Fighter Party
    private static final int BartonF = 33354;
    private static final int HayukF = 33355;
    private static final int EliyahF = 33356;
    private static final int AliceF = 33353;
    private static final int AdolphF = 33352;

    private static final int Archer = 33414;
    private static final int Infantry = 33415;
    private static final int DefenseWall = 33416;


    private static final int INSTANCE_ID = 185;

    private static final int dogtag = 17749;


    public _10342_DayofDestinyElvenFate() {
        super(false);
        addStartNpc(Winonin);
        addTalkId(Quartermaster);
        addTalkId(Vanguardmember);
        addTalkId(Adolph);
        addTalkId(VanguardCorpse1);
        addTalkId(VanguardCorpse2);
        addTalkId(VanguardCorpse3);
        addTalkId(VanguardCorpse4);
        addTalkId(Alice);
        addTalkId(Barton);
        addTalkId(Hayuk);
        addTalkId(Eliyah);
        addTalkId(AdolphF);


        addLevelCheck(75, 99);
        //addClassLevelCheck(2);
    }
    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc) {
        String htmltext = event;
        if (event.equalsIgnoreCase("quest_accept")) {
            st.setState(STARTED);
            st.setCond(1);
            st.playSound(SOUND_ACCEPT);
            htmltext = "0-5.htm";
        }
        if (event.equalsIgnoreCase("red")) {
            htmltext = "0-7.htm";
            st.getPlayer().addExpAndSp(2050000, 0);
            st.giveItems(33771, 1);   //SA 14 RED
            st.giveItems(57, 5000000);
            st.exitCurrentQuest(false);
            st.playSound(SOUND_FINISH);
        }
        if (event.equalsIgnoreCase("blue")) {
            htmltext = "0-7.htm";
            st.getPlayer().addExpAndSp(2050000, 0);
            st.giveItems(33772, 1);   //SA 14 BLUE
            st.exitCurrentQuest(false);
            st.giveItems(57, 5000000);
            st.playSound(SOUND_FINISH);
        }
        if (event.equalsIgnoreCase("green")) {
            htmltext = "0-7.htm";
            st.getPlayer().addExpAndSp(2050000, 0);
            st.giveItems(33773, 1);   //SA 14 GREEN
            st.exitCurrentQuest(false);
            st.giveItems(57, 5000000);
            st.playSound(SOUND_FINISH);
        }
        if (event.equalsIgnoreCase("corps")) {
            htmltext = "1-2.htm";
            st.setCond(2);
            st.playSound(SOUND_MIDDLE);
        }
        if (event.equalsIgnoreCase("corps1")) {
            htmltext = "2-2.htm";
            st.set("Corp1", 1);
            st.giveItems(dogtag, 1, false);
            st.setCond(2);
            st.playSound(SOUND_MIDDLE);
            if (st.getQuestItemsCount(dogtag) >= 4) {
                st.setCond(3);
                htmltext = "2-4.htm";
            }
        }
        if (event.equalsIgnoreCase("corps2")) {
            htmltext = "2-2.htm";
            st.set("Corp2", 1);
            st.giveItems(dogtag, 1, false);
            st.setCond(2);
            st.playSound(SOUND_MIDDLE);
            if (st.getQuestItemsCount(dogtag) >= 4) {
                st.setCond(3);
                htmltext = "2-4.htm";
            }
        }
        if (event.equalsIgnoreCase("corps3")) {
            htmltext = "2-2.htm";
            st.set("Corp3", 1);
            st.giveItems(dogtag, 1, false);
            st.setCond(2);
            st.playSound(SOUND_MIDDLE);
            if (st.getQuestItemsCount(dogtag) >= 4) {
                st.setCond(3);
                htmltext = "2-4.htm";
            }
        }
        if (event.equalsIgnoreCase("corps4")) {
            htmltext = "2-2.htm";
            st.set("Corp4", 1);
            st.giveItems(dogtag, 1, false);
            st.playSound(SOUND_MIDDLE);
            if (st.getQuestItemsCount(dogtag) >= 4) {
                st.setCond(3);
                htmltext = "2-4.htm";
            }
        }
        if (event.equalsIgnoreCase("give_dogtags")) {
            htmltext = "1-5.htm";
            st.takeAllItems(dogtag);
            st.playSound(SOUND_MIDDLE);
            st.setCond(4);
        }
        if (event.equalsIgnoreCase("enter_instance")) {
            enterInstance(st.getPlayer());
            st.playSound(SOUND_MIDDLE);
                st.setCond(5);
            return null;
        }
        if (event.equalsIgnoreCase("select_team")) {
            htmltext = "8-2.htm";
            st.playSound(SOUND_MIDDLE);
            st.setCond(6);
       }

        return htmltext;
    }
    private void enterInstance(Player player)
    {
        Reflection reflection = player.getActiveReflection();
        if(reflection != null)
        {
            if(player.canReenterInstance(INSTANCE_ID))
                player.teleToLocation(reflection.getTeleportLoc(), reflection);
        }
        else if(player.canEnterInstance(INSTANCE_ID))
            ReflectionUtils.enterReflection(player, INSTANCE_ID);

    }

    @Override
    public String onTalk(NpcInstance npc, QuestState st) {
        int cond = st.getCond();
        int npcId = npc.getNpcId();
        String htmltext = "noquest";

        int corp1 = st.getInt("Corp1");
        int corp2 = st.getInt("Corp2");
        int corp3 = st.getInt("Corp3");
        int corp4 = st.getInt("Corp4");

        if (npcId == Winonin)
        {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 0 && isAvailableFor(st.getPlayer()))
                htmltext = "start.htm";
            else if (cond == 1)
                htmltext = "0-5.htm";
            else if (cond == 13)
                htmltext = "0-6.htm";

            else
                htmltext = "0-nc.htm";
    }
       else  if (npcId == Quartermaster)
        {
            if (st.isCompleted())
                htmltext = "0-c.htm";
            else if (cond == 1)       {
                htmltext = "1-1.htm";
                st.set("Corp1", 0);
                st.set("Corp2", 0);
                st.set("Corp3", 0);
                st.set("Corp4", 0);
           }
            else if (cond == 2)
            {
                htmltext = "1-3.htm";
                st.set("Corp1", 0);
                st.set("Corp2", 0);
                st.set("Corp3", 0);
                st.set("Corp4", 0);
              }
            else if (cond == 3)
            {
                htmltext = "1-4.htm";
            }
            else if (cond == 4)
            {
                htmltext = "1-5.htm";
            }
          else
                htmltext = "0-nc.htm";
        }
     else if (npcId == VanguardCorpse1)
        {
          if (cond == 2 && corp1 == 0)
                htmltext = "2-11.htm";
            else if (cond == 2 && corp1 == 1)
                htmltext = "2-3.htm";
          else if (cond == 3)
              htmltext = "2-4.htm";
            else
                htmltext = "0-nc.htm";
        }
        else if (npcId == VanguardCorpse2)
        {
            if (cond == 2 && corp2 == 0)
                htmltext = "2-12.htm";
            else if (cond == 2 && corp2 == 1)
                htmltext = "2-3.htm";
            else if (cond == 3)
                htmltext = "2-4.htm";
            else
                htmltext = "0-nc.htm";
        }
        else if (npcId == VanguardCorpse3)
        {
            if (cond == 2 && corp3 == 0)
                htmltext = "2-13.htm";
            else if (cond == 2 && corp3 == 1)
                htmltext = "2-3.htm";
            else if (cond == 3)
                htmltext = "2-4.htm";
            else
                htmltext = "0-nc.htm";
        }
        else if (npcId == VanguardCorpse4)
        {
            if (cond == 2 && corp4 == 0)
                htmltext = "2-14.htm";
            else if (cond == 2 && corp4 == 1)
                htmltext = "2-3.htm";
            else if (cond == 3)
                htmltext = "2-4.htm";
            else
                htmltext = "0-nc.htm";
        }
        
        else if (npcId == Vanguardmember)
        {
            if (cond == 4 || cond == 5 || cond == 6|| cond == 7|| cond == 8|| cond == 9|| cond == 10 || cond == 11)
             htmltext = "3-1.htm";
        }
        else if (npcId == Barton)
        {
          if (cond == 5)
              htmltext = "4-1.htm";
            else if (cond == 6)
              htmltext = "4-2.htm";
        }
        else if (npcId == Alice)
        {
            if (cond == 5)
                htmltext = "5-1.htm";
            else if (cond == 6)
                htmltext = "5-2.htm";

        }
        else if (npcId == Hayuk)
        {
            if (cond == 5)
                htmltext = "6-1.htm";
            else if (cond == 6)
                htmltext = "6-2.htm";
        }
        else if (npcId == Eliyah)
        {
            if (cond == 5)
                htmltext = "7-1.htm";
            else if (cond == 6)
                htmltext = "7-2.htm";
        }
        else if (npcId == Adolph)
        {
            if (cond == 5)
                htmltext = "8-1.htm";
            else if (cond == 6)
                htmltext = "8-2.htm";
        }
    return htmltext;
    }



    @Override
    public String onKill(NpcInstance npc, QuestState qs) {


        return null;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onReload() {
    }

    @Override
    public void onShutdown() {
    }
}
