package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * UNUSED
 *
 * Minimal-effort implementation of a multi-map, with little regard for good
 * practices.
 */
@SuppressWarnings("serial")
public class Multimap<K, V> extends HashMap<K, Set<V>>
{
  /****************************************************************************/
  private Set<V> getOrNew(K key)
  {
    Set<V> set = get(key);

    if (set == null) {
      set = new HashSet<V>();
      put(key, set);
    }

    return set;
  }

  /****************************************************************************/
  public boolean add(K key, V value)
  {
    return getOrNew(key).add(value);
  }

  /****************************************************************************/
  public void add(K key, Collection<? extends V> values)
  {
    getOrNew(key).addAll(values);
  }

  /****************************************************************************/
  public Set<V> allValues()
  {
    Set<V> all = new HashSet<V>();

    for (Set<V> set : values()) {
      all.addAll(set);
    }

    return all;
  }
}
