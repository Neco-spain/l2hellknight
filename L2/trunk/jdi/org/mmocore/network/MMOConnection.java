package org.mmocore.network;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

public class MMOConnection<T extends MMOClient>
{
  private final SelectorThread<T> _selectorThread;
  private T _client;
  private ISocket _socket;
  private WritableByteChannel _writableByteChannel;
  private ReadableByteChannel _readableByteChannel;
  private NioNetStackList<SendablePacket<T>> _sendQueue = new NioNetStackList();
  private SelectionKey _selectionKey;
  private int _readHeaderPending;
  private ByteBuffer _readBuffer;
  private ByteBuffer _primaryWriteBuffer;
  private ByteBuffer _secondaryWriteBuffer;
  private boolean _pendingClose;
  private final InetAddress _address;

  public MMOConnection(SelectorThread<T> selectorThread, ISocket socket, SelectionKey key)
  {
    _selectorThread = selectorThread;
    setSocket(socket);
    setWritableByteChannel(socket.getWritableByteChannel());
    setReadableByteChannel(socket.getReadableByteChannel());
    setSelectionKey(key);
    _address = socket.getInetAddress();
  }

  public MMOConnection(T client, SelectorThread<T> selectorThread, ISocket socket, SelectionKey key)
  {
    this(selectorThread, socket, key);
    setClient(client);
  }

  public MMOConnection(SelectorThread<T> selectorThread)
  {
    _selectorThread = selectorThread;
    _address = null;
  }

  protected void setClient(T client)
  {
    _client = client;
  }

  public T getClient()
  {
    return _client;
  }

  public void sendPacket(SendablePacket<T> sp)
  {
    sp.setClient(getClient());

    if (_pendingClose) {
      return;
    }
    synchronized (getSendQueue())
    {
      getSendQueue().addLast(sp);
    }
    if ((!getSendQueue().isEmpty()) && (getSelectionKey().isValid()))
    {
      try
      {
        getSelectionKey().interestOps(getSelectionKey().interestOps() | 0x4);
      }
      catch (CancelledKeyException e)
      {
        e.printStackTrace();
      }
    }
  }

  protected SelectorThread<T> getSelectorThread()
  {
    return _selectorThread;
  }

  protected void setSelectionKey(SelectionKey key)
  {
    _selectionKey = key;
  }

  protected SelectionKey getSelectionKey()
  {
    return _selectionKey;
  }

  protected void enableReadInterest()
  {
    try
    {
      getSelectionKey().interestOps(getSelectionKey().interestOps() | 0x1);
    }
    catch (CancelledKeyException e)
    {
    }
  }

  protected void disableReadInterest()
  {
    try
    {
      getSelectionKey().interestOps(getSelectionKey().interestOps() & 0xFFFFFFFE);
    }
    catch (CancelledKeyException e)
    {
    }
  }

  protected void enableWriteInterest()
  {
    try
    {
      getSelectionKey().interestOps(getSelectionKey().interestOps() | 0x4);
    }
    catch (CancelledKeyException e)
    {
    }
  }

  protected void disableWriteInterest()
  {
    try
    {
      getSelectionKey().interestOps(getSelectionKey().interestOps() & 0xFFFFFFFB);
    }
    catch (CancelledKeyException e)
    {
    }
  }

  protected void setSocket(ISocket socket)
  {
    _socket = socket;
  }

  public ISocket getSocket()
  {
    return _socket;
  }

  protected void setWritableByteChannel(WritableByteChannel wbc)
  {
    _writableByteChannel = wbc;
  }

  public WritableByteChannel getWritableChannel()
  {
    return _writableByteChannel;
  }

  protected void setReadableByteChannel(ReadableByteChannel rbc)
  {
    _readableByteChannel = rbc;
  }

  public ReadableByteChannel getReadableByteChannel()
  {
    return _readableByteChannel;
  }

  protected NioNetStackList<SendablePacket<T>> getSendQueue()
  {
    return _sendQueue;
  }

