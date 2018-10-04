/*
 * #%L
 * Common package for I/O and related utilities
 * %%
 * Copyright (C) 2005 - 2016 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.common.utests;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import loci.common.Location;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit tests for the loci.common.Location class.
 *
 * @see loci.common.Location
 */
public class LocationTest {

  private static final boolean IS_WINDOWS = System.getProperty("os.name").startsWith("Windows");

  // -- Fields --

  private Location[] files;
  private boolean[] exists;
  private boolean[] isDirectory;
  private boolean[] isHidden;
  private String[] mode;
  private boolean[] isRemote;
  private boolean isOnline;

  // -- Setup methods --

  @BeforeClass
  public void setup() throws IOException {
    File tmpDirectory = new File(System.getProperty("java.io.tmpdir"),
      System.currentTimeMillis() + "-location-test");
    boolean success = tmpDirectory.mkdirs();
    tmpDirectory.deleteOnExit();

    File hiddenFile = File.createTempFile(".hiddenTest", null, tmpDirectory);
    hiddenFile.deleteOnExit();

    File invalidFile = File.createTempFile("invalidTest", null, tmpDirectory);
    String invalidPath = invalidFile.getAbsolutePath();
    invalidFile.delete();

    File validFile = File.createTempFile("validTest", null, tmpDirectory);
    validFile.deleteOnExit();

    files = new Location[] {
      new Location(validFile.getAbsolutePath()),
      new Location(invalidPath),
      new Location(tmpDirectory),
      new Location("http://www.openmicroscopy.org/"),
      new Location("https://www.openmicroscopy.org/"),
      new Location("https://www.openmicroscopy.org/nonexisting"),
      new Location(hiddenFile),
      new Location("s3://server/bucket-dne/key/"),
      new Location("s3://server/bucket-dne/key/file.tif"),
    };

    exists = new boolean[] {
      true, false, true, true, true, false, true, false, false
    };

    isDirectory = new boolean[] {
      false, false, true, false, false, false, false, false, false
    };

    isHidden = new boolean[] {
      false, false, false, false, false, false, true, false, false
    };

    mode = new String[] {
      "rw", "", "rw", "r", "r", "","rw", "", "" // S3 isn't readable
    };

    isRemote = new boolean[] {
      false, false, false, true, true, true, false, true, true
    };
  }

  @BeforeClass
  public void checkIfOnline() throws IOException {
    try {
      new Socket("www.openmicroscopy.org", 80).close();
      isOnline = true;
    } catch (IOException e) {
      isOnline = false;
    }
  }

  private void skipIfOffline(int i) throws SkipException {
    if (isRemote[i] && !isOnline) {
      throw new SkipException("must be online to test " + files[i].getName());
    }
  }

  // -- Tests --

  @Test
  public void testReadWriteMode() {
    for (int i=0; i<files.length; i++) {
      skipIfOffline(i);
      String msg = files[i].getName();
      assertEquals(msg, files[i].canRead(), mode[i].contains("r"));
      assertEquals(msg, files[i].canWrite(), mode[i].contains("w"));
    }
  }

  @Test
  public void testAbsolute() {
    for (Location file : files) {
      assertEquals(file.getName(), file.getAbsolutePath(),
        file.getAbsoluteFile().getAbsolutePath());
    }
  }

  @Test
  public void testExists() {
    for (int i=0; i<files.length; i++) {
      skipIfOffline(i);
      assertEquals(files[i].getName(), files[i].exists(), exists[i]);
    }
  }

  @Test
  public void testCanonical() throws IOException {
    for (Location file : files) {
      assertEquals(file.getName(), file.getCanonicalPath(),
        file.getCanonicalFile().getAbsolutePath());
    }
  }

  @Test
  public void testParent() {
    for (Location file : files) {
      assertEquals(file.getName(), file.getParent(),
        file.getParentFile().getAbsolutePath());
    }
  }

  @Test
  public void testIsDirectory() {
    for (int i=0; i<files.length; i++) {
      assertEquals(files[i].getName(), files[i].isDirectory(), isDirectory[i]);
    }
  }

  @Test
  public void testIsFile() {
    for (int i=0; i<files.length; i++) {
      skipIfOffline(i);
      assertEquals(files[i].getName(), files[i].isFile(),
        !isDirectory[i] && exists[i]);
    }
  }

  @Test
  public void testIsHidden() {
    for (int i=0; i<files.length; i++) {
      assertEquals(files[i].getName(), files[i].isHidden() || IS_WINDOWS, isHidden[i] || IS_WINDOWS);
    }
  }

  @Test
  public void testListFiles() {
    for (int i=0; i<files.length; i++) {
      String[] completeList = files[i].list();
      String[] unhiddenList = files[i].list(true);
      Location[] fileList = files[i].listFiles();

      if (!files[i].isDirectory()) {
        assertEquals(files[i].getName(), completeList, null);
        assertEquals(files[i].getName(), unhiddenList, null);
        assertEquals(files[i].getName(), fileList, null);
        continue;
      }

      assertEquals(files[i].getName(), completeList.length, fileList.length);

      List<String> complete = Arrays.asList(completeList);
      for (String child : unhiddenList) {
        assertEquals(files[i].getName(), complete.contains(child), true);
        assertEquals(files[i].getName(),
          new Location(files[i], child).isHidden(), false);
      }

      for (int f=0; f<fileList.length; f++) {
        assertEquals(files[i].getName(),
          fileList[f].getName(), completeList[f]);
      }
    }
  }

  @Test
  public void testToURL() throws IOException {
    for (Location file : files) {
      String path = file.getAbsolutePath();
      if (!path.contains("://")) {
        if (IS_WINDOWS) {
          path = "file:/" + path;
        }
        else {
          path = "file://" + path;
        }
      }
      if (file.isDirectory() && !path.endsWith(File.separator)) {
        path += File.separator;
      }
      try {
        assertEquals(file.getName(), file.toURL(), new URL(path));
      } catch (MalformedURLException e) {
        assertEquals(path, true, path.contains("s3://"));
      }
    }
  }

  @Test
  public void testToString() {
    for (Location file : files) {
      assertEquals(file.getName(), file.toString(), file.getAbsolutePath());
    }
  }

}
