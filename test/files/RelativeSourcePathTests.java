package files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RelativeSourcePathTests
{
  @Test public void aa_relativeSourcePath()
  {
    Package abc = new Package("a.b.c");
    Path path = Paths.get("a/b/c/File.java");

    RelativeSourcePath one = new RelativeSourcePath(abc, "File.java");
    assertTrue  (one.pkg() == abc);
    assertEquals("File", one.fileStem());
    assertEquals("java", one.fileExt());
    assertEquals("File.java", one.fileName());
    assertEquals(path, one.relativePath());
    assertEquals("a.b.c.File", one.pkgString());

    RelativeSourcePath two = new RelativeSourcePath(path);
    assertEquals(abc, two.pkg());
    assertEquals("File", two.fileStem());
    assertEquals("java", two.fileExt());
    assertEquals("File.java", two.fileName());
    assertTrue  (path == two.relativePath());
    assertEquals("a.b.c.File", two.pkgString());

    assertEquals(one, two);
    assertTrue(one.hashCode() == two.hashCode());

    RelativeSourcePath three = new RelativeSourcePath(abc, "File.javb");
    assertFalse(one.equals(three));
    assertFalse(one.hashCode() == three.hashCode());
  }
}
