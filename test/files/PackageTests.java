package files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.ListUtils.list;

import java.nio.file.Paths;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PackageTests
{
  @Test public void aa_package()
  {
    Package one = new Package("one.two.three");
    assertTrue(one.components().containsAll(list("one", "two", "three")));
    assertTrue(one.name().equals("one.two.three"));

    Package two = new Package(" one . two . three ");
    assertTrue(two.components().containsAll(list("one", "two", "three")));
    assertTrue(two.name().equals("one.two.three"));

    assertEquals(one, two);
    assertTrue(one.hashCode() == two.hashCode());

    Package three = new Package(list("one", "two", "three"));
    assertTrue(three.components().containsAll(list("one", "two", "three")));
    assertTrue(three.name().equals("one.two.three"));

    assertEquals(Paths.get("one/two/three"), one.relativePath());

    Package four = new Package("one.three.two");
    assertFalse(one.equals(four));
    assertFalse(one.hashCode() == four.hashCode());
  }
}
