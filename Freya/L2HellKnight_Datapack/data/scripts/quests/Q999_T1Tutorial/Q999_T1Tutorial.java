package quests.Q999_T1Tutorial;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2MonsterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.util.Util;
import gnu.trove.TIntObjectHashMap;

import java.util.HashMap;

public class Q999_T1Tutorial extends Quest
{
    private static final String qn = "Q999_T1Tutorial";
    private static final String qnTutorial = "255_Tutorial";

    private static final int RECOMMENDATION_01 = 1067;
    private static final int RECOMMENDATION_02 = 1068;
    private static final int LEAF_OF_MOTHERTREE = 1069;
    private static final int BLOOD_OF_JUNDIN = 1070;
    private static final int LICENSE_OF_MINER = 1498;
    private static final int VOUCHER_OF_FLAME = 1496;
    private static final int SOULSHOT_NOVICE = 5789;
    private static final int SPIRITSHOT_NOVICE = 5790;
    private static final int BLUE_GEM = 6353;
    private static final int SCROLL = 8594;
    private static final int DIPLOMA = 9881;

    private static final HashMap<Object, Object[]> Event = new HashMap<Object, Object[]>();
    private static final TIntObjectHashMap<Talk> Talks = new TIntObjectHashMap<Talk>();
    private static final TIntObjectHashMap<Object[]> Level2 = new TIntObjectHashMap<Object[]>();
    private static final TIntObjectHashMap<Object[]> Level6 = new TIntObjectHashMap<Object[]>();
    private static final TIntObjectHashMap<Object[]> Level10 = new TIntObjectHashMap<Object[]>();
    private static final TIntObjectHashMap<Object[]> Level15 = new TIntObjectHashMap<Object[]>();

    private static class Talk
	{
		public int raceId;
		public String[] htmlfiles;
		public int npcTyp;
		public int item;

		public Talk(int _raceId, String[] _htmlfiles, int _npcTyp, int _item)
		{
			raceId = _raceId;
			htmlfiles = _htmlfiles;
			npcTyp = _npcTyp;
			item = _item;
		}
	}

