package driver;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static util.ListUtils.list;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;

import files.BackedRequire;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import files.RelativeSourcePath;
import files.Require;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RequiresTests
{
  @Test public void aa_addBacked()
  {
    RelativeSourcePath path =
      new RelativeSourcePath(Paths.get("a/b/File.java"));

    Require req1 = new Require(path, list("A"), null);
    Require req2 = new Require(path, list("B"), null);
    Require req3 = new Require(path, list("C"), null);

    SourceFile file1 = mock(SourceFile.class);
    SourceFile file2 = mock(SourceFile.class);

    BackedRequire breq1 = new BackedRequire(req1, file1);
    BackedRequire breq2 = new BackedRequire(req2, file1);
    BackedRequire breq3 = new BackedRequire(req3, file2);

    Requires requires = new Requires();
    requires.add(breq1);
    requires.add(breq2);
    requires.add(breq3);

    Set<SourceFile> deps = requires.dependencies();
    assertTrue(deps.containsAll(list(file1, file2)));

    Collection<BackedRequire> breqs = requires.get();
    assertTrue(breqs.containsAll(list(breq1, breq2, breq3)));
  }
}