  protected void createWriteBuffer(ByteBuffer buf)
  {
    if (_primaryWriteBuffer == null)
    {
      _primaryWriteBuffer = getSelectorThread().getPooledBuffer();
      _primaryWriteBuffer.put(buf);
    }
    else
    {
      ByteBuffer temp = getSelectorThread().getPooledBuffer();
      temp.put(buf);

      int remaining = temp.remaining();
      _primaryWriteBuffer.flip();
      int limit = _primaryWriteBuffer.limit();

      if (remaining >= _primaryWriteBuffer.remaining())
      {
        temp.put(_primaryWriteBuffer);
        getSelectorThread().recycleBuffer(_primaryWriteBuffer);
        _primaryWriteBuffer = temp;
      }
      else
      {
        _primaryWriteBuffer.limit(remaining);
        temp.put(_primaryWriteBuffer);
        _primaryWriteBuffer.limit(limit);
        _primaryWriteBuffer.compact();
        _secondaryWriteBuffer = _primaryWriteBuffer;
        _primaryWriteBuffer = temp;
      }
    }
  }

  protected boolean hasPendingWriteBuffer()
  {
    return _primaryWriteBuffer != null;
  }

  protected void movePendingWriteBufferTo(ByteBuffer dest)
  {
    _primaryWriteBuffer.flip();
    dest.put(_primaryWriteBuffer);
    getSelectorThread().recycleBuffer(_primaryWriteBuffer);
    _primaryWriteBuffer = _secondaryWriteBuffer;
    _secondaryWriteBuffer = null;
  }

  protected ByteBuffer getWriteBuffer()
  {
    ByteBuffer ret = _primaryWriteBuffer;
    if (_secondaryWriteBuffer != null)
    {
      _primaryWriteBuffer = _secondaryWriteBuffer;
      _secondaryWriteBuffer = null;
    }
    return ret;
  }

  protected void setPendingHeader(int size)
  {
    _readHeaderPending = size;
  }

  protected int getPendingHeader()
  {
    return _readHeaderPending;
  }

  protected void setReadBuffer(ByteBuffer buf)
  {
    _readBuffer = buf;
  }

  protected ByteBuffer getReadBuffer()
  {
    return _readBuffer;
  }

  public boolean isClosed()
  {
    return _pendingClose;
  }

  protected void closeNow()
  {
    synchronized (getSendQueue())
    {
      if (!isClosed())
      {
        _pendingClose = true;
        getSendQueue().clear();
        disableWriteInterest();
        getSelectorThread().closeConnection(this);
      }
    }
  }

  public void close(SendablePacket<T> sp)
  {
    synchronized (getSendQueue())
    {
      if (!isClosed())
      {
        getSendQueue().clear();
        sendPacket(sp);
        _pendingClose = true;
        getSelectorThread().closeConnection(this);
      }
    }
  }

  protected void closeLater()
  {
    synchronized (getSendQueue())
    {
      if (!isClosed())
      {
        _pendingClose = true;
        getSelectorThread().closeConnection(this);
      }
    }
  }

  protected void releaseBuffers()
  {
    if (_primaryWriteBuffer != null)
    {
      getSelectorThread().recycleBuffer(_primaryWriteBuffer);
      _primaryWriteBuffer = null;
      if (_secondaryWriteBuffer != null)
      {
        getSelectorThread().recycleBuffer(_secondaryWriteBuffer);
        _secondaryWriteBuffer = null;
      }
    }
    if (_readBuffer != null)
    {
      getSelectorThread().recycleBuffer(_readBuffer);
      _readBuffer = null;
    }
  }

  protected void onDisconnection()
  {
    getClient().onDisconnection();
  }

  protected void onForcedDisconnection()
  {
    getClient().onForcedDisconnection();
  }

  public final InetAddress getInetAddress()
  {
    return _address;
  }

  final int write(ByteBuffer buf) throws IOException
  {
    if ((_writableByteChannel != null) && (_writableByteChannel.isOpen()))
      return _writableByteChannel.write(buf);
    return -1;
  }
}