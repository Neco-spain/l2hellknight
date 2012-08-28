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
		_reply[0] = readD(); // 32 ���� ��� ����� ������������ ������� ��� ������
		_reply[1] = readD(); // �������... �������� ���� int ��� ������, �������� ������
		_reply[2] = readD(); // ���������� ������������� ������� 
		_reply[3] = readD(); // ��������� ����, �� 4-�� ��� � 1 ������� ��� ������� ��������� � ������� ���� �������

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
