package l2rt.gameserver.model;

import l2rt.util.GArray;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

public class L2ObjectArray<E extends L2Object> implements Iterable<E>
{
	private static final Logger _log = Logger.getLogger(L2ObjectsStorage.class.getName());

	public final String name;
	public final int resizeStep, initCapacity;
	private final GArray<Integer> freeIndexes;
	private E[] elementData;
	private int size = 0, real_size = 0;

	@SuppressWarnings("unchecked")
	public L2ObjectArray(String _name, int initialCapacity, int _resizeStep)
	{
		name = _name;
		resizeStep = _resizeStep;
		initCapacity = initialCapacity;

		if(initialCapacity < 0)
			throw new IllegalArgumentException("Illegal Capacity (" + name + "): " + initialCapacity);
		if(resizeStep < 1)
			throw new IllegalArgumentException("Illegal resize step (" + name + "): " + resizeStep);

		freeIndexes = new GArray<Integer>(resizeStep);
		elementData = (E[]) new L2Object[initialCapacity];
	}

	public int size()
	{
		return size;
	}

	public int getRealSize()
	{
		return real_size;
	}

	public int capacity()
	{
		return elementData.length;
	}

	public synchronized int add(E e)
	{
		Integer freeIndex = freeIndexes.removeLast();
		if(freeIndex != null)
		{
			real_size++;
			elementData[freeIndex] = e;
			return freeIndex;
		}

		if(elementData.length <= size)
		{
			int newCapacity = elementData.length + resizeStep;
			_log.warning("Object array [" + name + "] resized: " + elementData.length + " -> " + newCapacity);
			elementData = Arrays.copyOf(elementData, newCapacity);
		}
		elementData[size++] = e;
		real_size++;
		return size - 1;
	}

	public synchronized E remove(int index, int expectedObjId)
	{
		if(index >= size)
			return null;

		E old = elementData[index];
		if(old == null || old.getObjectId() != expectedObjId)
			return null;

		elementData[index] = null;
		real_size--;
		if(index == size - 1)
			size--;
		else
			freeIndexes.add(index);
		return old;
	}

	public E get(int index)
	{
		return index >= size ? null : elementData[index];
	}

	public E findByObjectId(int objId)
	{
		if(objId <= 0)
			return null;
		E o;
		for(int i = 0; i < size; i++)
		{
			o = elementData[i];
			if(o != null && o.getObjectId() == objId)
				return o;
		}
		return null;
	}

	public E findByName(String s)
	{
		E o;
		for(int i = 0; i < size; i++)
		{
			o = elementData[i];
			if(o != null && s.equalsIgnoreCase(o.getName()))
				return o;
		}
		return null;
	}

	public GArray<E> findAllByName(String s)
	{
		GArray<E> result = new GArray<E>();
		E o;
		for(int i = 0; i < size; i++)
		{
			o = elementData[i];
			if(o != null && s.equalsIgnoreCase(o.getName()))
				result.add(o);
		}
		return result;
	}

	public GArray<E> getAll()
	{
		return getAll(new GArray<E>(size));
	}

	public GArray<E> getAll(GArray<E> list)
	{
		E o;
		for(int i = 0; i < size; i++)
		{
			o = elementData[i];
			if(o != null)
				list.add(o);
		}
		return list;
	}

	private int indexOf(E o)
	{
		if(o == null)
			return -1;

		for(int i = 0; i < size; i++)
			if(o.equals(elementData[i]))
				return i;

		return -1;
	}

	public boolean contains(E o)
	{
		return indexOf(o) > -1;
	}

	@SuppressWarnings("unchecked")
	public synchronized void clear()
	{
		elementData = (E[]) new L2Object[0];
		size = 0;
		real_size = 0;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new Itr();
	}

	/**
	 * хитрый итератор :)
	 * @author Drin
	 */
	class Itr implements Iterator<E>
	{
		private int cursor = 0;
		private E _next;

		/**
		 * находим и запоминаем 
		 */
		@Override
		public boolean hasNext()
		{
			while(cursor < size)
				if((_next = elementData[cursor++]) != null)
					return true;
			return false;
		}

		/**
		 * возвращаем то что до этого нашли в hasNext()
		 */
		@Override
		public E next()
		{
			E result = _next;
			_next = null;
			if(result == null)
				throw new NoSuchElementException();
			return result;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}