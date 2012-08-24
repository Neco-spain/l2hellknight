/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2.hellknight.gameserver.util;

import gnu.trove.function.TObjectFunction;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Custom extension of TIntObjectHashMap that is synchronized via ReentrantReadWriteLock.<br>
 * The purpose of this map is to replace the use of FastMap<K,V>.shared() which requires a lot of resources.
 * @author Nik
 * @param <V> value object.
 */
public class L2TIntObjectHashMap<V extends Object> extends TIntObjectHashMap<V>
{
	private static final long serialVersionUID = 1L;
	
	private final Lock _readLock;
	private final Lock _writeLock;
	private boolean _tempLocksDisable;
	
	public L2TIntObjectHashMap()
	{
		super();
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		_readLock = lock.readLock();
		_writeLock = lock.writeLock();
		_tempLocksDisable = false;
	}
	
	@Override
	public V put(int key, V value)
	{
		_writeLock.lock();
		try
		{
			return super.put(key, value);
		}
		finally
		{
			_writeLock.unlock();
		}
	}
	
	/**
	 * Unsynchronized operation, its free from any locks.<br>
	 * Its useful while the readLock is taken by a thread<br>
	 * (forEach operation for example)<br>
	 * and you need to put something in the map without causing a deadlock<br>
	 * by taking the writeLock before the readLock is unlocked.
	 * @param key
	 * @param value
	 * @return
	 */
	public V unsynchronizedPut(int key, V value)
	{
		return super.put(key, value);
	}
	
	@Override
	public V get(int key)
	{
		if (!_tempLocksDisable)
		{
			_readLock.lock();
		}
		
		try
		{
			return super.get(key);
		}
		finally
		{
			if (!_tempLocksDisable)
			{
				_readLock.unlock();
			}
		}
	}
	
	@Override
	public void clear()
	{
		if (!_tempLocksDisable)
		{
			_writeLock.lock();
		}
		try
		{
			super.clear();
		}
		finally
		{
			if (!_tempLocksDisable)
			{
				_writeLock.unlock();
			}
		}
	}
	
	@Override
	public V remove(int key)
	{
		if (!_tempLocksDisable)
		{
			_writeLock.lock();
		}
		try
		{
			return super.remove(key);
		}
		finally
		{
			if (!_tempLocksDisable)
			{
				_writeLock.unlock();
			}
		}
	}
	
	/**
	 * Unsynchronized operation, its free from any locks.<br>
	 * Its useful while the readLock is taken by a thread (forEach operation for example)<br>
	 * and you need to remove something in the map without causing a deadlock<br>
	 * by taking the writeLock before the readLock is unlocked.
	 * @param key
	 * @return
	 */
	public V unsynchronizedRemove(int key)
	{
		return super.remove(key);
	}
	
	@Override
	public boolean equals(Object other)
	{
		_readLock.lock();
		try
		{
			return super.equals(other);
		}
		finally
		{
			_readLock.unlock();
		}
	}
	
	@Override
	public V[] values()
	{
		if (!_tempLocksDisable)
		{
			_readLock.lock();
		}
		try
		{
			return super.values();
		}
		finally
		{
			if (!_tempLocksDisable)
			{
				_readLock.unlock();
			}
		}
	}
	
	@Override
	public V[] values(V[] arg0)
	{
		if (!_tempLocksDisable)
		{
			_readLock.lock();
		}
		try
		{
			return super.values(arg0);
		}
		finally
		{
			if (!_tempLocksDisable)
			{
				_readLock.unlock();
			}
		}
	}
	
	@Override
	public Collection<V> valueCollection()
	{
		_readLock.lock();
		try
		{
			return super.valueCollection();
		}
		finally
		{
			_readLock.unlock();
		}
	}
	
	@Override
	public int[] keys()
	{
		_readLock.lock();
		try
		{
			return super.keys();
		}
		finally
		{
			_readLock.unlock();
		}
	}
	
