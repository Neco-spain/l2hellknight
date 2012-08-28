package net.sf.l2j.loginserver.serverpackets;

import net.sf.l2j.loginserver.L2LoginClient;

public final class Init extends L2LoginServerPacket
{
  private int _sessionId;
  private byte[] _publicKey;
  private byte[] _blowfishKey;

  public Init(L2LoginClient client)
  {
    this(client.getScrambledModulus(), client.getBlowfishKey(), client.getSessionId());
  }

  public Init(byte[] publickey, byte[] blowfishkey, int sessionId)
  {
    _sessionId = sessionId;
    _publicKey = publickey;
    _blowfishKey = blowfishkey;
  }

  protected void write()
  {
    writeC(0);

    writeD(_sessionId);
    writeD(50721);

    writeB(_publicKey);

    writeD(702387534);
    writeD(2009308412);
    writeD(-1750223328);
    writeD(129884407);

    writeB(_blowfishKey);
    writeC(0);
  }
}