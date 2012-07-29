package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.Request;
import l2p.gameserver.model.Request.L2RequestType;
import l2p.gameserver.model.World;
import l2p.gameserver.model.actor.instances.player.Mentee;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExMentorAdd;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;

import java.util.Map;

/**
 * @author Cain
 */
public class RequestMenteeAdd extends L2GameClientPacket {
    private String _newMentee;

    @Override
    protected void readImpl() {
        _newMentee = readS();
    }

    @Override
    protected void runImpl() {
        GameClient client = getClient();
        Player activeChar = client.getActiveChar();
        Player newMentee = World.getPlayer(_newMentee);

        // Чар онлайн?
        if (newMentee == null)
        {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE));
            return;
        }
        
        // Только после перерождения можно стать наставником
        if (activeChar.getClassId().getId() < 139)
        {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.YOU_MUST_AWAKEN_IN_ORDER_TO_BECOME_A_MENTOR));
            return;
        }

        // Уже 3 ученика у наставника
        if (activeChar.getMenteeList().getList().size() == 3)
        {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.A_MENTOR_CAN_HAVE_UP_TO_3_MENTEES_AT_THE_SAME_TIME));
            return;
        }

        // Уже есть наставник
        if (newMentee.getMenteeList().getMentor() != 0)
        {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_ALREADY_HAS_A_MENTOR).addName(newMentee));
            return;
        }

        // Выбраный чар уже в списке у наставника
        for (Map.Entry<Integer,Mentee> entry : activeChar.getMenteeList().getList().entrySet())
            if (entry.getValue().getName().equals(_newMentee))
                return;

        // Попытка стать наставником себе
        if (activeChar.getName().equals(_newMentee))
        {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.YOU_CANNOT_BECOME_YOUR_OWN_MENTEE));
            return;
        }

        // Выше 85 лвла
        if (newMentee.getLevel() > 85)
        {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_IS_ABOVE_LEVEL_86_AND_CANNOT_BECOME_A_MENTEE).addName(newMentee));
            return;
        }

        // У нового ученика нет Сертификата Подопечного
        if (!newMentee.getInventory().validateCapacity(33800,1))
        {
            activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_DOES_NOT_HAVE_THE_ITEM_NEDEED_TO_BECOME_A_MENTEE).addName(newMentee));
            return;
        }

        long mentorPenalty = activeChar.getVarLong("mentorPenalty", 0L);
        if (mentorPenalty > System.currentTimeMillis())
        {
            long milisPenalty = mentorPenalty - System.currentTimeMillis();
            double numSecs = milisPenalty / 1000 % 60;
            double countDown = (milisPenalty / 1000 - numSecs) / 60;
            int numMins = (int) Math.floor(countDown % 60);
            countDown = (countDown - numMins) / 60;
            int numHours = (int) Math.floor(countDown % 24);
            int numDays = (int) Math.floor((countDown - numHours) / 24);
            activeChar.sendPacket(new SystemMessage2(SystemMsg.YOU_CAN_BOND_WITH_A_NEW_MENTEE_IN_S1_DAYS_S2_HOUR_S3_MINUTE).addInteger(numDays).addInteger(numHours).addInteger(numMins));
            return;
        }

        new Request(L2RequestType.MENTEE, activeChar, newMentee).setTimeout(10000L);
        activeChar.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_OFFERED_TO_BECOME_S1_MENTOR).addName(newMentee));
        newMentee.sendPacket(new ExMentorAdd(activeChar));
    }
}