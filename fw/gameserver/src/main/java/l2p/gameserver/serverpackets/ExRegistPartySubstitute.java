package l2p.gameserver.serverpackets;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 25.05.12
 * Time: 20:57
 * предположительно запрос лидеру пати на принятие найденого и согласившегося на замену игрока
 */
public class ExRegistPartySubstitute extends L2GameServerPacket {

    public ExRegistPartySubstitute() {
    }

    @Override
    protected void writeImpl() {
        writeEx(0x105);
        //writeD(0);
    }
}
