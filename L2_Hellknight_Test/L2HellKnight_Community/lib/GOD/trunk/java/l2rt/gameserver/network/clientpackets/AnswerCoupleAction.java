package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ExRotation;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.util.Util;
/**
 * @author : Ragnarok
 * @date : 19.12.10    13:05
 */
public class AnswerCoupleAction extends L2GameClientPacket {
    private int requesterId;
    private int answer;
    private int coupleId;

    @Override
    public void readImpl()
	{
        coupleId = readD();// ид социалки
        answer = readD();// ответ, 0 - не принял, 1 - принял.
        requesterId = readD();// objId того, кто запрашивал
    }

    @Override
    public void runImpl()
	{
        L2Player requester = null;
        L2Player cha = getClient().getActiveChar();
        requester = L2ObjectsStorage.getPlayer(requesterId);

        if (requester == null || cha == null)
            return;

        if (answer == 0)
            cha.sendPacket(new SystemMessage(3119));
		else if (answer == 1)
		{
            double distance = cha.getDistance(requester);
            if (distance > 2000 || distance < 70)
			{
                cha.sendPacket(new SystemMessage(3120));
                requester.sendPacket(new SystemMessage(3120));
                return;
            }

            int heading = Util.calculateHeadingFrom(requester, cha);
            cha.broadcastPacket(new ExRotation(cha.getObjectId(), heading));
            cha.setHeading(heading);
            heading = Util.calculateHeadingFrom(cha, requester);
            requester.broadcastPacket(new ExRotation(requester.getObjectId(), heading));
            requester.setHeading(heading);
            requester.sendPacket((new SystemMessage(3151)).addName(cha));
            requester.broadcastPacket(new SocialAction(requester.getObjectId(), coupleId));
            cha.broadcastPacket(new SocialAction(cha.getObjectId(), coupleId));
        }
		else if (answer == -1)
		{
            SystemMessage sm = new SystemMessage(3164);
            sm.addName(cha);
            requester.sendPacket(sm);
        }
    }

    @Override
    public String getType() 
	{
        return "[C] D0:7A AnswerCoupleAction";
    }
}
