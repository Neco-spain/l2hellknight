package net.sf.l2j.loginserver;

import java.nio.ByteBuffer;

import net.sf.l2j.loginserver.L2LoginClient.LoginClientState;
import net.sf.l2j.loginserver.clientpackets.AuthGameGuard;
import net.sf.l2j.loginserver.clientpackets.RequestAuthLogin;
import net.sf.l2j.loginserver.clientpackets.RequestServerList;
import net.sf.l2j.loginserver.clientpackets.RequestServerLogin;

import org.mmocore.network.IPacketHandler;
import org.mmocore.network.ReceivablePacket;

public final class L2LoginPacketHandler implements IPacketHandler<L2LoginClient>
{
	public ReceivablePacket<L2LoginClient> handlePacket(ByteBuffer buf, L2LoginClient client)
	{
		int opcode = buf.get() & 0xFF;

		ReceivablePacket<L2LoginClient> packet = null;
		LoginClientState state = client.getState();

		switch (state)
		{
			case CONNECTED:
				if (opcode == 0x07)
				{
					packet = new AuthGameGuard();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
			case AUTHED_GG:
				if (opcode == 0x00)
				{
					packet = new RequestAuthLogin();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
			case AUTHED_LOGIN:
				if (opcode == 0x05)
				{
					packet = new RequestServerList();
				}
				else if (opcode == 0x02)
				{
					packet = new RequestServerLogin();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
		}
		return packet;
	}

	private void debugOpcode(int opcode, LoginClientState state)
	{
		System.out.println("Unknown Opcode: "+opcode+" for state: "+state.name());
	}
}