	@Override
	public int[] keys(int[] arg0)
	{
		_readLock.lock();
		try
		{
			return super.keys(arg0);
		}
		finally
		{
			_readLock.unlock();
		}
	}
	
	@Override
	public boolean contains(int val)
	{
		_readLock.lock();
		try
		{
			return super.contains(val);
		}
		finally
		{
			_readLock.unlock();
		}
	}
	
	@Override
	public boolean containsValue(Object arg0)
	{
		_readLock.lock();
		try
		{
			return super.containsValue(arg0);
		}
		finally
		{
			_readLock.unlock();
		}
	}
	
	@Override
	public boolean containsKey(int key)
	{
		_readLock.lock();
		try
		{
			return super.containsKey(key);
		}
		finally
		{
			_readLock.unlock();
		}
	}
	
	@Override
	public boolean forEachKey(TIntProcedure procedure)
	{
		_readLock.lock();
		try
		{
			return super.forEachKey(procedure);
		}
		finally
		{
			_readLock.unlock();
		}
	}
	
	/**
	 * A safe from deadlock loop.<br>
	 * put and remove synchronizers are disabled while this loop is running.<br>
	 * Keep in mind that this uses writeLock instead of readLock,<br>
	 * and its intended only if you are trying to put/remove something while looping<br>
	 * the values of this map.
	 * @param procedure
	 * @return
	 */
	public boolean safeForEachKey(TIntProcedure procedure)
	{
		_writeLock.lock();
		try
		{
			_tempLocksDisable = true;
			return super.forEachKey(procedure);
		}
		finally
		{
			_tempLocksDisable = false;
			_writeLock.unlock();
		}
	}
	
	@Override
	public boolean forEachValue(TObjectProcedure<? super V> arg0)
	{
		_readLock.lock();
		try
		{
			return super.forEachValue(arg0);
		}
		finally
		{
			_readLock.unlock();
		}
	}
	
	/**
	 * A safe from deadlock loop.<br>
	 * put and remove synchronizers are disabled while this loop is running.<br>
	 * Keep in mind that this uses writeLock instead of readLock,<br>
	 * and its intended only if you are trying to put/remove something while looping<br>
	 * the values of this map.
	 * @param arg0
	 * @return
	 */
	public boolean safeForEachValue(TObjectProcedure<V> arg0)
	{
		_writeLock.lock();
		try
		{
			_tempLocksDisable = true;
			return super.forEachValue(arg0);
		}
		finally
		{
			_tempLocksDisable = false;
			_writeLock.unlock();
		}
	}
	
	@Override
	public boolean forEachEntry(TIntObjectProcedure<? super V> arg0)
	{
		_readLock.lock();
		try
		{
			return super.forEachEntry(arg0);
		}
		finally
		{
			_readLock.unlock();
		}
	}
	
	/**
	 * A safe from deadlock loop.<br>
	 * put and remove synchronizers are disabled while this loop is running.<br>
	 * Keep in mind that this uses writeLock instead of readLock, <br>
	 * and its intended only if you are trying to put/remove something while looping<br>
	 * the values of this map.
	 * @param arg0
	 * @return
	 */
	public boolean safeForEachEntry(TIntObjectProcedure<V> arg0)
	{
		_writeLock.lock();
		try
		{
			_tempLocksDisable = true;
			return super.forEachEntry(arg0);
		}
		finally
		{
			_tempLocksDisable = false;
			_writeLock.unlock();
		}
	}
	
	@Override
	public boolean retainEntries(TIntObjectProcedure<? super V> arg0)
	{
		_writeLock.lock();
		try
		{
			return super.retainEntries(arg0);
		}
		finally
		{
			_writeLock.unlock();
		}
	}
	
	@Override
	public void transformValues(TObjectFunction<V, V> arg0)
	{
		_writeLock.lock();
		try
		{
			super.transformValues(arg0);
		}
		finally
		{
			_writeLock.unlock();
		}
	}
}
