package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;

/**
 * @author : Ragnarok
 * @date : 20.12.10    20:09
 */
public class ExVoteSystemInfo extends L2GameServerPacket {
    private L2Player player;

    public ExVoteSystemInfo(L2Player player) {
        this.player = player;
    }

    @Override
    protected void writeImpl() {
		writeC(EXTENDED_PACKET);
        writeH(0xC9);
        writeD(player.getRecomLeft());// осталось рекомендаций,
        writeD(player.getRecomHave());// получил рекомендаций,
        writeD(player.getRecomTimeLeft());// оставшееся время
        writeD(player.getRecomExpBonus());// прибавка к опыту
        writeD(player.isRecomSupportTime() ? 1 : 0);// 0 - обычный отсчет времени, 1 - поддержка
    }
}
