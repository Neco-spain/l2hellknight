package net.sf.l2j.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.RandomAccess;

public class TList extends AbstractList
  implements List, RandomAccess, Cloneable, Serializable
{
  public static final int NORMAL = 0;
  public static final int RANDOM = 1;
  private transient Object root;
  private transient Leaf head;
  private transient Leaf tail;
  private transient int uprb = 0;

  private int mode = 0;

  private static final Random rndx = new Random();
  private static final int LIMIT = 256;

  public TList()
  {
    root = null;
    head = null;
    tail = null;
  }

  public TList(int initialCapacity)
  {
    root = null;
    head = null;
    tail = null;
  }

  public TList(Collection c)
  {
    root = null;
    head = null;
    tail = null;
    addAll(c);
  }

  public TList(Object[] a)
  {
    root = null;
    head = null;
    tail = null;
    addAll(Arrays.asList(a));
  }

  public final void ensureCapacity(int minCapacity)
  {
  }

  public final void trimToSize()
  {
  }

  public static void main(String[] args)
  {
    TList t = new TList(new String[] { "Hello", "World!" });
    System.out.println(t.toString());
  }

  public boolean isEmpty()
  {
    return size() == 0;
  }

  public final int size()
  {
    return (root instanceof Leaf) ? 1 : root == null ? 0 : ((Node)root).getWeight();
  }

  public final void clear()
  {
    if (root == null)
      return;
    modCount += 1;
    tail = null;
    head = null;
    root = null;
    uprb = 0;
  }

  public final Object clone()
  {
    TList clone = null;
    int sz = size();
    try {
      clone = (TList)super.clone();
      clone.modCount = 0;
      clone.uprb = 0;
      clone.mode = mode;
      clone.root = null;
      clone.tail = null;
      clone.head = null;
      if (sz == 0) {
        return clone;
      }
      if (sz == 1) {
        clone.root = new Leaf(((Leaf)root).getValue());
        clone.head = ((Leaf)(Leaf)clone.root);
        clone.tail = ((Leaf)(Leaf)clone.root);
        return clone;
      }

      1Q front = new Object()
      {
        TList.Node val = null;

        1Q next = null;
      };
      front.val = new Node(sz);
      1Q back = front;
      1Q curr = front;
      1Q q = null;
      clone.root = front.val;

      for (int i = 0; i < sz - 2; i++) {
        q = new Object()
        {
          TList.Node val = null;

          1Q next = null;
        };
        q.val = new Node(2);
        back.next = q;
        back = q;
      }

      curr = front;
      1Q posn = curr;
      int wt = sz;

      while (wt > 3) {
        int wl = wt / 2;
        int wr = wt - wl;

        posn = posn.next;
        posn.val.setWeight(wl);

        posn = posn.next;
        posn.val.setWeight(wr);

        curr = curr.next;
        wt = curr.val.getWeight();
      }

      curr = front;
      posn = front;
      Node g = null;

      while (curr != null)
      {
        g = curr.val;
        wt = g.getWeight();
        int wl = wt / 2;
        int wr = wt - wl;

        if (wl > 1) {
          posn = posn.next;
          g.setLeft(posn.val);
        }

        if (wr > 1) {
          posn = posn.next;
          g.setRight(posn.val);
        }

        curr = curr.next;
      }

      Object node = null; Object xnode = null;
      Leaf nxlf = new Leaf();
      Leaf pv = null; Leaf v = null;
      nxlf.setRight(head);
      int index = 0;
      do
      {
        node = (Node)clone.root;
        int wn = sz;
        int first = 0;
        int last = sz;

        while (wn > 4)
        {
          int pivot = first;
          xnode = ((Node)node).getLeft();
          wn = ((Node)xnode).getWeight();
          pivot += wn;

          if (index < pivot) {
            last = pivot;
            node = xnode; continue;
          }
          first = pivot;
          node = (Node)(Node)((Node)node).getRight();
          wn = ((Node)node).getWeight();
        }

        if (wn == 4) {
          xnode = (Node)(Node)((Node)node).getLeft();
          nxlf = nxlf.getRight();
          pv = v;
          v = new Leaf(nxlf.getValue());
          if (pv == null) {
            clone.head = v;
          } else {
            v.setLeft(pv);
            pv.setRight(v);
          }
          ((Node)xnode).setLeft(v);
          index++;
          nxlf = nxlf.getRight();
          pv = v;
          v = new Leaf(nxlf.getValue());
          if (pv == null) {
            clone.head = v;
          } else {
            v.setLeft(pv);
            pv.setRight(v);
          }
          ((Node)xnode).setRight(v);
          index++;
          node = (Node)(Node)((Node)node).getRight();
        }
        if (wn == 3) {
          nxlf = nxlf.getRight();
          pv = v;
          v = new Leaf(nxlf.getValue());
          if (pv == null) {
            clone.head = v;
          } else {
            v.setLeft(pv);
            pv.setRight(v);
          }
          ((Node)node).setLeft(v);
          index++;
          node = (Node)(Node)((Node)node).getRight();
        }
        nxlf = nxlf.getRight();
        pv = v;
        v = new Leaf(nxlf.getValue());
        if (pv == null) {
          clone.head = v;
        } else {
          v.setLeft(pv);
          pv.setRight(v);
        }
        ((Node)node).setLeft(v);
        index++;
        nxlf = nxlf.getRight();
        pv = v;
        v = new Leaf(nxlf.getValue());
        if (pv == null) {
          clone.head = v;
        } else {
          v.setLeft(pv);
          pv.setRight(v);
        }
        ((Node)node).setRight(v);
        index++;
      }
      while (index < sz);

      clone.tail = v;

      return clone; } catch (CloneNotSupportedException e) {
    }
    throw new InternalError();
  }

  public final void splice(int whereIndex, TList other)
    throws IndexOutOfBoundsException, NullPointerException, IllegalArgumentException
  {
    if ((whereIndex < 0) || (whereIndex > size()))
      throw new IndexOutOfBoundsException();
    if (other == null)
      throw new NullPointerException();
    if ((other == this) || (!(other instanceof TList))) {
      throw new IllegalArgumentException();
    }
    rebuildTest();
    other.rebuildTest();

    if (other.size() == 0) {
      return;
    }

    if (size() == 0) {
      root = other.root;
      head = other.head;
      tail = other.tail;
      other.clear();
      modCount += 1;
      return;
    }

    if (whereIndex == 0) {
      other.splice(other.size(), this);
      root = other.root;
      head = other.head;
      tail = other.tail;
      other.clear();
      rebuildTest();
      modCount += 1;
      return;
    }

    if (whereIndex == size()) {
      int sz = size() + other.size();
      Node z = new Node(sz);
      z.setLeft(root);
      z.setRight(other.root);
      root = z;
      z = null;
      tail.setRight(other.head);
      other.head.setLeft(tail);
      tail = other.tail;
      other.clear();
      rebuildTest();
      modCount += 1;
      return;
    }

    TList tlist2 = split(whereIndex);
    splice(size(), other);
    splice(size(), tlist2);
  }

  public final TList split(int whereIndex)
    throws IndexOutOfBoundsException
  {
    if ((whereIndex < 0) || (whereIndex > size())) {
      throw new IndexOutOfBoundsException();
    }
    TList retTList = new TList();
    retTList.mode = mode;

    if (whereIndex == size()) {
      rebuildTest();
      return retTList;
    }

    if (whereIndex == 0) {
      retTList.root = root;
      retTList.head = head;
      retTList.tail = tail;
      retTList.uprb = uprb;
      clear();
      retTList.rebuildTest();
      return retTList;
    }

    retTList = getTList(size() - whereIndex, getLeaf(whereIndex));
    removeRange(whereIndex, size());

    return retTList;
  }

  public final List subList(int fromIndex, int toIndex)
  {
    return new SubTList(this, fromIndex, toIndex);
  }

  public Object[] toArray()
  {
    int s = size();
    Object[] o = new Object[s];
    Iterator iter = iterator();
    int i = 0;
    while (iter.hasNext()) {
      o[(i++)] = iter.next();
    }
    return o;
  }

  public Object[] toArray(Object[] o)
    throws NullPointerException
  {
    if (o == null)
      throw new NullPointerException();
    int s = size();
    if (o.length < s) {
      o = (Object[])(Object[])Array.newInstance(o.getClass().getComponentType(), s);
    }
    Iterator iter = iterator();
    int i = 0;
    while (iter.hasNext()) {
      o[(i++)] = iter.next();
    }
    if (o.length > s)
      o[s] = null;
    return o;
  }

  public boolean contains(Object o)
  {
    Iterator iter = iterator();
    while (iter.hasNext()) {
      Object o2 = iter.next();
      if (o == null) {
        if (o2 == null)
          return true;
      }
      else if ((o2 != null) && 
        (o2.equals(o))) {
        return true;
      }
    }
    return false;
  }

  public int indexOf(Object o)
  {
    ListIterator lter = listIterator();
    if (o == null) {
      do if (!lter.hasNext())
          break; while (lter.next() != null);
      return lter.previousIndex();
    }
    while (lter.hasNext()) {
      if (o.equals(lter.next()))
        return lter.previousIndex();
    }
    return -1;
  }

  public int lastIndexOf(Object o)
  {
    ListIterator lter = listIterator(size());
    if (o == null) {
      do if (!lter.hasPrevious())
          break; while (lter.previous() != null);
      return lter.nextIndex();
    }
    while (lter.hasPrevious()) {
      if (o.equals(lter.previous()))
        return lter.nextIndex();
    }
    return -1;
  }

  public boolean containsAll(Collection c)
    throws NullPointerException
  {
    if (c == null)
      throw new NullPointerException();
    Iterator iter = c.iterator();
    while (iter.hasNext()) {
      if (!contains(iter.next()))
        return false;
    }
    return true;
  }

  public boolean add(Object o)
  {
    addLast(o);
    return true;
  }

  public boolean addAll(Collection c)
    throws NullPointerException
  {
    if (c == null)
      throw new NullPointerException();
    if (c.isEmpty()) {
      return false;
    }

    Iterator iter = c.iterator();
    while (iter.hasNext()) {
      addLast(iter.next());
    }
    return true;
  }

  public boolean addAll(int index, Collection c)
    throws NullPointerException, IndexOutOfBoundsException
  {
    if ((index < 0) || (index > size()))
      throw new IndexOutOfBoundsException();
    if (c == null)
      throw new NullPointerException();
    if (c.isEmpty()) {
      return false;
    }

    Iterator iter = c.iterator();
    int i = index;

    while (iter.hasNext())
    {
      add(i++, iter.next());
    }
    return true;
  }

  public final void addLast(Object o)
  {
    if (root == null) {
      root = new Leaf(o);
      head = ((Leaf)root);
      tail = head;
      modCount += 1;
      return;
    }

    if ((root instanceof Leaf)) {
      Object r = new Node(2);
      ((Node)r).setLeft(root);
      Leaf rl = new Leaf(o);
      ((Node)r).setRight(rl);
      head = ((Leaf)root);
      tail = rl;
      head.setRight(tail);
      tail.setLeft(head);
      root = r;
      modCount += 1;
      return;
    }

    if (mode == 0) {
      int index = size();
      int ix0 = index - 256; int ix1 = index;
      if (ix0 < 0)
        ix0 = 0;
      int ixx = ix0 + rndx.nextInt(1 + (ix1 - ix0));
      if (ixx != index)
      {
        Leaf lf;
        Leaf lf;
        if (ixx == 0)
          lf = xaddFirst(null);
        else
          lf = xadd(ixx, null);
        Leaf lg;
        for (int k = ixx; k < index; lf = lg) {
          lg = lf.getRight();
          lf.setValue(lg.getValue());

          k++;
        }

        lf.setValue(o);
        return;
      }
    }

    modCount += 1;

    uprb += 1;

    Object p = root;

    Object node = root;
    while ((node instanceof Node)) {
      ((Node)node).setWeight(((Node)node).getWeight() + 1);
      p = node;
      node = ((Node)node).getRight();
    }

    Object q = new Node(2);
    ((Node)q).setLeft(node);
    Leaf rl = new Leaf(o);
    ((Node)q).setRight(rl);
    tail = rl;
    tail.setLeft((Leaf)node);
    ((Leaf)node).setRight(tail);
    ((Node)p).setRight(q);
    rebuildTest();
  }

  private final Leaf xaddLast(Object o)
  {
    modCount += 1;

    uprb += 1;

    Object p = root;

    Object node = root;
    while ((node instanceof Node)) {
      ((Node)node).setWeight(((Node)node).getWeight() + 1);
      p = node;
      node = ((Node)node).getRight();
    }

    Object q = new Node(2);
    ((Node)q).setLeft(node);
    Leaf rl = new Leaf(o);
    ((Node)q).setRight(rl);
    tail = rl;
    tail.setLeft((Leaf)node);
    ((Leaf)node).setRight(tail);
    ((Node)p).setRight(q);
    rebuildTest();
    return rl;
  }

  public final void addFirst(Object o)
  {
    if (root == null) {
      root = new Leaf(o);
      head = ((Leaf)root);
      tail = head;
      modCount += 1;
      return;
    }

    if ((root instanceof Leaf)) {
      Object r = new Node(2);
      ((Node)r).setRight(root);
      Leaf rl = new Leaf(o);
      ((Node)r).setLeft(rl);
      head = rl;
      tail = ((Leaf)root);
      head.setRight(tail);
      tail.setLeft(head);
      root = r;
      modCount += 1;
      return;
    }

    if (mode == 0) {
      int index = 0;
      int ix0 = 0; int ix1 = 256;
      if (ix1 > size())
        ix1 = size();
      int ixx = ix0 + rndx.nextInt(1 + (ix1 - ix0));
      if (ixx != index)
      {
        Leaf lf;
        Leaf lf;
        if (ixx == size())
          lf = xaddLast(null);
        else
          lf = xadd(ixx, null);
        Leaf lg;
        for (int k = ixx; k > index; lf = lg) {
          lg = lf.getLeft();
          lf.setValue(lg.getValue());

          k--;
        }

        lf.setValue(o);
        return;
      }
    }

    modCount += 1;

    uprb += 1;

    Object p = root;

    Object node = root;
    while ((node instanceof Node)) {
      ((Node)node).setWeight(((Node)node).getWeight() + 1);
      p = node;
      node = ((Node)node).getLeft();
    }

    Object q = new Node(2);
    ((Node)q).setRight(node);
    Leaf rl = new Leaf(o);
    ((Node)q).setLeft(rl);
    head = rl;
    head.setRight((Leaf)node);
    ((Leaf)node).setLeft(head);
    ((Node)p).setLeft(q);
    rebuildTest();
  }

  private final Leaf xaddFirst(Object o)
  {
    modCount += 1;

    uprb += 1;

    Object p = root;

    Object node = root;
    while ((node instanceof Node)) {
      ((Node)node).setWeight(((Node)node).getWeight() + 1);
      p = node;
      node = ((Node)node).getLeft();
    }

    Object q = new Node(2);
    ((Node)q).setRight(node);
    Leaf rl = new Leaf(o);
    ((Node)q).setLeft(rl);
    head = rl;
    head.setRight((Leaf)node);
    ((Leaf)node).setLeft(head);
    ((Node)p).setLeft(q);
    rebuildTest();
    return rl;
  }

  public final void add(int index, Object o)
    throws IndexOutOfBoundsException
  {
    if ((index < 0) || (index > size())) {
      throw new IndexOutOfBoundsException();
    }
    if (root == null) {
      root = new Leaf(o);
      head = ((Leaf)root);
      tail = head;
      modCount += 1;
      return;
    }

    if (index == size()) {
      addLast(o);
      return;
    }

    if (index == 0) {
      addFirst(o);
      return;
    }

    if (mode == 0) {
      int ix0 = index - 256; int ix1 = index + 256;
      if (ix0 < 0)
        ix0 = 0;
      if (ix1 > size())
        ix1 = size();
      int ixx = ix0 + rndx.nextInt(1 + (ix1 - ix0));
      if (ixx != index)
      {
        Leaf lf;
        Leaf lf;
        if (ixx == size()) {
          lf = xaddLast(null);
        }
        else
        {
          Leaf lf;
          if (ixx == 0)
            lf = xaddFirst(null);
          else
            lf = xadd(ixx, null); 
        }
        if (ixx < index)
        {
          Leaf lg;
          for (int k = ixx; k < index; lf = lg) {
            lg = lf.getRight();
            lf.setValue(lg.getValue());

            k++;
          }
        }
        else
        {
          Leaf lg;
          for (int k = ixx; k > index; lf = lg) {
            lg = lf.getLeft();
            lf.setValue(lg.getValue());

            k--;
          }
        }

        lf.setValue(o);
        return;
      }
    }

    Object p = root;

    Object node = root;

    int first = 0; int last = 0;

    modCount += 1;

    uprb += 1;

    last += size();

    while ((node instanceof Node))
    {
      ((Node)node).setWeight(((Node)node).getWeight() + 1);
      p = node;

      int pivot = first;

      if ((((Node)node).getLeft() instanceof Node))
        pivot += ((Node)(Node)((Node)node).getLeft()).getWeight();
      else {
        pivot++;
      }
      if (index < pivot) {
        last = pivot;
        node = ((Node)node).getLeft();
      } else {
        first = pivot;
        node = ((Node)node).getRight();
      }

    }

    Object q = new Node(2);
    ((Node)q).setRight(node);
    Leaf rl = ((Leaf)node).getLeft();
    Leaf r = new Leaf(o);
    ((Node)q).setLeft(r);
    ((Leaf)node).setLeft(r);

    r.setRight((Leaf)node);

    if (node != head) {
      rl.setRight(r);
      r.setLeft(rl);
    } else {
      head = r;
    }
    if (node == ((Node)p).getLeft()) {
      ((Node)p).setLeft(q);
    }
    else {
      ((Node)p).setRight(q);
    }
    rebuildTest();
  }

  private final Leaf xadd(int index, Object o)
  {
    Object p = root;

    Object node = root;

    int first = 0; int last = 0;

    modCount += 1;

    uprb += 1;

    last += size();

    while ((node instanceof Node))
    {
      ((Node)node).setWeight(((Node)node).getWeight() + 1);
      p = node;

      int pivot = first;

      if ((((Node)node).getLeft() instanceof Node))
        pivot += ((Node)(Node)((Node)node).getLeft()).getWeight();
      else {
        pivot++;
      }
      if (index < pivot) {
        last = pivot;
        node = ((Node)node).getLeft();
      } else {
        first = pivot;
        node = ((Node)node).getRight();
      }

    }

    Object q = new Node(2);
    ((Node)q).setRight(node);
    Leaf rl = ((Leaf)node).getLeft();
    Leaf r = new Leaf(o);
    ((Node)q).setLeft(r);
    ((Leaf)node).setLeft(r);

    r.setRight((Leaf)node);

    if (node != head) {
      rl.setRight(r);
      r.setLeft(rl);
    } else {
      head = r;
    }
    if (node == ((Node)p).getLeft()) {
      ((Node)p).setLeft(q);
    }
    else {
      ((Node)p).setRight(q);
    }
    rebuildTest();

    return r;
  }

  public final Object set(int index, Object o)
    throws IndexOutOfBoundsException
  {
    if ((index < 0) || (index >= size())) {
      throw new IndexOutOfBoundsException();
    }
    Leaf lf = getLeaf(index);
    Object v = lf.getValue();
    lf.setValue(o);
    return v;
  }

  public final void setMode(int mode)
    throws IllegalArgumentException
  {
    if ((mode != 0) && (mode != 1))
      throw new IllegalArgumentException();
    if (mode != this.mode) {
      rebuild();
      this.mode = mode;
    }
  }

  public int getMode()
  {
    return mode;
  }

  public final Object get(int index)
    throws IndexOutOfBoundsException
  {
    Object node = root;
    int first = 0; int last = 0;

    if ((index < 0) || (index >= size())) {
      throw new IndexOutOfBoundsException();
    }
    if ((root instanceof Leaf)) {
      return ((Leaf)root).getValue();
    }
    last += size();

    while ((node instanceof Node))
    {
      int pivot = first;

      if ((((Node)node).getLeft() instanceof Node))
        pivot += ((Node)(Node)((Node)node).getLeft()).getWeight();
      else {
        pivot++;
      }
      if (index < pivot) {
        last = pivot;
        node = ((Node)node).getLeft();
      } else {
        first = pivot;
        node = ((Node)node).getRight();
      }
    }
    return ((Leaf)node).getValue();
  }

  public final Object getFirst()
    throws NoSuchElementException
  {
    if (root == null) {
      throw new NoSuchElementException();
    }
    Object node = root;

    if ((root instanceof Leaf)) {
      return ((Leaf)root).getValue();
    }
    while ((node instanceof Node)) {
      node = ((Node)node).getLeft();
    }

    return ((Leaf)node).getValue();
  }

  public final Object getLast()
    throws NoSuchElementException
  {
    if (root == null) {
      throw new NoSuchElementException();
    }
    Object node = root;

    if ((root instanceof Leaf)) {
      return ((Leaf)root).getValue();
    }
    while ((node instanceof Node)) {
      node = ((Node)node).getRight();
    }

    return ((Leaf)node).getValue();
  }

  private final Leaf getLeaf(int index)
    throws IndexOutOfBoundsException
  {
    Object node = root;
    int first = 0; int last = 0;

    if ((index < 0) || (index >= size())) {
      throw new IndexOutOfBoundsException();
    }
    if ((root instanceof Leaf)) {
      return (Leaf)root;
    }
    last += size();

    while ((node instanceof Node))
    {
      int pivot = first;

      if ((((Node)node).getLeft() instanceof Node))
        pivot += ((Node)(Node)((Node)node).getLeft()).getWeight();
      else {
        pivot++;
      }
      if (index < pivot) {
        last = pivot;
        node = ((Node)node).getLeft();
      } else {
        first = pivot;
        node = ((Node)node).getRight();
      }
    }
    return (Leaf)node;
  }

  private final TList getTList(int listSize, Leaf firstLeaf)
    throws IllegalArgumentException
  {
    if ((firstLeaf != null) && 
      (!(firstLeaf instanceof Leaf)))
      throw new IllegalArgumentException();
    if (listSize < 0) {
      throw new IllegalArgumentException();
    }
    TList tlist = new TList();
    if ((listSize == 0) || (firstLeaf == null))
      return tlist;
    if (listSize == 1) {
      tlist.root = firstLeaf;
      tlist.head = firstLeaf;
      tlist.tail = firstLeaf;
      tlist.head.setLeft(null);
      tlist.tail.setRight(null);
      return tlist;
    }
    int sz = listSize;
    2Q front = new Object()
    {
      TList.Node val = null;

      2Q next = null;
    };
    front.val = new Node(sz);
    2Q back = front;
    2Q curr = front;
    2Q q = null;
    tlist.root = front.val;

    for (int i = 0; i < sz - 2; i++) {
      q = new Object()
      {
        TList.Node val = null;

        2Q next = null;
      };
      q.val = new Node(2);
      back.next = q;
      back = q;
    }

    curr = front;
    2Q posn = curr;
    int wt = sz;

    while (wt > 3) {
      int wl = wt / 2;
      int wr = wt - wl;

      posn = posn.next;
      posn.val.setWeight(wl);

      posn = posn.next;
      posn.val.setWeight(wr);

      curr = curr.next;
      wt = curr.val.getWeight();
    }

    curr = front;
    posn = front;
    Node g = null;

    while (curr != null)
    {
      g = curr.val;
      wt = g.getWeight();
      int wl = wt / 2;
      int wr = wt - wl;

      if (wl > 1) {
        posn = posn.next;
        g.setLeft(posn.val);
      }

      if (wr > 1) {
        posn = posn.next;
        g.setRight(posn.val);
      }

      curr = curr.next;
    }

    Object node = null; Object xnode = null;
    Leaf nxlf = new Leaf();
    Leaf pv = null; Leaf v = null;
    nxlf.setRight(firstLeaf);
    int index = 0;
    do
    {
      node = (Node)tlist.root;
      int wn = sz;
      int first = 0;
      int last = sz;

      while (wn > 4)
      {
        int pivot = first;
        xnode = ((Node)node).getLeft();
        wn = ((Node)xnode).getWeight();
        pivot += wn;

        if (index < pivot) {
          last = pivot;
          node = xnode; continue;
        }
        first = pivot;
        node = (Node)(Node)((Node)node).getRight();
        wn = ((Node)node).getWeight();
      }

      if (wn == 4) {
        xnode = (Node)(Node)((Node)node).getLeft();
        nxlf = nxlf.getRight();
        pv = v;
        v = nxlf;
        if (pv == null) {
          tlist.head = v;
        } else {
          v.setLeft(pv);
          pv.setRight(v);
        }
        ((Node)xnode).setLeft(v);
        index++;
        nxlf = nxlf.getRight();
        pv = v;
        v = nxlf;
        if (pv == null) {
          tlist.head = v;
        } else {
          v.setLeft(pv);
          pv.setRight(v);
        }
        ((Node)xnode).setRight(v);
        index++;
        node = (Node)(Node)((Node)node).getRight();
      }
      if (wn == 3) {
        nxlf = nxlf.getRight();
        pv = v;
        v = nxlf;
        if (pv == null) {
          tlist.head = v;
        } else {
          v.setLeft(pv);
          pv.setRight(v);
        }
        ((Node)node).setLeft(v);
        index++;
        node = (Node)(Node)((Node)node).getRight();
      }
      nxlf = nxlf.getRight();
      pv = v;
      v = nxlf;
      if (pv == null) {
        tlist.head = v;
      } else {
        v.setLeft(pv);
        pv.setRight(v);
      }
      ((Node)node).setLeft(v);
      index++;
      nxlf = nxlf.getRight();
      pv = v;
      v = nxlf;
      if (pv == null) {
        tlist.head = v;
      } else {
        v.setLeft(pv);
        pv.setRight(v);
      }
      ((Node)node).setRight(v);
      index++;
    }
    while (index < sz);

    tlist.tail = v;
    tlist.head.setLeft(null);
    tlist.tail.setRight(null);

    return tlist;
  }

  public boolean remove(Object o)
  {
    Iterator iter = iterator();
    boolean changed = false;
    Object o2 = null;
    while (iter.hasNext()) {
      o2 = iter.next();
      if (o == null) {
        if (o2 == null) {
          iter.remove();

          return true;
        }
      }
      if ((o2 == null) || 
        (!o2.equals(o))) continue;
      iter.remove();

      return true;
    }

    return false;
  }

  public boolean removeAll(Collection c)
    throws NullPointerException
  {
    if (c == null)
      throw new NullPointerException();
    boolean changed = false;
    Iterator iter = iterator();
    while (iter.hasNext()) {
      if (c.contains(iter.next())) {
        iter.remove();
        changed = true;
      }
    }
    return changed;
  }

  public boolean retainAll(Collection c)
    throws NullPointerException
  {
    if (c == null)
      throw new NullPointerException();
    boolean changed = false;
    Iterator iter = iterator();
    while (iter.hasNext()) {
      if (!c.contains(iter.next())) {
        iter.remove();
        changed = true;
      }
    }
    return changed;
  }

  public final Object remove(int index)
    throws IndexOutOfBoundsException
  {
    if ((index < 0) || (index >= size())) {
      throw new IndexOutOfBoundsException();
    }
    if ((root instanceof Leaf)) {
      Object v = ((Leaf)root).getValue();
      clear();
      return v;
    }

    Object g = root;
    Object p = root;

    Object node = root;
    int first = 0; int last = 0;

    modCount += 1;

    uprb += 1;

    last += size();

    while ((node instanceof Node))
    {
      ((Node)node).setWeight(((Node)node).getWeight() - 1);

      g = p;
      p = node;

      int pivot = first;

      if ((((Node)node).getLeft() instanceof Node))
        pivot += ((Node)(Node)((Node)node).getLeft()).getWeight();
      else {
        pivot++;
      }
      if (index < pivot) {
        last = pivot;
        node = ((Node)node).getLeft();
      } else {
        first = pivot;
        node = ((Node)node).getRight();
      }

    }

    Object v = ((Leaf)node).getValue();

    if (g == p)
    {
      if (node == ((Node)p).getLeft()) {
        head = ((Leaf)node).getRight();

        root = ((Node)p).getRight();
      } else {
        tail = ((Leaf)node).getLeft();

        root = ((Node)p).getLeft();
      }
      rebuildTest();
      return v;
    }

    Leaf ll = ((Leaf)node).getLeft();
    Leaf rl = ((Leaf)node).getRight();

    if (node == head)
    {
      head = rl;
    } else if (node == tail)
    {
      tail = ll;
    } else {
      rl.setLeft(ll);
      ll.setRight(rl);
    }

    if (node == ((Node)p).getLeft())
    {
      if (p == ((Node)g).getLeft()) {
        ((Node)g).setLeft(((Node)p).getRight());
      }
      else {
        ((Node)g).setRight(((Node)p).getRight());
      }

    }
    else if (p == ((Node)g).getLeft()) {
      ((Node)g).setLeft(((Node)p).getLeft());
    }
    else
      ((Node)g).setRight(((Node)p).getLeft());
    rebuildTest();
    return v;
  }

  public final Object removeFirst()
    throws NoSuchElementException
  {
    if (root == null) {
      throw new NoSuchElementException();
    }
    if ((root instanceof Leaf)) {
      Object v = ((Leaf)root).getValue();
      clear();
      return v;
    }

    modCount += 1;

    uprb += 1;

    Object g = root;
    Object p = root;

    Object node = root;
    while ((node instanceof Node)) {
      ((Node)node).setWeight(((Node)node).getWeight() - 1);
      g = p;
      p = node;
      node = ((Node)node).getLeft();
    }

    Object v = ((Leaf)node).getValue();

    if (g == p)
    {
      head = ((Leaf)node).getRight();

      root = ((Node)p).getRight();
      rebuildTest();
      return v;
    }

    Leaf rl = ((Leaf)node).getRight();

    head = rl;

    if (p == ((Node)g).getLeft()) {
      ((Node)g).setLeft(((Node)p).getRight());
    }
    else {
      ((Node)g).setRight(((Node)p).getRight());
    }
    rebuildTest();
    return v;
  }

  public final Object removeLast()
    throws NoSuchElementException
  {
    if (root == null) {
      throw new NoSuchElementException();
    }
    if ((root instanceof Leaf)) {
      Object v = ((Leaf)root).getValue();
      clear();
      return v;
    }

    modCount += 1;

    uprb += 1;

    Object g = root;
    Object p = root;

    Object node = root;
    while ((node instanceof Node)) {
      ((Node)node).setWeight(((Node)node).getWeight() - 1);
      g = p;
      p = node;
      node = ((Node)node).getRight();
    }

    Object v = ((Leaf)node).getValue();

    if (g == p)
    {
      tail = ((Leaf)node).getRight();

      root = ((Node)p).getLeft();
      rebuildTest();
      return v;
    }

    Leaf ll = ((Leaf)node).getLeft();

    tail = ll;

    if (p == ((Node)g).getLeft()) {
      ((Node)g).setLeft(((Node)p).getLeft());
    }
    else {
      ((Node)g).setRight(((Node)p).getLeft());
    }
    rebuildTest();
    return v;
  }

  public final void removeRange(int fromIndex, int toIndex)
    throws IndexOutOfBoundsException, IllegalArgumentException
  {
    if ((fromIndex < 0) || (toIndex > size())) {
      throw new IndexOutOfBoundsException();
    }
    if (fromIndex > toIndex) {
      throw new IllegalArgumentException();
    }
    if (fromIndex == toIndex) {
      return;
    }
    if ((fromIndex == 0) && (toIndex == size())) {
      clear();
      return;
    }

    TList tl1 = null; TList tl2 = null;
    int sz = size(); int wt = sz - (toIndex - fromIndex);
    int i1 = fromIndex; int i2 = sz - toIndex;
    modCount += 1;

    if (toIndex == sz) {
      tl1 = getTList(i1, head);
      root = tl1.root;
      head = tl1.head;
      head.setLeft(null);
      tail = tl1.tail;
      tail.setRight(null);
      if ((root instanceof Node))
        ((Node)root).setWeight(wt);
      rebuild();
      return;
    }
    if (fromIndex == 0) {
      tl2 = getTList(i2, getLeaf(toIndex));
      root = tl2.root;
      head = tl2.head;
      head.setLeft(null);
      tail = tl2.tail;
      tail.setRight(null);
      if ((root instanceof Node))
        ((Node)root).setWeight(wt);
      rebuild();
      return;
    }

    tl1 = getTList(i1, head);
    tl2 = getTList(i2, getLeaf(toIndex));
    ((Node)root).setLeft(tl1.root);
    ((Node)root).setRight(tl2.root);
    ((Node)root).setWeight(wt);
    head = tl1.head;
    head.setLeft(null);
    tail = tl2.tail;
    tail.setRight(null);
    tl1.tail.setRight(tl2.head);
    tl2.head.setLeft(tl1.tail);
    rebuild();
  }

  public final ListIterator listIterator()
  {
    return listIterator(0);
  }

  public final ListIterator listIterator(int position)
    throws IndexOutOfBoundsException
  {
    if ((position < 0) || (position > size())) {
      throw new IndexOutOfBoundsException();
    }
    return new Object(position)
    {
      TList.Leaf curr;
      TList.Leaf prev;
      int index;
      boolean ncall;
      boolean pcall;
      boolean acall;
      boolean rcall;
      int expectedModCount;

      public final boolean hasPrevious()
      {
        return prev != null;
      }

      public final Object next()
        throws NoSuchElementException
      {
        if (expectedModCount != modCount)
          throw new ConcurrentModificationException();
        try
        {
          Object v = curr.getValue();
          index += 1;
          prev = curr;
          if (index == size())
            curr = null;
          else {
            curr = prev.getRight();
          }
          ncall = true;
          pcall = false;
          acall = false;
          rcall = false;
          return v;
        } catch (IndexOutOfBoundsException e) {
          if (expectedModCount != modCount)
            throw new ConcurrentModificationException(); 
        }
        throw new NoSuchElementException();
      }

      public final int nextIndex()
      {
        return index;
      }

      public final Object previous()
        throws NoSuchElementException
      {
        if (expectedModCount != modCount)
          throw new ConcurrentModificationException();
        try
        {
          index -= 1;
          Object v = prev.getValue();
          curr = prev;
          if (index == 0)
            prev = null;
          else {
            prev = curr.getLeft();
          }
          pcall = true;
          ncall = false;
          acall = false;
          rcall = false;
          return v;
        } catch (IndexOutOfBoundsException e) {
          if (expectedModCount != modCount)
            throw new ConcurrentModificationException(); 
        }
        throw new NoSuchElementException();
      }

      public final int previousIndex()
      {
        return index - 1;
      }

      public final void remove() throws IllegalStateException
      {
        if ((!ncall) && (!pcall))
          throw new IllegalStateException();
        if (expectedModCount != modCount)
          throw new ConcurrentModificationException();
        try {
          if (ncall) {
            remove(--index);
            expectedModCount = modCount;
            if (index == 0)
              prev = null;
            else
              prev = prev.getLeft();
          } else {
            remove(index);
            expectedModCount = modCount;
            if (index == size())
              curr = null;
            else
              curr = curr.getRight();
          }
          rcall = true;
          ncall = false;
          pcall = false;
          acall = false;
        } catch (IndexOutOfBoundsException e) {
          throw new ConcurrentModificationException();
        }
      }

      public final boolean hasNext() {
        return curr != null;
      }

      public final void add(Object o) {
        if (expectedModCount != modCount)
          throw new ConcurrentModificationException();
        try {
          add(index, o);
          expectedModCount = modCount;
          index += 1;

          if (curr == null) {
            prev = tail;
          } else {
            curr = TList.this.getLeaf(index);
            prev = curr.getLeft();
          }

          acall = true;
          ncall = false;
          pcall = false;
          rcall = false;
        } catch (IndexOutOfBoundsException e) {
          throw new ConcurrentModificationException();
        }
      }

      public final void set(Object o) throws IllegalStateException
      {
        if ((!ncall) && (!pcall))
          throw new IllegalStateException();
        if (expectedModCount != modCount)
          throw new ConcurrentModificationException();
        try
        {
          if (ncall)
          {
            prev.setValue(o);
          }
          else
          {
            curr.setValue(o);
          }
        } catch (IndexOutOfBoundsException e) {
          throw new ConcurrentModificationException();
        }
      }
    };
  }

  public final Iterator iterator()
  {
    return new Object()
    {
      TList.Leaf curr;
      int index;
      boolean ncall;
      int expectedModCount;

      public final void remove()
        throws IllegalStateException
      {
        if (!ncall)
          throw new IllegalStateException();
        if (expectedModCount != modCount)
          throw new ConcurrentModificationException();
        try
        {
          ncall = false;
          remove(--index);
          expectedModCount = modCount;
        }
        catch (IndexOutOfBoundsException e)
        {
          throw new ConcurrentModificationException();
        }
      }

      public final boolean hasNext() {
        return curr != null;
      }

      public final Object next()
        throws NoSuchElementException
      {
        if (expectedModCount != modCount) {
          throw new ConcurrentModificationException();
        }
        try
        {
          Object v = curr.getValue();
          index += 1;
          if (index == size())
            curr = null;
          else
            curr = curr.getRight();
          ncall = true;
          return v;
        } catch (IndexOutOfBoundsException e) {
          if (expectedModCount != modCount)
            throw new ConcurrentModificationException(); 
        }
        throw new NoSuchElementException();
      }
    };
  }

  private final void writeObject(ObjectOutputStream s)
    throws IOException
  {
    s.defaultWriteObject();

    int expectedModCount = modCount;
    int size = size();
    Leaf nxlf = new Leaf();
    nxlf.setRight(head);

    s.writeInt(size);
    s.writeInt(mode);

    for (int i = 0; i < size; i++) {
      nxlf = nxlf.getRight();
      s.writeObject(nxlf.getValue());
    }

    if (modCount != expectedModCount)
      throw new ConcurrentModificationException();
  }

  private final void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException
  {
    s.defaultReadObject();

    int size = s.readInt();
    int mode = s.readInt();

    Leaf pv = null; Leaf v = null;
    Leaf head = null;

    for (int i = 0; i < size; i++) {
      pv = v;
      v = new Leaf(s.readObject());
      if (pv == null) {
        head = v;
      } else {
        pv.setRight(v);
        v.setLeft(pv);
      }
    }
    TList tlist = getTList(size, head);
    root = tlist.root;
    this.head = tlist.head;
    tail = tlist.tail;
    this.mode = mode;
    uprb = 0;
  }

  public final void rebuild()
  {
    if (size() < 4) {
      return;
    }
    uprb = 0;

    3Q front = new Object()
    {
      TList.Node val = null;

      3Q next = null;
    };
    front.val = ((Node)root);
    3Q back = front;
    3Q curr = front;
    Object c = null;
    3Q q = null;

    while (curr != null) {
      c = curr.val.getLeft();
      if ((c instanceof Node)) {
        q = new Object()
        {
          TList.Node val = null;

          3Q next = null;
        };
        q.val = ((Node)c);
        q.val.setWeight(2);
        back.next = q;
        back = q;
      }
      c = curr.val.getRight();
      if ((c instanceof Node)) {
        q = new Object()
        {
          TList.Node val = null;

          3Q next = null;
        };
        q.val = ((Node)c);
        q.val.setWeight(2);
        back.next = q;
        back = q;
      }

      curr = curr.next;
    }

    curr = front;
    3Q posn = curr;
    int wt = ((Node)root).getWeight();

    while (wt > 3) {
      int wl = wt / 2;
      int wr = wt - wl;

      posn = posn.next;
      posn.val.setWeight(wl);

      posn = posn.next;
      posn.val.setWeight(wr);

      curr = curr.next;
      wt = curr.val.getWeight();
    }

    curr = front;
    posn = front;
    Node g = null;

    while (curr != null)
    {
      g = curr.val;
      wt = g.getWeight();
      int wl = wt / 2;
      int wr = wt - wl;

      if (wl > 1) {
        posn = posn.next;
        g.setLeft(posn.val);
      }

      if (wr > 1) {
        posn = posn.next;
        g.setRight(posn.val);
      }

      curr = curr.next;
    }

    int w = size();

    Object node = null; Object xnode = null;
    Leaf nxlf = new Leaf();
    nxlf.setRight(head);
    int index = 0;
    do
    {
      node = (Node)root;
      int wn = w;
      int first = 0;
      int last = w;

      while (wn > 4)
      {
        int pivot = first;
        xnode = ((Node)node).getLeft();
        wn = ((Node)xnode).getWeight();
        pivot += wn;

        if (index < pivot) {
          last = pivot;
          node = xnode; continue;
        }
        first = pivot;
        node = (Node)(Node)((Node)node).getRight();
        wn = ((Node)node).getWeight();
      }

      if (wn == 4) {
        xnode = (Node)(Node)((Node)node).getLeft();
        nxlf = nxlf.getRight();
        ((Node)xnode).setLeft(nxlf);
        index++;
        nxlf = nxlf.getRight();
        ((Node)xnode).setRight(nxlf);
        index++;
        node = (Node)(Node)((Node)node).getRight();
      }
      if (wn == 3) {
        nxlf = nxlf.getRight();
        ((Node)node).setLeft(nxlf);
        index++;
        node = (Node)(Node)((Node)node).getRight();
      }
      nxlf = nxlf.getRight();
      ((Node)node).setLeft(nxlf);
      index++;
      nxlf = nxlf.getRight();
      ((Node)node).setRight(nxlf);
      index++;
    }
    while (index < w);
  }

  private final void rebuildTest()
  {
    if (size() < 4) {
      return;
    }
    if (mode == 1) {
      return;
    }

    int exp = 0; for (int v = size(); v > 0; v /= 2) exp++;

    if (uprb < 1024 * exp) {
      return;
    }
    rebuild();
  }

  private class SubTList extends AbstractList
    implements List, RandomAccess
  {
    private AbstractList list;
    private int offset;
    private int size;
    private TList.Leaf head;
    private TList.Leaf tail;
    private int expectedModCount;

    public void add(int index, Object o)
      throws IndexOutOfBoundsException
    {
      if (index < 0)
        throw new IndexOutOfBoundsException();
      if (index > size) {
        throw new IndexOutOfBoundsException();
      }
      if ((list instanceof SubTList)) {
        if (((SubTList)list).modCount != expectedModCount)
          throw new ConcurrentModificationException();
      } else if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      try
      {
        if ((list instanceof SubTList)) {
          ((SubTList)list).add(index + offset, o);
          expectedModCount = ((SubTList)list).modCount;
        } else {
          TList.this.add(index + offset, o);
          expectedModCount = modCount;
        }
        size += 1;
        modCount += 1;
        head = getLeaf(0);
        tail = getLeaf(size - 1);
      } catch (IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException();
      }
    }

    public boolean add(Object o)
    {
      add(size, o);
      return true;
    }

    private void addFirst(Object o)
    {
      add(0, o);
    }

    private void addLast(Object o)
    {
      add(size, o);
    }

    public boolean addAll(Collection c)
    {
      return addAll(size, c);
    }

    public boolean addAll(int index, Collection c)
      throws IndexOutOfBoundsException
    {
      if (index < 0)
        throw new IndexOutOfBoundsException();
      if (index > size)
        throw new IndexOutOfBoundsException();
      int cize = c.size();
      if (cize == 0) {
        return false;
      }
      if ((list instanceof SubTList)) {
        if (((SubTList)list).modCount != expectedModCount)
          throw new ConcurrentModificationException();
      } else if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      if ((list instanceof SubTList)) {
        ((SubTList)list).addAll(index + offset, c);
        expectedModCount = ((SubTList)list).modCount;
      } else {
        TList.this.addAll(index + offset, c);
        expectedModCount = modCount;
      }

      size += cize;
      modCount += 1;
      return true;
    }

    protected void removeRange(int fromIndex, int toIndex)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
      if ((fromIndex < 0) || (toIndex > size)) {
        throw new IndexOutOfBoundsException();
      }
      if (fromIndex > toIndex) {
        throw new IllegalArgumentException();
      }
      if (fromIndex == toIndex) {
        return;
      }
      if ((list instanceof SubTList)) {
        if (((SubTList)list).modCount != expectedModCount)
          throw new ConcurrentModificationException();
      } else if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      if ((list instanceof SubTList)) {
        ((SubTList)list).removeRange(fromIndex + offset, toIndex + offset);

        expectedModCount = ((SubTList)list).modCount;
      } else {
        TList.this.removeRange(fromIndex + offset, toIndex + offset);
        expectedModCount = modCount;
      }

      size -= toIndex - fromIndex;
      modCount += 1;
      if (size == 0) {
        head = null;
        tail = null;
      } else {
        head = getLeaf(0);
        tail = getLeaf(size - 1);
      }
    }

    public void clear()
    {
      int fromIndex = 0;
      int toIndex = size;
      removeRange(fromIndex, toIndex);
    }

    public boolean contains(Object o)
    {
      Iterator iter = iterator();
      while (iter.hasNext()) {
        Object o2 = iter.next();
        if (o == null) {
          if (o2 == null)
            return true;
        }
        else if ((o2 != null) && 
          (o2.equals(o))) {
          return true;
        }
      }
      return false;
    }

    public boolean containsAll(Collection c)
      throws NullPointerException
    {
      if (c == null)
        throw new NullPointerException();
      Iterator iter = c.iterator();
      while (iter.hasNext()) {
        if (!contains(iter.next()))
          return false;
      }
      return true;
    }

    public Object get(int index)
      throws IndexOutOfBoundsException
    {
      if ((index < 0) || (index >= size)) {
        throw new IndexOutOfBoundsException();
      }
      if ((list instanceof SubTList)) {
        if (((SubTList)list).modCount != expectedModCount)
          throw new ConcurrentModificationException();
      } else if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      if ((list instanceof SubTList)) {
        return ((SubTList)list).get(index + offset);
      }
      return TList.this.get(index + offset);
    }

    private TList.Leaf getLeaf(int index) throws IndexOutOfBoundsException
    {
      if ((index < 0) || (index >= size)) {
        throw new IndexOutOfBoundsException();
      }
      if ((list instanceof SubTList)) {
        if (((SubTList)list).modCount != expectedModCount)
          throw new ConcurrentModificationException();
      } else if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      if ((list instanceof SubTList)) {
        return ((SubTList)list).getLeaf(index + offset);
      }
      return TList.this.getLeaf(index + offset);
    }

    private Object getFirst() {
      if (root == null) {
        throw new NoSuchElementException();
      }
      return get(0);
    }

    private Object getLast() {
      if (root == null) {
        throw new NoSuchElementException();
      }
      return get(size - 1);
    }

    public int indexOf(Object o)
    {
      ListIterator lter = listIterator();
      if (o == null) {
        do if (!lter.hasNext())
            break; while (lter.next() != null);
        return lter.previousIndex();
      }
      while (lter.hasNext()) {
        if (o.equals(lter.next()))
          return lter.previousIndex();
      }
      return -1;
    }

    public boolean isEmpty()
    {
      return size == 0;
    }

    public Iterator iterator()
    {
      SubTList sublist = this;

      return new Object(sublist)
      {
        TList.Leaf curr;
        int index;
        boolean ncall;
        int expectedModCount;

        public final void remove()
          throws IllegalStateException
        {
          if (!ncall)
            throw new IllegalStateException();
          if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
          try {
            ncall = false;
            val$sublist.remove(--index);
            expectedModCount = modCount;
          }
          catch (IndexOutOfBoundsException e)
          {
            throw new ConcurrentModificationException();
          }
        }

        public final boolean hasNext() {
          return curr != null;
        }

        public final Object next()
          throws NoSuchElementException
        {
          if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
          try
          {
            Object v = curr.getValue();
            index += 1;
            if (index == size)
              curr = null;
            else
              curr = curr.getRight();
            ncall = true;
            return v;
          } catch (IndexOutOfBoundsException e) {
            if (modCount != expectedModCount)
              throw new ConcurrentModificationException(); 
          }
          throw new NoSuchElementException();
        }
      };
    }

    public int lastIndexOf(Object o)
    {
      ListIterator lter = listIterator(size());
      if (o == null) {
        do if (!lter.hasPrevious())
            break; while (lter.previous() != null);
        return lter.nextIndex();
      }
      while (lter.hasPrevious()) {
        if (o.equals(lter.previous()))
          return lter.nextIndex();
      }
      return -1;
    }

    public ListIterator listIterator()
    {
      return listIterator(0);
    }

    public ListIterator listIterator(int position)
      throws IndexOutOfBoundsException
    {
      SubTList sublist = this;

      if ((position < 0) || (position > size)) {
        throw new IndexOutOfBoundsException();
      }
      return new Object(position, sublist)
      {
        TList.Leaf curr;
        TList.Leaf prev;
        int index;
        boolean ncall;
        boolean pcall;
        boolean acall;
        boolean rcall;
        int expectedModCount;

        public final boolean hasPrevious()
        {
          return prev != null;
        }

        public final Object next()
          throws NoSuchElementException
        {
          if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
          try
          {
            Object v = curr.getValue();
            prev = curr;
            index += 1;
            if (index == size) {
              curr = null;
            }
            else
            {
              curr = prev.getRight();
            }
            ncall = true;
            pcall = false;
            acall = false;
            rcall = false;
            return v;
          } catch (IndexOutOfBoundsException e) {
            if (modCount != expectedModCount)
              throw new ConcurrentModificationException(); 
          }
          throw new NoSuchElementException();
        }

        public final int nextIndex()
        {
          return index;
        }

        public final Object previous()
          throws NoSuchElementException
        {
          if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
          try
          {
            index -= 1;
            Object v = prev.getValue();
            curr = prev;
            if (index == 0) {
              prev = null;
            }
            else
            {
              prev = curr.getLeft();
            }
            pcall = true;
            ncall = false;
            acall = false;
            rcall = false;
            return v;
          } catch (IndexOutOfBoundsException e) {
            if (modCount != expectedModCount)
              throw new ConcurrentModificationException(); 
          }
          throw new NoSuchElementException();
        }

        public final int previousIndex()
        {
          return index - 1;
        }

        public final void remove() throws IllegalStateException
        {
          if ((!ncall) && (!pcall))
            throw new IllegalStateException();
          if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
          }

          try
          {
            if (ncall) {
              val$sublist.remove(--index);
              expectedModCount = modCount;
              if (index == 0)
                prev = null;
              else
                prev = prev.getLeft();
            } else {
              val$sublist.remove(index);
              expectedModCount = modCount;
              if (index == size)
                curr = null;
              else
                curr = curr.getRight();
            }
            rcall = true;
            ncall = false;
            pcall = false;
            acall = false;
          } catch (IndexOutOfBoundsException e) {
            throw new ConcurrentModificationException();
          }
        }

        public final boolean hasNext() {
          return curr != null;
        }

        public final void add(Object o) {
          if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
          }

          try
          {
            val$sublist.add(index, o);
            expectedModCount = modCount;
            index += 1;

            if (curr == null) {
              prev = val$sublist.tail;
            } else {
              curr = val$sublist.getLeaf(index);
              prev = curr.getLeft();
            }
            acall = true;
            ncall = false;
            pcall = false;
            rcall = false;
          } catch (IndexOutOfBoundsException e) {
            throw new ConcurrentModificationException();
          }
        }

        public final void set(Object o) throws IllegalStateException
        {
          if ((!ncall) && (!pcall))
            throw new IllegalStateException();
          if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
          if (ncall)
          {
            prev.setValue(o);
          }
          else
            curr.setValue(o);
        }
      };
    }

    public Object remove(int index)
      throws IndexOutOfBoundsException
    {
      if (index < 0)
        throw new IndexOutOfBoundsException();
      if (index >= size) {
        throw new IndexOutOfBoundsException();
      }
      if ((list instanceof SubTList)) {
        if (((SubTList)list).modCount != expectedModCount)
          throw new ConcurrentModificationException();
      } else if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
      try
      {
        Object o = null;
        if ((list instanceof SubTList)) {
          o = ((SubTList)list).remove(index + offset);
          expectedModCount = ((SubTList)list).modCount;
        } else {
          o = TList.this.remove(index + offset);
          expectedModCount = modCount;
        }
        size -= 1;
        modCount += 1;

        if (size == 0) {
          head = null;
          tail = null;
        } else {
          head = getLeaf(0);
          tail = getLeaf(size - 1);
        }
        return o; } catch (IndexOutOfBoundsException e) {
      }
      throw new ConcurrentModificationException();
    }

    private Object removeFirst()
    {
      return remove(0);
    }

    private Object removeLast() {
      return remove(size - 1);
    }

    public boolean remove(Object o)
    {
      Iterator iter = iterator();
      boolean changed = false;
      Object o2 = null;
      while (iter.hasNext()) {
        o2 = iter.next();
        if (o == null) {
          if (o2 == null) {
            iter.remove();

            return true;
          }
        }
        if ((o2 == null) || 
          (!o2.equals(o))) continue;
        iter.remove();

        return true;
      }

      return false;
    }

    public boolean removeAll(Collection c)
      throws NullPointerException
    {
      if (c == null) {
        throw new NullPointerException();
      }
      boolean changed = false;
      Iterator iter = iterator();
      while (iter.hasNext()) {
        if (c.contains(iter.next())) {
          iter.remove();
          changed = true;
        }
      }
      return changed;
    }

    public boolean retainAll(Collection c)
      throws NullPointerException
    {
      if (c == null)
        throw new NullPointerException();
      boolean changed = false;
      Iterator iter = iterator();
      while (iter.hasNext()) {
        if (!c.contains(iter.next())) {
          iter.remove();
          changed = true;
        }
      }
      return changed;
    }

    public Object set(int index, Object o)
      throws IndexOutOfBoundsException
    {
      if ((index < 0) || (index >= size)) {
        throw new IndexOutOfBoundsException();
      }
      if ((list instanceof SubTList)) {
        if (((SubTList)list).modCount != expectedModCount)
          throw new ConcurrentModificationException();
      } else if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
      try {
        Object v = null;
        if ((list instanceof SubTList)) {
          v = ((SubTList)list).set(index + offset, o);
        }
        else {
          v = TList.this.set(index + offset, o);
        }

        return v; } catch (IndexOutOfBoundsException e) {
      }
      throw new ConcurrentModificationException();
    }

    public int size()
    {
      if ((list instanceof SubTList)) {
        if (((SubTList)list).modCount != expectedModCount)
          throw new ConcurrentModificationException();
      } else if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      return size;
    }

    public List subList(int fromIndex, int toIndex)
    {
      return new SubTList(TList.this, this, fromIndex, toIndex);
    }

    public Object[] toArray()
    {
      int s = size;
      Object[] o = new Object[s];
      Iterator iter = iterator();
      int i = 0;
      while (iter.hasNext()) {
        o[i] = iter.next();
        i++;
      }
      return o;
    }

    public Object[] toArray(Object[] o)
      throws NullPointerException
    {
      if (o == null)
        throw new NullPointerException();
      int s = size;
      if (o.length < s) {
        o = (Object[])(Object[])Array.newInstance(o.getClass().getComponentType(), s);
      }
      Iterator iter = iterator();
      int i = 0;
      while (iter.hasNext()) {
        o[i] = iter.next();
        i++;
      }
      if (o.length > s)
        o[s] = null;
      return o;
    }

    public SubTList()
    {
    }

    public SubTList(AbstractList aList, int fromIndex, int toIndex)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
      list = aList;
      int sz = 0;
      if ((list instanceof SubTList))
        sz = ((SubTList)list).size;
      else
        sz = TList.this.size();
      if (fromIndex < 0)
        throw new IndexOutOfBoundsException();
      if (toIndex > sz)
        throw new IndexOutOfBoundsException();
      if (fromIndex > toIndex) {
        throw new IllegalArgumentException();
      }
      size = (toIndex - fromIndex);
      offset = fromIndex;
      if ((list instanceof SubTList))
        expectedModCount = ((SubTList)list).modCount;
      else {
        expectedModCount = modCount;
      }
      if (size == 0) {
        head = null;
        tail = null;
      } else {
        head = getLeaf(0);
        if (size == 1)
          tail = head;
        else
          tail = getLeaf(size - 1);
      }
    }
  }

  private class Leaf
  {
    private Leaf llink;
    private Leaf rlink;
    private Object value;

    Leaf()
    {
      llink = null;
      rlink = null;
      value = null;
    }

    Leaf(Object o) {
      llink = null;
      rlink = null;
      value = o;
    }

    final void setValue(Object o) {
      value = o;
    }

    final Object getValue() {
      return value;
    }

    final void setLeft(Leaf lleaf) {
      llink = lleaf;
    }

    final void setRight(Leaf rleaf) {
      rlink = rleaf;
    }

    final Leaf getLeft() {
      return llink;
    }

    final Leaf getRight() {
      return rlink;
    }
  }

  private class Node
  {
    private int weight;
    private Object left;
    private Object right;

    Node()
    {
      weight = 0;
      left = null;
      right = null;
    }

    Node(int aWeight) {
      weight = aWeight;
      left = null;
      right = null;
    }

    final void setWeight(int aWeight) {
      weight = aWeight;
    }

    final void setLeft(Object lchild) {
      left = lchild;
    }

    final void setRight(Object rchild) {
      right = rchild;
    }

    final int getWeight() {
      return weight;
    }

    final Object getLeft() {
      return left;
    }

    final Object getRight() {
      return right;
    }
  }
}