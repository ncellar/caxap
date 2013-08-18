package driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import files.BackedRequire;

import files.Require;

/**
 * Manages the require statements included in a source file. A member of
 * CTimeFile.
 *
 * Require statements are kept as BackedRequire objects.
 */
public class Requires
{
  /****************************************************************************/
  private final Map<SourceFile, Set<BackedRequire>> map = new HashMap<>();

  /****************************************************************************/
  private final Set<BackedRequire> all = new HashSet<>();

  /****************************************************************************/
  @Override public String toString()
  {
    return map.values().toString();
  }

  /*****************************************************************************
   * Adds all Require's to this collection, using the Context's
   * CTimeFileRepository to find the backing source files.
   */
  void addAll(List<Require> requires)
  {
    SourceRepository repo = Context.get().repo;

    for (Require require : requires) {
      add(repo, require);
    }
  }

  /*****************************************************************************
   * Adds the Require to this collection, using $repo to find the backing source
   * file.
   */
  void add(SourceRepository repo, Require require)
  {
    SourceFile file = repo.get(require.relativePath());
    add(new BackedRequire(require, file));
  }

  /****************************************************************************/
  void add(BackedRequire require)
  {
    Set<BackedRequire> set = map.get(require.file());

    if (set == null) {
      set = new HashSet<BackedRequire>();
      map.put(require.file(), set);
    }

    set.add(require);
    all.add(require);
  }

  /****************************************************************************/
  Set<SourceFile> dependencies()
  {
    return map.keySet();
  }

  /****************************************************************************/
  Collection<BackedRequire> get()
  {
    return all;
  }

  /****************************************************************************/
  List<String> imports()
  {
    List<String> out = new ArrayList<>(all.size());

    for (BackedRequire breq : all) {
      String importString = breq.require().importString();

      if (!importString.isEmpty()) {
        out.add(importString);
      }
    }

    return out;
  }
}
