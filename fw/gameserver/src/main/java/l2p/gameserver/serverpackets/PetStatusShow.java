package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Summon;

public class PetStatusShow extends L2GameServerPacket {
    private int summonType;
    private int summonObjId;

    public PetStatusShow(Summon summon) {
        summonType = summon.getSummonType();
        summonObjId = summon.getObjectId();
    }

    @Override
    protected final void writeImpl() {
        writeC(0xb1);
        writeD(summonType);
		writeD(summonObjId);
    }
}