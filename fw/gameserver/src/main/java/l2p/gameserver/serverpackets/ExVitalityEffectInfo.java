package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.Player;

/**
 * @author : Ragnarok
 * @date : 27.01.12  13:33
 */
public class ExVitalityEffectInfo extends L2GameServerPacket {
    private Player player;

    public ExVitalityEffectInfo(Player player) {
        this.player = player;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x11D);

        writeD(player.getVitality());// Остаток виталити
        writeD(player.getVitality() == 0 ? 0 : (int) (Config.ALT_VITALITY_RATE * 100));// Exp bonus
        // Один из следующих - максимальное, другой - текущее.
        writeD(7);// TODO: Remaining items count
        writeD(7);// TODO: Remaining items count
    }
}
