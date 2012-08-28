package l2m.gameserver.skills.conditions;

import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.skills.Env;

public abstract class Condition
{
  public static final Condition[] EMPTY_ARRAY = new Condition[0];
  private SystemMsg _message;

  public final void setSystemMsg(int msgId)
  {
    _message = SystemMsg.valueOf(msgId);
  }

  public final SystemMsg getSystemMsg()
  {
    return _message;
  }

  public final boolean test(Env env)
  {
    return testImpl(env);
  }

  protected abstract boolean testImpl(Env paramEnv);
}