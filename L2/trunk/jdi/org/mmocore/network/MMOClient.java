package org.mmocore.network;

import java.nio.ByteBuffer;

public abstract class MMOClient<T extends MMOConnection>
{
  private T _connection;

  public MMOClient(T con)
  {
    setConnection(con);
    con.setClient(this);
  }

  public void setConnection(T con)
  {
    _connection = con;
  }

  public T getConnection()
  {
    return _connection;
  }

  public void closeNow()
  {
    getConnection().closeNow();
  }

  public void closeLater()
  {
    getConnection().closeLater();
  }

  public abstract boolean decrypt(ByteBuffer paramByteBuffer, int paramInt);

  public abstract boolean encrypt(ByteBuffer paramByteBuffer, int paramInt);

  protected void onDisconnection()
  {
  }

  protected void onForcedDisconnection()
  {
  }
}