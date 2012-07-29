package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.serverpackets.ExCallToChangeClass;
import l2p.gameserver.serverpackets.ExShowScreenMessage;
import l2p.gameserver.serverpackets.components.NpcString;

/**
 * @author : Ragnarok
 * @date : 28.03.12  17:05
 */
public class RequestCallToChangeClass extends L2GameClientPacket {

    @Override
    protected void readImpl() throws Exception {
    }

    @Override
    protected void runImpl() throws Exception {
        Player player = getClient().getActiveChar();
        if (player == null)
            return;
        if (player.getVarB("GermunkusUSM"))
            return;
        int _cId = 0;
        for (ClassId Cl : ClassId.VALUES) {
            if (Cl.getLevel() == 5 && player.getClassId().childOf(Cl)) {
                _cId = Cl.getId();
                break;
            }
        }

        if (player.isDead()) {
            sendPacket(new ExShowScreenMessage(NpcString.YOU_CANNOT_TELEPORT_WHILE_YOU_ARE_DEAD,
                    10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, ExShowScreenMessage.STRING_TYPE,
                    -1, false), new ExCallToChangeClass(_cId, false));
            return;
        }
        player.processQuestEvent("_10338_SeizeYourDestiny", "MemoryOfDisaster", null);
    }
}
