package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.protection.nProtect;

public class GameGuardReply extends L2GameClientPacket
{
    private static final String _C__CA_GAMEGUARDREPLY = "[C] CA GameGuardReply";
	private int[] _reply = new int[4];
    @Override
	protected void readImpl()
    {
		_reply[0] = readD(); // 32 бита дл€ ключа уникальности сервера нам хватит
		_reply[1] = readD(); // —оление... забиваем этот int чем угодно, скрываем пакеты
		_reply[2] = readD(); // ”Ќикальный идентификатор клиента 
		_reply[3] = readD(); // ѕосоленое поле, но 4-ый бит в 1 означет что запущен нехорошик и клиента надо казнить

    }

    @Override
	protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();

        if(activeChar == null)
            return;
        
		if(!nProtect.getInstance().checkGameGuardReply(getClient(), _reply))
			return;

        getClient().setGameGuardOk(true);
    }

    @Override
	public String getType()
    {
        return _C__CA_GAMEGUARDREPLY;
    }

}
