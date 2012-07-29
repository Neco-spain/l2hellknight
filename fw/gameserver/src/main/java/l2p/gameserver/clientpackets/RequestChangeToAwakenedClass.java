package l2p.gameserver.clientpackets;

import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.serverpackets.ExShowUsmVideo;
import l2p.gameserver.serverpackets.SocialAction;

/**
 * @author : Ragnarok
 * @date : 17.04.12  0:29
 */
public class RequestChangeToAwakenedClass extends L2GameClientPacket {
    private static final int SCROLL_OF_AFTERLIFE = 17600;
    private int change;

    @Override
    protected void readImpl() throws Exception {
        this.change = readD();
    }

    @Override
    protected void runImpl() throws Exception {
        final Player player = getClient().getActiveChar();
        if (player == null)
            return;
        if (change != 1)
            return;
        int registeredId = player.getVarInt("AwakenedID", -1);
        if (registeredId < 0 || registeredId > ClassId.VALUES.length || ClassId.VALUES[registeredId].getLevel() != 5)
            return;

        if (ClassId.VALUES[registeredId].childOf(player.getClassId())) { // Для обычной смены класса
            if (player.getInventory().getCountOf(SCROLL_OF_AFTERLIFE) > 0) {
                player.getInventory().removeItemByItemId(SCROLL_OF_AFTERLIFE, 1);
                player.broadCast(new SocialAction(player.getObjectId(), SocialAction.AWAKENING));
                player.setClassId(registeredId, false, false);
                player.broadcastCharInfo();
                player.unsetVar("AwakenedID");
                player.unsetVar("AwakenPrepared");
                ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                    @Override
                    public void runImpl() throws Exception {
                        player.sendPacket(new ExShowUsmVideo(ExShowUsmVideo.Q010));
                    }
                }, 15000);
            }
        } else { // if(player.getInventory().getCountOf(Камень для перерождения в любую профу)

        }
    }
}
