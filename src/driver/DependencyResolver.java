package driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import files.RootedSourcePath;

/**
 * This class resolves the dependencies between source files used at
 * compile-time.
 *
 * Macro definitions are contained in macro files (.javam extension). There may
 * be zero or more macro definitions in one such file.
 *
 * There is currently no visibility control mechanism for macros, which means
 * that all macros are accessible from everywhere. Macros do however behave like
 * classes when it comes to accessing other classes (this comes from the fact
 * that macros are compiled into classes).
 *
 * Macro files may depend on other macro files or on regular source files (to be
 * used at compile-time). The regular source files may themselves depend on
 * macros or on other source files. We need to find an ordering of source files
 * such that all dependencies are met and no loops occur.
 *
 * The mechanism through which dependencies between macros and sources files are
 * defined is the require statement, a modified form of import statement. You
 * can learn more about the require statement in the javadoc of
 * {@link RequiresParser}.
 *
 * Macro files and source files used at compile time can also rely on
 * pre-compiled classes without hurdles (using the regular import statements).
 * The compiled classes (.class or .jar) just needs to be retrievable via
 * caxap's own classpath.
 *
 * It is important to note that due to the way dependencies are resolved, the
 * prelude (package declaration + import/require statements) in each file used
 * at compile-time can't contain macro calls.
 *
 * If a file requires a macro, the macro can be used anywhere in the file
 * (except, as said above, in the prelude of files used at compile-time). A
 * macro may be used in the file that defines it after the definition is
 * complete. A given macro's call cannot appear in its own expansion code,
 * neither can another macro's call which expand to a call of the given macro.
 * Macros whose expansion code use the given macro can't be called either.
 */
public class DependencyResolver
{
  /*****************************************************************************
   * Finds an ordering of macro and regular source files that satisfies all
   * compile-time dependencies. If no such ordering can be found - because
   * there is a circular dependency - throws an error.
   *
   * {@see {@link DependencyResolver}}
   */
  public static List<SourceFile> resolve(
    List<RootedSourcePath> macroPaths, List<RootedSourcePath> sourcePaths)
  {
    return new DependencyResolver().run(macroPaths, sourcePaths);
  }

  /****************************************************************************/
  private final SourceRepository repo = Context.get().repo;

  /****************************************************************************/
  private List<SourceFile> run(
    List<RootedSourcePath> macroPaths, List<RootedSourcePath> sourcePaths)
  {
    repo.hint(macroPaths);
    repo.hint(sourcePaths);

    List<SourceFile> files = new ArrayList<>();

    for (RootedSourcePath path : macroPaths) {
      files.add(repo.get(path));
    }

    Set <SourceFile> set     = detectCycles(files);
    List<SourceFile> ordered = parseOrdering(set);

    for (SourceFile file : ordered) {
      file.declareUsedAtCompileTime();
    }

    for (RootedSourcePath path : sourcePaths) {
      SourceFile file = repo.get(path);

      if (!set.contains(file)) {
        ordered.add(file);
      }
    }

    return ordered;
  }

  /*****************************************************************************
   * Detect cycles in files dependencies, using the direct dependencies stored
   * in the Requires object associated to each SourceFile. If a cycle is
   * encountered, throws an error. Returns the set of visited SourceFile.
   *
   * Checks for cycles by walking the dependency graph recursively, starting
   * from each file (= each node in the graph). This would normally result in
   * passing many times over the same file. But once a file has been checked, it
   * is guaranteed not to belong to any cycle. Therefore, we keep a set of
   * checked file to avoid to duplicate work.
   */
  private Set<SourceFile> detectCycles(List<SourceFile> files)
  {
    Set<SourceFile> checked = new HashSet<>();

    for (SourceFile file : files)
    if (!checked.contains(file)) {
      detectCycles(file, checked, new HashSet<SourceFile>());
      checked.add(file);
    }

    return checked;
  }

  /*****************************************************************************
   * Detects cycles by recursively walking $file and keeping files on the
   * current path in $visited. $checked is explained in
   * {@link DependencyResolver#detectCycles()}.
   */
  private void detectCycles(
    SourceFile file, Set<SourceFile> checked, Set<SourceFile> visited)
  {
    for (SourceFile dependency : file.requires().dependencies())
    if (!checked.contains(dependency))
    {
      // if visited did already contain $dependency
      if (!visited.add(dependency)) {
        throw new Error("Dependency loop detect for macro class \""
          + dependency + "\", via macro class \"" + file + "\".");
      }

      detectCycles(dependency, checked, visited);
      visited.remove(dependency);
      checked.add(dependency);
    }
  }

  /*****************************************************************************
   * Return an ordering of $files that respects the dependencies between them.
   *
   * If the need should arise, the time complexity of this algorithm can be
   * diminished by adding a HashSet that mirrors the content of the list.
   */
  private List<SourceFile> parseOrdering(Collection<SourceFile> files)
  {
    List<SourceFile> ordering = new ArrayList<>(files.size());

    for (SourceFile file : files)
    if (!ordering.contains(file)) {
      parseOrdering(file, ordering);
    }

    return ordering;
  }

  /*****************************************************************************
   * Adds $file to $ordering, ensuring that the dependencies of $file precedes
   * it in the $ordering, adding them recursively if needed.
   */
  private void parseOrdering(SourceFile file, List<SourceFile> ordering)
  {
    for (SourceFile dependency : file.requires().dependencies())
    if (!ordering.contains(dependency)) {
      parseOrdering(dependency, ordering);
    }

    ordering.add(file);
  }
}