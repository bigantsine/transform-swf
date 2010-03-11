/*
 * DecodeMovieTest.java
 * Transform
 *
 * Copyright (c) 2001-2009 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.flagstone.transform.tools;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.zip.DataFormatException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.flagstone.transform.Movie;
import com.flagstone.transform.tools.VideoWriter;
import com.flagstone.transform.video.Video;

/**
 * DecodeMovieTest is used to create Movies using all the Flash files in a given
 * directory to verify that they can be decoded correctly.
 */
public final class VideoWriterTest {
    private static File srcDir;
    private static File destDir;
    private static FilenameFilter filter;

    @BeforeClass
    public static void setUp() {
        if (System.getProperty("test.suite") == null) {
            srcDir = new File("test/data/flv/reference");
        } else {
            srcDir = new File(System.getProperty("test.suite"));
        }

        filter = new FilenameFilter() {
            public boolean accept(final File directory, final String name) {
                return name.endsWith(".flv");
            }
        };

        destDir = new File("test/results", "VideoWriterTest");

        if (!destDir.exists() && !destDir.mkdirs()) {
            fail();
        }
    }

    @Test
    public void write() throws DataFormatException, IOException {
        File sourceFile = null;
        File destFile;

        final Video video = new Video();
        final VideoWriter writer = new VideoWriter();

        final String[] files = srcDir.list(filter);

        for (final String file : files) {
            sourceFile = new File(srcDir, file);
            destFile = new File(destDir, file.replaceFirst("\\.flv", ".txt"));
            video.decodeFromFile(sourceFile);
            writer.write(video, destFile);
        }
    }
}