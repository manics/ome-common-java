//
// ReadCharTest.java
//

/*
LOCI Common package: utilities for I/O, reflection and miscellaneous tasks.
Copyright (C) 2005-@year@ Melissa Linkert and Curtis Rueden.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package loci.common.utests;

import java.io.IOException;

import loci.common.IRandomAccess;
import loci.common.utests.providers.IRandomAccessProvider;
import loci.common.utests.providers.IRandomAccessProviderFactory;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Tests for reading characters from a loci.common.IRandomAccess.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/common/test/loci/common/utests/ReadCharTest.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/common/test/loci/common/utests/ReadCharTest.java">SVN</a></dd></dl>
 *
 * @see loci.common.IRandomAccess
 */
@Test(groups="readTests")
public class ReadCharTest {

  private static final byte[] PAGE = new byte[] {
    // 16-bit unicode encoding
    (byte) 0x00, (byte) 0x61, (byte) 0x00, (byte) 0x62,  // a, b
    (byte) 0x00, (byte) 0x63, (byte) 0x00, (byte) 0x64,  // c, d
    (byte) 0x00, (byte) 0x65, (byte) 0x00, (byte) 0x66,  // e, f
    (byte) 0x00, (byte) 0x67, (byte) 0x00, (byte) 0x68,  // g, h
    (byte) 0x00, (byte) 0x69, (byte) 0x00, (byte) 0x6A,  // i, j
    (byte) 0x00, (byte) 0x6B, (byte) 0x00, (byte) 0x6C,  // k, l
    (byte) 0x00, (byte) 0x6D, (byte) 0x00, (byte) 0x6E,  // m, n
    (byte) 0x00, (byte) 0x6F, (byte) 0x00, (byte) 0x70   // o, p
  };

  private static final String MODE = "r";

  private static final int BUFFER_SIZE = 1024;

  private IRandomAccess fileHandle;

  @Parameters({"provider"})
  @BeforeMethod
  public void setUp(String provider) throws IOException {
    IRandomAccessProviderFactory factory = new IRandomAccessProviderFactory();
    IRandomAccessProvider instance = factory.getInstance(provider);
    fileHandle = instance.createMock(PAGE, MODE, BUFFER_SIZE);
  }

  @Test
  public void testLength() throws IOException {
    assertEquals(32, fileHandle.length());
  }

  @Test
  public void testSequentialReadChar() throws IOException {
    char[] expectedValues = new char[] { 
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
        'j', 'k', 'l', 'm', 'n', 'o', 'p'
    };
    for (char expectedValue : expectedValues) {
      char value = fileHandle.readChar();
      assertTrue(Character.isLetter(value));
      assertEquals(expectedValue, value);
    }
  }

  @Test
  public void testSeekForwardReadChar() throws IOException {
    fileHandle.seek(8);
    char value = fileHandle.readChar();
    assertTrue(Character.isLetter(value));
    assertEquals('e', value);
    value = fileHandle.readChar();
    assertTrue(Character.isLetter(value));
    assertEquals('f', value);
  }

  @Test
  public void testResetReadChar() throws IOException {
    char value = fileHandle.readChar();
    assertTrue(Character.isLetter(value));
    assertEquals('a', value);
    value = fileHandle.readChar();
    assertTrue(Character.isLetter(value));
    assertEquals('b', value);
    fileHandle.seek(0);
    value = fileHandle.readChar();
    assertTrue(Character.isLetter(value));
    assertEquals('a', value);
    value = fileHandle.readChar();
    assertTrue(Character.isLetter(value));
    assertEquals('b', value);
  }

  @Test
  public void testSeekBackReadChar() throws IOException {
    fileHandle.seek(16);
    fileHandle.seek(8);
    char value = fileHandle.readChar();
    assertTrue(Character.isLetter(value));
    assertEquals('e', value);
    value = fileHandle.readChar();
    assertTrue(Character.isLetter(value));
    assertEquals('f', value);
  }

  @Test
  public void testRandomAccessReadChar() throws IOException {
    testSeekForwardReadChar();
    testSeekBackReadChar();
    // The test relies on a "new" file or reset file pointer
    fileHandle.seek(0);
    testResetReadChar();
  }

}
