package l2m.commons.net.nio.impl;

import java.nio.ByteBuffer;

public abstract class MMOClient<T extends MMOConnection>
{
  private T _connection;
  private boolean isAuthed;

  public MMOClient(T con)
  {
    _connection = con;
  }

  protected void setConnection(T con)
  {
    _connection = con;
  }

  public T getConnection()
  {
    return _connection;
  }

  public boolean isAuthed()
  {
    return isAuthed;
  }

  public void setAuthed(boolean isAuthed)
  {
    this.isAuthed = isAuthed;
  }

  public void closeNow(boolean error)
  {
    if (isConnected())
      _connection.closeNow();
  }

  public void closeLater()
  {
    if (isConnected())
      _connection.closeLater();
  }

  public boolean isConnected()
  {
    return (_connection != null) && (!_connection.isClosed());
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