    public Q999_T1Tutorial(int id, String name, String descr)
    {
        super(id, name, descr);
        Event.put("32133_02", new Object[] {"32133-03.htm", -119692, 44504, 380, DIPLOMA, 0x7b, SOULSHOT_NOVICE  ,200,0x7c,SOULSHOT_NOVICE, 200});
        Event.put("30008_02", new Object[] {"30008-03.htm", 0, 0, 0, RECOMMENDATION_01, 0x00, SOULSHOT_NOVICE, 200, 0x00, 0, 0});
        Event.put("30008_04", new Object[] {"30008-04.htm", -84058, 243239, -3730, 0, 0x00, 0, 0, 0, 0, 0});
        Event.put("30017_02", new Object[] {"30017-03.htm", 0, 0, 0, RECOMMENDATION_02, 0x0a, SPIRITSHOT_NOVICE, 100, 0x00, 0, 0});
        Event.put("30017_04", new Object[] {"30017-04.htm", -84058, 243239, -3730, 0, 0x0a, 0, 0, 0x00, 0, 0});
        Event.put("30370_02", new Object[] {"30370-03.htm", 0, 0, 0, LEAF_OF_MOTHERTREE, 0x19, SPIRITSHOT_NOVICE, 100, 0x12, SOULSHOT_NOVICE, 200});
        Event.put("30370_04", new Object[] {"30370-04.htm", 45491, 48359, -3086, 0, 0x19, 0, 0, 0x12, 0, 0});
        Event.put("30129_02", new Object[] {"30129-03.htm", 0, 0, 0, BLOOD_OF_JUNDIN, 0x26, SPIRITSHOT_NOVICE, 100, 0x1f, SOULSHOT_NOVICE, 200});
        Event.put("30129_04", new Object[] {"30129-04.htm", 12116, 16666, -4610, 0, 0x26, 0, 0, 0x1f, 0, 0});
        Event.put("30528_02", new Object[] {"30528-03.htm", 0, 0, 0, LICENSE_OF_MINER, 0x35, SOULSHOT_NOVICE, 200, 0x00, 0, 0});
        Event.put("30528_04", new Object[] {"30528-04.htm", 115642, -178046, -941, 0, 0x35, 0, 0, 0x00, 0, 0});
        Event.put("30573_02", new Object[] {"30573-03.htm", 0, 0, 0, VOUCHER_OF_FLAME, 0x31, SPIRITSHOT_NOVICE, 100, 0x2c, SOULSHOT_NOVICE, 200});
        Event.put("30573_04", new Object[] {"30573-04.htm", -45067, -113549, -235, 0, 0x31, 0, 0, 0x2c, 0, 0});

        Talks.put(30017, new Talk(0, new String[]{"30017-01.htm", "30017-02.htm", "30017-04.htm"}, 0, 0));
		Talks.put(30008, new Talk(0, new String[]{"30008-01.htm", "30008-02.htm", "30008-04.htm"}, 0, 0));
		Talks.put(30370, new Talk(1, new String[]{"30370-01.htm", "30370-02.htm", "30370-04.htm"}, 0, 0));
		Talks.put(30129, new Talk(2, new String[]{"30129-01.htm", "30129-02.htm", "30129-04.htm"}, 0, 0));
		Talks.put(30573, new Talk(3, new String[]{"30573-01.htm", "30573-02.htm", "30573-04.htm"}, 0, 0));
		Talks.put(30528, new Talk(4, new String[]{"30528-01.htm", "30528-02.htm", "30528-04.htm"}, 0, 0));
		Talks.put(30018, new Talk(0, new String[]{"30131-01.htm", "", "30019-03a.htm", "30019-04.htm",}, 1, RECOMMENDATION_02));
		Talks.put(30019, new Talk(0, new String[]{"30131-01.htm", "", "30019-03a.htm", "30019-04.htm",}, 1, RECOMMENDATION_02));
		Talks.put(30020, new Talk(0, new String[]{"30131-01.htm", "", "30019-03a.htm", "30019-04.htm",}, 1, RECOMMENDATION_02));
		Talks.put(30021, new Talk(0, new String[]{"30131-01.htm", "", "30019-03a.htm", "30019-04.htm",}, 1, RECOMMENDATION_02));
		Talks.put(30009, new Talk(0, new String[]{"30530-01.htm", "30009-03.htm", "", "30009-04.htm",}, 1, RECOMMENDATION_01));
		Talks.put(30011, new Talk(0, new String[]{"30530-01.htm", "30009-03.htm", "", "30009-04.htm",}, 1, RECOMMENDATION_01));
		Talks.put(30012, new Talk(0, new String[]{"30530-01.htm", "30009-03.htm", "", "30009-04.htm",}, 1, RECOMMENDATION_01));
		Talks.put(30056, new Talk(0, new String[]{"30530-01.htm", "30009-03.htm", "", "30009-04.htm",}, 1, RECOMMENDATION_01));
		Talks.put(30400, new Talk(1, new String[]{"30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm",}, 1, LEAF_OF_MOTHERTREE));
		Talks.put(30401, new Talk(1, new String[]{"30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm",}, 1, LEAF_OF_MOTHERTREE));
		Talks.put(30402, new Talk(1, new String[]{"30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm",}, 1, LEAF_OF_MOTHERTREE));
		Talks.put(30403, new Talk(1, new String[]{"30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm",}, 1, LEAF_OF_MOTHERTREE));
		Talks.put(30131, new Talk(2, new String[]{"30131-01.htm", "30131-03.htm", "30131-03a.htm", "30131-04.htm",}, 1, BLOOD_OF_JUNDIN));
		Talks.put(30404, new Talk(2, new String[]{"30131-01.htm", "30131-03.htm", "30131-03a.htm", "30131-04.htm",}, 1, BLOOD_OF_JUNDIN));
		Talks.put(30574, new Talk(3, new String[]{"30575-01.htm", "30575-03.htm", "30575-03a.htm", "30575-04.htm",}, 1, VOUCHER_OF_FLAME));
		Talks.put(30575, new Talk(3, new String[]{"30575-01.htm", "30575-03.htm", "30575-03a.htm", "30575-04.htm",}, 1, VOUCHER_OF_FLAME));
		Talks.put(30530, new Talk(4, new String[]{"30530-01.htm", "30530-03.htm", "", "30530-04.htm",}, 1, LICENSE_OF_MINER));
		Talks.put(32133, new Talk(5, new String[]{"32133-01.htm", "32133-02.htm", "32133-04.htm"}, 0, 0));
		Talks.put(32134, new Talk(5, new String[]{"32134-01.htm", "32134-03.htm", "", "32134-04.htm",}, 1, DIPLOMA));

        Level2.put(30598, new Object[] {"30598-L2.htm",-84436,242793,-3728,"1_LettersOfLove1"});
        Level2.put(30599, new Object[] {"30599-L2.htm",42978,49115,-2992,"2_WhatWomenWant1"});
        Level2.put(30600, new Object[] {"30600-L2.htm",25856,10832,-3720,"166_DarkMass"});
        Level2.put(30601, new Object[] {"30601-L2.htm",112656,-174864,-608,"5_MinersFavor"});
        Level2.put(30602, new Object[] {"30602-L2.htm",-47360,-113791,-224,"4_LongLiveLordOfFlame"});
        Level2.put(32135, new Object[] {"32135-L2.htm",-119378,49242,8,"174_SupplyCheck"});

        Level6.put(30598, new Object[] {"30598-L6.htm",-82236,241573,-3728,"257_GuardIsBusy1"});
        Level6.put(30599, new Object[] {"30599-L6.htm",42812,51138,-2992,"260_HuntForOrcs1"});
        Level6.put(30600, new Object[] {"30600-L6.htm",7644,18048,-4376,"265_ChainsOfSlavery"});
        Level6.put(30601, new Object[] {"30601-L6.htm",116103,-178407,-944,"293_HiddenVein"});
        Level6.put(30602, new Object[] {"30602-L6.htm",-46802,-114011,-112,"273_InvadersOfHolyland"});
        Level6.put(32135, new Object[] {"32135-L6.htm",-119378,49242,8,"281_HeadForTheHills"});

        Level10.put(0, new Object[] {"human_fighter_lv10.htm",-71384,258304,-3104,"101_SwordOfSolidarity"});
        Level10.put(10, new Object[] {"human_mage_lv10.htm",-91008,248060,-3560,"104_SpiritOfMirrors"});
        Level10.put(18, new Object[] {"elf_lv10.htm",47595,51569,-2992,"105_SkirmishWithOrcs"});
        Level10.put(25, new Object[] {"elf_lv10.htm",47595,51569,-2992,"105_SkirmishWithOrcs"});
        Level10.put(31, new Object[] {"delf_fighter_lv10.htm",10580,17574,-4552,"103_SpiritOfCraftsman"});
        Level10.put(38, new Object[] {"delf_mage_lv10.htm",10775,14190,-4240,"106_ForgottenTruth"});
        Level10.put(44, new Object[] {"orc_lv10.htm",-46808,-113184,-112,"107_MercilessPunishment"});
        Level10.put(49, new Object[] {"orc_lv10.htm",-46808,-113184,-112,"107_MercilessPunishment"});
        Level10.put(53, new Object[] {"dwarven_fighter_lv10.htm",115717,-183488,-1472,"108_JumbleTumbleDiamondFuss"});
        Level10.put(123, new Object[] {"kamael_fighter_lv10.htm",-118080,42835,712,"175_TheWayOfTheWarrior"});
        Level10.put(124, new Object[] {"kamael_fighter_lv10.htm",-118080,42835,712,"175_TheWayOfTheWarrior"});

        Level15.put(30598, new Object[] {"30598-L15.htm",-84057,242832,-3728,"151_SaveMySister1"});
        Level15.put(30599, new Object[] {"30599-L15.htm",45859,50827,-3056,"261_DreamOfMoneylender1"});
        Level15.put(30600, new Object[] {"30600-L15.htm",11258,14431,-4240,"169_NightmareChildren"});
        Level15.put(30601, new Object[] {"30601-L15.htm",116268,-177524,-880,"296_SilkOfTarantula"});
        Level15.put(30602, new Object[] {"30602-L15.htm",-45863,-112621,-200,"276_HestuiTotem"});
        Level15.put(32135, new Object[] {"32135-L15.htm",-125872,38208,1232,"283_TheFewTheProudTheBrave"});

        int[] nps_ids = {30008,30009,30017,30019,30129,30131,30573,30575,30370,30528,30530,30400,30401,30402,30403,30404,30600,30601,30602,30598,30599,32133,32134,32135};
        
        for (int i : nps_ids )
        {
            addStartNpc(i);
            addFirstTalkId(i);
            addTalkId(i);
        }
        addKillId(18342);
        addKillId(20001);
    }

    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        QuestState st = player.getQuestState(qn);
        if (st == null) return null;
        QuestState qs = st.getPlayer().getQuestState(qnTutorial);
        if (qs == null) return null;
        int Ex = qs.getInt("Ex");
        if (event.equals("TimerEx_NewbieHelper"))
        {
            if (Ex == 0)
            {
                 if (player.getClassId().isMage())
                    st.playTutorialVoice("tutorial_voice_009b");
                 else
                    st.playTutorialVoice("tutorial_voice_009a");
                 qs.set("Ex","1");
            }
            else if (Ex == 3)
            {
                 st.playTutorialVoice("tutorial_voice_010a");
                 qs.set("Ex","4");
            }
            return null;
        }
        else if (event.equals("TimerEx_GrandMaster"))
        {
            if (Ex >= 4)
            {
                st.showQuestionMark(7);
                st.playSound("ItemSound.quest_tutorial");
                st.playTutorialVoice("tutorial_voice_025");
            }
            return null;
        }
        else if (event.equals("isle"))
        {
            st.addRadar(-119692,44504,380);
            st.getPlayer().teleToLocation(-120050,44500,360);
            event = "<html><body>"+npc.getName()+":<br>Go to the <font color=\"LEVEL\">Isle of Souls</font> and meet the <font color=\"LEVEL\">Newbie Guide</font> there to learn a number of important tips. He will also give you an item to assist your development. <br>Follow the direction arrow above your head and it will lead you to the Newbie Guide. Good luck!</body></html>";
        }
        else
        {
            Object[] map = Event.get(event);
            event = (String)map[0];
            final int radarX = (Integer)map[1];
            final int radarY = (Integer)map[2];
            final int radarZ = (Integer)map[3];
            final int item = (Integer)map[4];
            final int classId1 = (Integer)map[5];
            final int gift1 = (Integer)map[6];
            final int count1 = (Integer)map[7];
            final int classId2 = (Integer)map[8];
            final int gift2 = (Integer)map[9];
            final int count2 = (Integer)map[10];
            if (radarX != 0)
                st.addRadar(radarX,radarY,radarZ);
            if (st.getQuestItemsCount(item) != 0 && st.getInt("onlyone") == 0)
            {
                st.addExpAndSp(0,50);
                st.startQuestTimer("TimerEx_GrandMaster",60000);
                st.takeItems(item,1);
                if (Ex <= 3)
                    qs.set("Ex","4");
                if (st.getPlayer().getClassId().getId() == classId1)
                {
                    st.giveItems(gift1,count1);
                    if (gift1 == SPIRITSHOT_NOVICE)
                        st.playTutorialVoice("tutorial_voice_027");
                    else
                        st.playTutorialVoice("tutorial_voice_026");
                 }
                 else if (st.getPlayer().getClassId().getId() == classId2)
                 {
                    if (gift2 != 0)
                    {
                        st.giveItems(gift2,count2);
                        st.playTutorialVoice("tutorial_voice_026");
                    }
                 }
                 st.unset("step");
                 st.set("onlyone","1");
            }
        }
        return event;
    }

    public final String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        QuestState qs = player.getQuestState(qnTutorial);
        if (qs == null)
        {
            npc.showChatWindow(player);
            return null;
        }
        QuestState st = player.getQuestState(qn);
        if (st == null)
            st = newQuestState(player);
        String htmltext = "";
        final int Ex = qs.getInt("Ex");
        final int step = st.getInt("step");
        final int onlyone = st.getInt("onlyone");
        Talk talk = Talks.get(npc.getNpcId());
        if ((player.getLevel() >= 10 || onlyone != 0) && talk != null && talk.npcTyp == 1)
            htmltext = "30575-05.htm";
        else if (Util.contains( new int[] {30600, 30601, 30602, 30598, 30599, 32135}, npc.getNpcId()))
        {
            int reward = qs.getInt("reward");
            if (reward == 0)
            {
                if (player.getClassId().isMage())
                {
                    st.playTutorialVoice("tutorial_voice_027");
                    st.giveItems(SPIRITSHOT_NOVICE,100);
                }
                else
                {
                    st.playTutorialVoice("tutorial_voice_026");
                    st.giveItems(SOULSHOT_NOVICE,200);
                }
                if (player.getLevel() < 2)
                    st.addExpAndSp(68,50);
                else
                    st.addExpAndSp(0,50);
                st.giveItems(SCROLL,2);
                qs.set("reward","1");
            }
            if (player.getLevel() < 6 && st.getInt("Lv2") == 0)
            {
                if (Level2.containsKey(npc.getNpcId()))
                {
                    Object[] map = Level2.get(npc.getNpcId());
                    htmltext = (String)map[0];
                    int x = (Integer)map[1];
                    int y = (Integer)map[2];
                    int z = (Integer)map[3];
                    String questName = (String)map[4];
                    st.addRadar(x,y,z);
                    QuestState lvl2st = player.getQuestState(questName);
                    if (lvl2st != null && lvl2st.isCompleted() && st.getInt("Lv2") == 0)
                    {
                        st.giveItems(57,695);
                        st.addExpAndSp(3154,127);
                        st.set("Lv2","1");
                        if (st.getInt("nc6") == 0)
                        {
                            st.set("nc6","1");
                            st.getPlayer().getRadar().removeAllMarkers();
                            htmltext = "NewbieCoupons.htm";
                        }
                        else if (Level6.containsKey(npc.getNpcId()))
                        {
                            map = Level2.get(npc.getNpcId());
                            htmltext = (String)map[0];
                            x = (Integer)map[1];
                            y = (Integer)map[2];
                            z = (Integer)map[3];
                            st.addRadar(x,y,z);
                        }
                    }
                }
            }
            if (player.getLevel() >= 6 && player.getLevel() < 10 && st.getInt("Lv6") == 0)
            {
                if (st.getInt("nc6") == 0 && player.getLevel() == 6)
                {
                    st.set("nc6","1");
                    htmltext = "NewbieCoupons.htm";
                }
                else if (Level6.containsKey(npc.getNpcId()))
                {
                    Object[] map = Level6.get(npc.getNpcId());
                    htmltext = (String)map[0];
                    int x = (Integer)map[1];
                    int y = (Integer)map[2];
                    int z = (Integer)map[3];
                    st.addRadar(x,y,z);
                    if (player.getNewbie() >= 4 && player.getNewbie() < 8 && st.getInt("Lv6") == 0)
                    {
                        switch(player.getLevel())
                        {
                            case 6:
                                st.giveItems(57,12928);
                                st.addExpAndSp(42191,1753);
                                break;
                            case 7:
                                st.giveItems(57,11567);
                                st.addExpAndSp(36942,1541);
                                break;
                            case 8:
                                st.giveItems(57,9290);
                                st.addExpAndSp(28806,1207);
                                break;
                            case 9:
                                st.giveItems(57,5563);
                                st.addExpAndSp(16851,711);
                                break;
                        }
                        st.set("Lv6","1");
                        if (Level10.containsKey(player.getClassId().ordinal()))
                        {
                            map = Level10.get(player.getClassId().ordinal());
                            htmltext = (String)map[0];
                            x = (Integer)map[1];
                            y = (Integer)map[2];
                            z = (Integer)map[3];
                            st.addRadar(x,y,z);
                        }
                    }
                }
            }
            if (player.getLevel() >= 10 && player.getLevel() < 15 && st.getInt("Lv10") == 0 && Level10.containsKey(player.getClassId().ordinal()))
            {
                Object[] map = Level10.get(player.getClassId().ordinal());
                htmltext = (String)map[0];
                int x = (Integer)map[1];
                int y = (Integer)map[2];
                int z = (Integer)map[3];
                String questName = (String)map[4];
                st.addRadar(x,y,z);
                QuestState lvl10st = player.getQuestState(questName);
                if (lvl10st != null && lvl10st.isCompleted() && st.getInt("Lv10") == 0)
                {
                    switch(player.getLevel())
                    {
                        case 10:
                            st.giveItems(57,43054);
                            st.addExpAndSp(206101,9227);
                            break;
                        case 11:
                            st.giveItems(57,38180);
                            st.addExpAndSp(183128,8242);
                            break;
                        case 12:
                            st.giveItems(57,31752);
                            st.addExpAndSp(152653,6914);
                            break;
                        case 13:
                            st.giveItems(57,23468);
                            st.addExpAndSp(113137,5161);
                            break;
                        case 14:
                            st.giveItems(57,695);
                            st.addExpAndSp(3154,127);
                            break;
                    }
                    st.set("Lv10","1");
                    if (Level15.containsKey(npc.getNpcId()))
                    {
                        map = Level15.get(npc.getNpcId());
                        htmltext = (String)map[0];
                        x = (Integer)map[1];
                        y = (Integer)map[2];
                        z = (Integer)map[3];
                        st.addRadar(x,y,z);
                    }
                }
            }
            if (player.getLevel() >= 15 && player.getLevel() < 18 && st.getInt("Lv15") == 0 && Level15.containsKey(npc.getNpcId()))
            {
                final Object[] map = Level15.get(npc.getNpcId());
                htmltext = (String)map[0];
                final int x = (Integer)map[1];
                final int y = (Integer)map[2];
                final int z = (Integer)map[3];
                final String questName = (String)map[4];
                st.addRadar(x,y,z);
                QuestState lvl15st = player.getQuestState(questName);
                if (lvl15st != null && lvl15st.isCompleted() && st.getInt("Lv15") == 0)
                {
                    switch(player.getLevel())
                    {
                        case 15:
                            st.giveItems(57,13648);
                            st.addExpAndSp(285670,58155);
                            break;
                        case 16:
                            st.giveItems(57,10018);
                            st.addExpAndSp(208133,42237);
                            break;
                        case 17:
                            st.giveItems(57,22996);
                            st.addExpAndSp(113712,5518);
                            break;
                    }
                    st.set("Lv15","1");
                    htmltext = "";
                }
            }
            npc.showChatWindow(player);
        }
        else if (onlyone == 0 && player.getLevel() < 10)
        {
            if (talk != null && player.getRace().ordinal() == talk.raceId)
            {
                htmltext = talk.htmlfiles[0];
                if (talk.npcTyp == 1)
                {
                    if (step == 0 && Ex < 0)
                    {
                        qs.set("Ex","0");
                        st.startQuestTimer("TimerEx_NewbieHelper",30000);
                        if (player.getClassId().isMage())
                        {
                            st.set("step","1");
                            st.setState(State.STARTED);
                        }
                        else
                        {
                            htmltext="30530-01.htm";
                            st.set("step","1");
                            st.setState(State.STARTED);
                        }
                    }
                    else if (step == 1 && st.getQuestItemsCount(talk.item) == 0 && Ex <= 2)
                    {
                        if (st.getQuestItemsCount(BLUE_GEM) != 0)
                        {
                            st.takeItems(BLUE_GEM, -1);
                            st.giveItems(talk.item,1);
                            st.set("step","2");
                            qs.set("Ex","3");
                            st.startQuestTimer("TimerEx_NewbieHelper",30000);
                            qs.set("ucMemo","3");
                            if (player.getClassId().isMage())
                            {
                                st.playTutorialVoice("tutorial_voice_027");
                                st.giveItems(SPIRITSHOT_NOVICE,100);
                                htmltext = talk.htmlfiles[2];
                                if (htmltext.equals(""))
                                    htmltext = "<html><body>I am sorry.  I only help warriors.  Please go to another Newbie Helper who may assist you.</body></html>";
                            }
                            else
                            {
                                st.playTutorialVoice("tutorial_voice_026");
                                st.giveItems(SOULSHOT_NOVICE,200);
                                htmltext = talk.htmlfiles[1];
                                if (htmltext.equals(""))
                                    htmltext = "<html><body>I am sorry.  I only help mystics.  Please go to another Newbie Helper who may assist you.</body></html>";
                            }
                        }
                        else
                        {
                            if (player.getClassId().isMage())
                                htmltext = "30131-02.htm";
                            if (player.getRace().ordinal() == 3)
                                htmltext = "30575-02.htm";
                            else
                                htmltext = "30530-02.htm";
                        }
                    }
                    else if (step == 2)
                        htmltext = talk.htmlfiles[3];
                }
                else if (talk.npcTyp == 0)
                {
                    if (step==1)
                        htmltext = talk.htmlfiles[0];
                    else if (step==2)
                        htmltext = talk.htmlfiles[1];
                    else if (step==3)
                        htmltext = talk.htmlfiles[2];
                }
            }
        }
        else if (st.isCompleted() && talk.npcTyp == 0)
            htmltext = npc.getNpcId()+"-04.htm";
        if (htmltext == null || htmltext.equals(""))
            npc.showChatWindow(player);
        return htmltext;
    }

    public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        QuestState st = player.getQuestState(qn);
        if (st == null) return super.onKill(npc, player, isPet);
        QuestState qs = st.getPlayer().getQuestState(qnTutorial);
        if (qs == null) return super.onKill(npc, player, isPet);
        int Ex = qs.getInt("Ex");
        if ( Ex <= 1 )
        {
            st.playTutorialVoice("tutorial_voice_011");
            st.showQuestionMark(3);
            qs.set("Ex","2");
        }
        if ( Ex <= 2 && st.getQuestItemsCount(BLUE_GEM) < 1 && npc instanceof L2MonsterInstance)
        {
            st.dropItem((L2MonsterInstance)npc,player,BLUE_GEM,1);
            st.playSound("ItemSound.quest_tutorial");
        }
        return super.onKill(npc, player, isPet);
    }

    public static void main(String[] args)
    {
        new Q999_T1Tutorial(-1, qn, "Tutorial");
    }
}
