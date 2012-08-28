package net.sf.l2j.loginserver.gameserverpackets;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.loginserver.clientpackets.ClientBasePacket;

public class PlayerInGame extends ClientBasePacket
{
  private List<String> _accounts;

  public PlayerInGame(byte[] decrypt)
  {
    super(decrypt);
    _accounts = new FastList();
    int size = readH();
    for (int i = 0; i < size; i++)
    {
      _accounts.add(readS());
    }
  }

  public List<String> getAccounts()
  {
    return _accounts;
  }
}