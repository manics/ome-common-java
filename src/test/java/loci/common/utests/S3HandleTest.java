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

import loci.common.S3Handle;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Unit tests for the loci.common.S3Handle class.
 *
 * @see loci.common.URLHandle
 */
public class S3HandleTest {

  // -- Fields --

  // -- Setup methods --

  @BeforeMethod
  public void setup() {
    // no-op
  }

  // -- Test methods --

  @Test
  public void testParseDefault() throws IOException {
    S3Handle s3 = new S3Handle("s3://bucket/key/file.tif", false);
    assertEquals(S3Handle.DEFAULT_SERVER, s3.getServer());
    assertEquals(0, s3.getPort());
    assertEquals("bucket", s3.getBucket());
    assertEquals("key/file.tif", s3.getPath());
  }

  @Test
  public void testParseLocalhost() throws IOException {
    S3Handle s3 = new S3Handle("s3://bucket.localhost:9000/key/file.tif", false);
    assertEquals("http://localhost", s3.getServer());
    assertEquals(9000, s3.getPort());
    assertEquals("bucket", s3.getBucket());
    assertEquals("key/file.tif", s3.getPath());
  }

  @Test
  public void testSetLocalhost() throws IOException {
    S3Handle s3 = new S3Handle("localhost", "s3://bucket/key/file.tif", false);
    assertEquals("http://localhost", s3.getServer());
    assertEquals(0, s3.getPort());
    assertEquals("bucket", s3.getBucket());
    assertEquals("key/file.tif", s3.getPath());
  }

  @Test
  public void testParseAuth() throws IOException {
    S3Handle s3 = new S3Handle("s3://access:secret@bucket/key/file.tif", false);
    assertEquals(S3Handle.DEFAULT_SERVER, s3.getServer());
    assertEquals(0, s3.getPort());
    assertEquals("bucket", s3.getBucket());
    assertEquals("key/file.tif", s3.getPath());
  }

  @Test
  public void testParseAuthLocalhost() throws IOException {
    S3Handle s3 = new S3Handle("s3://access:secret@bucket.localhost:9000/key/file.tif", false);
    assertEquals("http://localhost", s3.getServer());
    assertEquals(9000, s3.getPort());
    assertEquals("bucket", s3.getBucket());
    assertEquals("key/file.tif", s3.getPath());
  }

}
