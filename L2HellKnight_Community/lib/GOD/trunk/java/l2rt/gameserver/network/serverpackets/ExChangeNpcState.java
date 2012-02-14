package l2rt.gameserver.network.serverpackets;

/**
 * @author : Ragnarok
 * @date : 19.12.10    15:40
 */
public class ExChangeNpcState extends L2GameServerPacket {
    public static final int NO_STATE = 0;//Ничего нет
    public static final int STATE1 = 1;//Красное пламя для npc 18928
    public static final int STATE2 = 2;//Зеленое пламя
    public static final int STATE3 = 3;//Синее пламя

    int objId;
    int state;

    public ExChangeNpcState(int id, int _state) {
        objId = id;
        state = _state;
    }


    @Override
    protected void writeImpl() {
		writeC(EXTENDED_PACKET);
        writeH(0xBD);
        writeD(objId);
        writeD(state);
    }
}
