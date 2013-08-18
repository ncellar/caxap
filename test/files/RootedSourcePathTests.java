package files;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RootedSourcePathTests
{
  @Test public void aa_absoluteSourcePath()
  {
    Package pkg   = new Package("b.c");
    Path root     = Paths.get("a");
    Path absPath  = Paths.get("a/b/c/File.java");
    Path relPath  = Paths.get("b/c/File.java");

    RootedSourcePath one = new RootedSourcePath(root, relPath, true);
    assertEquals(root,     one.root());
    assertEquals(absPath,  one.absolutePath());
    assertEquals(relPath,  one.relativePath());

    RootedSourcePath two = new RootedSourcePath(root, absPath, false);
    assertEquals(root,     two.root());
    assertEquals(absPath,  two.absolutePath());
    assertEquals(relPath,  two.relativePath());

    RootedSourcePath three = new RootedSourcePath(root, pkg, "File.java");
    assertEquals(root,     three.root());
    assertEquals(absPath,  three.absolutePath());
    assertEquals(relPath,  three.relativePath());

    assertEquals(one, two);
    assertEquals(one, three);
    assertTrue(one.hashCode() == two.hashCode());
    assertTrue(one.hashCode() == three.hashCode());

    RootedSourcePath four =
      new RootedSourcePath(root, Paths.get("b/c/File.javb"), true);

    assertFalse(one.equals(four));
    assertFalse(one.hashCode() == four.hashCode());
  }
}
