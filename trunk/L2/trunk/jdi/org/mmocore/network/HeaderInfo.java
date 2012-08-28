package org.mmocore.network;

public final class HeaderInfo<T>
{
  private int _headerPending;
  private int _dataPending;
  private boolean _multiPacket;
  private T _client;

  public HeaderInfo<T> set(int headerPending, int dataPending, boolean multiPacket, T client)
  {
    setHeaderPending(headerPending);
    setDataPending(dataPending);
    setMultiPacket(multiPacket);
    setClient(client);
    return this;
  }

  protected boolean headerFinished()
  {
    return getHeaderPending() == 0;
  }

  protected boolean packetFinished()
  {
    return getDataPending() == 0;
  }

  private void setDataPending(int dataPending)
  {
    _dataPending = dataPending;
  }

  protected int getDataPending()
  {
    return _dataPending;
  }

  private void setHeaderPending(int headerPending)
  {
    _headerPending = headerPending;
  }

  protected int getHeaderPending()
  {
    return _headerPending;
  }

  protected void setClient(T client)
  {
    _client = client;
  }

  protected T getClient()
  {
    return _client;
  }

  private void setMultiPacket(boolean multiPacket)
  {
    _multiPacket = multiPacket;
  }

  public boolean isMultiPacket()
  {
    return _multiPacket;
  }
}