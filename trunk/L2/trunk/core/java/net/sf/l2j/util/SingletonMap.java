package net.sf.l2j.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

public final class SingletonMap<K, V> implements Map<K, V>
{
	private FastMap<K, V> _map;
	private boolean _shared = false;
	
	@SuppressWarnings("unchecked")
	private Map<K, V> get(boolean init)
	{
		if (_map == null)
		{
			if (init)
				_map = ((FastMap<K, V>)FastMap.newInstance()).setShared(_shared);
			else
				return Collections.emptyMap();
		}
		
		return _map;
	}
	
	public SingletonMap<K, V> setShared()
	{
		_shared = true;
		
		if (_map != null)
			_map.setShared(true);
		
		return this;
	}
	
	public void clear()
	{
		get(false).clear();
	}
	
	public boolean containsKey(Object key)
	{
		return get(false).containsKey(key);
	}
	
	public boolean containsValue(Object value)
	{
		return get(false).containsValue(value);
	}
	
	public Set<Entry<K, V>> entrySet()
	{
		return get(false).entrySet();
	}
	
	public V get(Object key)
	{
		return get(false).get(key);
	}
	
	public boolean isEmpty()
	{
		return get(false).isEmpty();
	}
	
	public Set<K> keySet()
	{
		return get(false).keySet();
	}
	
	public V put(K key, V value)
	{
		return get(true).put(key, value);
	}
	
	public void putAll(Map<? extends K, ? extends V> m)
	{
		get(true).putAll(m);
	}
	
	public V remove(Object key)
	{
		return get(false).remove(key);
	}
	
	public int size()
	{
		return get(false).size();
	}
	
	public Collection<V> values()
	{
		return get(false).values();
	}
}
