package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.CharSelectionInfo;

/**
 * @author : Ragnarok
 * @date : 22.01.12  11:44
 */
public class ExLoginVitalityEffectInfo extends L2GameServerPacket {

    private CharSelectionInfo charInfo;

    public ExLoginVitalityEffectInfo(CharSelectionInfo charInfo) {
        this.charInfo = charInfo;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x11E);

        writeD(charInfo.getVitalityPoints() == 0 ? 0 : (int) (Config.ALT_VITALITY_RATE * 100)); // Exp bonus
        writeD(7); // TODO: Remaining items count
    }
}
