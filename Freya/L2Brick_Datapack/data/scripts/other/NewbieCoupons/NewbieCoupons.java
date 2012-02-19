package other.NewbieCoupons;

import l2.brick.gameserver.datatables.MultiSell;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.network.serverpackets.ExShowScreenMessage;

public class NewbieCoupons extends Quest
{
    private static final String qn = "NewbieCoupons";
    private static final int COUPON_ONE = 7832;
    private static final int COUPON_TWO = 7833;

    private static final int[] NPCS = {30598, 30599, 30600, 30601, 30602, 30603, 31076, 31077, 32135};

    private static final int WEAPON_MULTISELL = 305986001;
    private static final int ACCESORIES_MULTISELL = 305986002;

    private static final ExShowScreenMessage msg = new ExShowScreenMessage(4153, 5000, ExShowScreenMessage.POSITION_TOP_CENTER, true, false, -1, false);

    private static final int NEWBIE_WEAPON = 16;
    private static final int NEWBIE_ACCESORY = 32;

    public NewbieCoupons(int id, String name, String descr)
    {
        super(id, name, descr);
        for (int i : NPCS)
        {
            addStartNpc(i);
            addTalkId(i);
        }
    }
    
    @Override
    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        QuestState st = player.getQuestState(qn);
        int newbie = player.getNewbie();
        int level = player.getLevel();
        int occupation_level = player.getClassId().level();
        int pkkills = player.getPkKills();
        _log.info("" + newbie);
        if (event.equals("newbie_give_weapon_coupon"))
        {
            if (level >= 6 && level <= 19 && pkkills == 0 && occupation_level == 0)
            {
                if ( ( newbie | NEWBIE_WEAPON ) != newbie )
                {
                    player.setNewbie(newbie|NEWBIE_WEAPON);
                    st.giveItems(COUPON_ONE, 5);
                    if (st.getGlobalQuestVar("NC6").equals(""))
                    {
                        player.sendPacket(msg);
                        st.saveGlobalQuestVar("NC6", "1");
                    }
                    return "30598-2.htm";
                }
                else
                    return "30598-1.htm";
            }
            else
                return "30598-3.htm";
        }
        else if (event.equals("newbie_give_armor_coupon"))
        {
            if (level >= 20 && level <= 39 && pkkills == 0 && occupation_level == 1)
            {
                if ( ( newbie | NEWBIE_ACCESORY ) != newbie)
                {
                    player.setNewbie(newbie | NEWBIE_ACCESORY);
                    st.giveItems(COUPON_TWO, 1);
                    return "30598-5.htm";
                }
                else
                    return "30598-4.htm";
            }
            else
                return "30598-6.htm";
        }
        else if (event.equals("newbie_show_weapon"))
        {
            if (level >= 6 && level <= 19 && pkkills == 0 && occupation_level == 0)
            	MultiSell.getInstance().separateAndSend(WEAPON_MULTISELL, player, npc, false);
            else
                return "30598-7.htm";
        }
        else if (event.equals("newbie_show_armor"))
        {
            if (level >= 20 && level <= 39 && pkkills == 0 && occupation_level > 0)
            	MultiSell.getInstance().separateAndSend(ACCESORIES_MULTISELL, player, npc, false);
            else
                return "30598-8.htm";
        }
        return null;
    }

    @Override
    public final String onTalk(L2Npc npc, L2PcInstance player)
    {
        QuestState st = player.getQuestState(qn);
        if (st == null)
            st = newQuestState(player);
        return "30598.htm";
    }

    
    public static void main(String[] args)
    {
        new NewbieCoupons(-1, qn, "other");
    }
}
