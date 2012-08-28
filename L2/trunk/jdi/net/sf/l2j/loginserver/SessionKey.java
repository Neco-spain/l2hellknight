package net.sf.l2j.loginserver;

import net.sf.l2j.Config;

public class SessionKey
{
  public int playOkID1;
  public int playOkID2;
  public int loginOkID1;
  public int loginOkID2;

  public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
  {
    playOkID1 = playOK1;
    playOkID2 = playOK2;
    loginOkID1 = loginOK1;
    loginOkID2 = loginOK2;
  }

  public String toString()
  {
    return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
  }

  public boolean checkLoginPair(int loginOk1, int loginOk2)
  {
    return (loginOkID1 == loginOk1) && (loginOkID2 == loginOk2);
  }

  public boolean equals(SessionKey key)
  {
    if (Config.SHOW_LICENCE)
    {
      return (playOkID1 == key.playOkID1) && (loginOkID1 == key.loginOkID1) && (playOkID2 == key.playOkID2) && (loginOkID2 == key.loginOkID2);
    }

    return (playOkID1 == key.playOkID1) && (playOkID2 == key.playOkID2);
  }
}