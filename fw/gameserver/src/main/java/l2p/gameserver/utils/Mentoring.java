package l2p.gameserver.utils;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.database.mysql;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.World;
import l2p.gameserver.model.actor.instances.player.Mentee;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.mail.Mail;
import l2p.gameserver.serverpackets.ExNoticePostArrived;
import l2p.gameserver.tables.SkillTable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 12.04.12
 * Time: 18:22
 */
public class Mentoring {
    
    public static final Map<Integer, Integer> SIGN_OF_TUTOR = new HashMap<Integer, Integer>() {{
        put(10,1);        put(20,25);       put(30,30);
        put(40,63);       put(50,68);       put(51,16);
        put(52,7);        put(53,9);        put(54,11);
        put(55,13);       put(56,16);       put(57,19);
        put(58,23);       put(59,29);       put(60,37);
        put(61,51);       put(62,20);       put(63,24);
        put(64,30);       put(65,36);       put(66,44);
        put(67,55);       put(68,67);       put(69,84);
        put(70,107);      put(71,120);      put(72,92);
        put(73,114);      put(74,139);      put(75,172);
        put(76,213);      put(77,629);      put(78,322);
        put(79,413);      put(80,491);      put(81,663);
        put(82,746);      put(83,850);      put(84,987);
        put(85,1149);     put(86,2015);
    }};
    
    public static int[] effectsForMentee = {9233, 9227, 9228, 9229, 9230, 9231, 9232};
    public static int skillForMenee = 9379;
    public static int[] skillsForMentor = {9376, 9377, 9378};
    public static int effectForMentor = 9256;
    public static int[] effectsForDebuff = {9233, 9227, 9228, 9229, 9230, 9231, 9232, 9376, 9377, 9378, 9256};
    
    public static void applyMentoringCond(Player dependPlayer, boolean login)
    {
        if (login)  // чар вошел уже находится в игре
        {
            if (dependPlayer.getClassId().getId() > 138)    // чар - наставник
            {
                addMentoringSkills(dependPlayer);

                if (!dependPlayer.getEffectList().containEffectFromSkills(new int[effectForMentor]))
                    SkillTable.getInstance().getInfo(effectForMentor,1).getEffects(dependPlayer,dependPlayer,false,false); // баф себе

                for (Mentee mentee : dependPlayer.getMenteeList().getList().values())                           // баф ученикам
                {
                    Player menteePlayer = World.getPlayer(mentee.getObjectId());
                    if (menteePlayer != null)
                        for (int effect : effectsForMentee)
                            if (!menteePlayer.getEffectList().containEffectFromSkills(new int[effect]))
                                SkillTable.getInstance().getInfo(effect,1).getEffects(menteePlayer,menteePlayer,false,false);
                }
            }
            else     // чар - ученик
            {
                for (int effect : effectsForMentee)
                    if (!dependPlayer.getEffectList().containEffectFromSkills(new int[effect]))                        // баф себе
                        SkillTable.getInstance().getInfo(effect,1).getEffects(dependPlayer,dependPlayer,false,false);

                Player mentorPlayer = World.getPlayer(dependPlayer.getMenteeList().getMentor());                 // бафф наставнику
                if (mentorPlayer != null)
                    if (!mentorPlayer.getEffectList().containEffectFromSkills(new int[effectForMentor]))
                        SkillTable.getInstance().getInfo(effectForMentor,1).getEffects(mentorPlayer,mentorPlayer,false,false);
            }
        }
        else        // чар выходит игры или из системы наставничества
        {
            for (Mentee mentee : dependPlayer.getMenteeList().getList().values())
            {
                Player menteePlayer = World.getPlayer(mentee.getObjectId());
                if (menteePlayer != null)
                    if (!menteePlayer.getMenteeList().someOneOnline(false))
                        for (int buff : effectsForDebuff)
                            menteePlayer.getEffectList().stopEffect(buff);
            }
        }
    }

    public static void addMentoringSkills(Player mentoringPlayer)
    {
        if (mentoringPlayer.getMenteeList().getMentor() == 0)
            for (int skillId : skillsForMentor)                       // скиллы для наставника
            {
                Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
                mentoringPlayer.addSkill(skill, true);
                mentoringPlayer.sendSkillList();
            }
        else
        {
            Skill skill = SkillTable.getInstance().getInfo(skillForMenee, 1);   // скилл для ученика
            mentoringPlayer.addSkill(skill, true);
            mentoringPlayer.sendSkillList();
        }
    }

    public static void setTimePenalty(int mentorId, long timeTo, long expirationTime) {
        Player mentor = World.getPlayer(mentorId);
        if (mentor != null && mentor.isOnline())
            mentor.setVar("mentorPenalty", timeTo, -1);
        else
            mysql.set("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (?,'user-var','mentorPenalty',?,?)", mentorId, timeTo, expirationTime);
    }

    public static void sendMentorMail(Player receiver, Map<Integer, Long> items) {
        if (receiver == null || !receiver.isOnline())
            return;
        if (items.keySet().size() > 8)
            return;

        Mail mail = new Mail();
        mail.setSenderId(1);
        mail.setSenderName("Mentoring System");
        mail.setReceiverId(receiver.getObjectId());
        mail.setReceiverName(receiver.getName());
        mail.setTopic("Mentoring");
        mail.setBody("Sign of Tutor for Mentor");
        for (Map.Entry<Integer, Long> itm : items.entrySet()) {
            ItemInstance item = ItemFunctions.createItem(itm.getKey());
            item.setLocation(ItemInstance.ItemLocation.MAIL);
            item.setCount(itm.getValue());
            item.save();
            mail.addAttachment(item);
        }
        mail.setType(Mail.SenderType.MENTOR);
        mail.setUnread(true);
        mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
        mail.save();

        receiver.sendPacket(ExNoticePostArrived.STATIC_TRUE);
        receiver.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
    }
}
