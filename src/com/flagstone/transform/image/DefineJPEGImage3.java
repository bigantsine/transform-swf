/*
 * DefineJPEGImage3.java
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

package com.flagstone.transform.image;

import java.util.Arrays;

import com.flagstone.transform.Strings;
import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.FLVDecoder;
import com.flagstone.transform.coder.ImageTag;
import com.flagstone.transform.coder.MovieTypes;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * DefineJPEGImage3 is used to define a transparent JPEG encoded image.
 * 
 * <p>
 * It extends the DefineJPEGImage2 class by including a separate zlib compressed
 * table of alpha channel values. This allows the transparency of existing JPEG
 * encoded images to be changed without re-encoding the original image.
 * </p>
 * 
 * @see DefineJPEGImage3
 */
public final class DefineJPEGImage3 implements ImageTag {
    private static final String FORMAT = "DefineJPEGImage3: { identifier=%d; image=%d; alpha=%d }";

    private transient int length;
    private transient int width;
    private transient int height;

    private byte[] image;
    private byte[] alpha;
    private int identifier;

    // TODO(doc)
    public DefineJPEGImage3(final SWFDecoder coder) throws CoderException {
        final int start = coder.getPointer();
        length = coder.readWord(2, false) & 0x3F;

        if (length == 0x3F) {
            length = coder.readWord(4, false);
        }
        final int end = coder.getPointer() + (length << 3);
        identifier = coder.readWord(2, false);

        final int offset = coder.readWord(4, false);

        image = coder.readBytes(new byte[offset]);
        alpha = coder.readBytes(new byte[length - offset - 6]);

        decodeInfo();

        if (coder.getPointer() != end) {
            throw new CoderException(getClass().getName(), start >> 3, length,
                    (coder.getPointer() - end) >> 3);
        }
    }

    /**
     * Creates a DefineJPEGImage3 object with the specified image data, and
     * alpha channel data.
     * 
     * @param uid
     *            the unique identifier for this object. Must be in the range
     *            1..65535.
     * @param image
     *            the JPEG encoded image data. Must not be null.
     * @param alpha
     *            byte array containing the zlib compressed alpha channel data.
     *            Must not be null.
     */
    public DefineJPEGImage3(final int uid, final byte[] image,
            final byte[] alpha) {
        setIdentifier(uid);
        setImage(image);
        setAlpha(alpha);
    }

    // TODO(doc)
    public DefineJPEGImage3(final DefineJPEGImage3 object) {
        identifier = object.identifier;
        width = object.width;
        height = object.height;
        image = Arrays.copyOf(object.image, object.image.length);
        alpha = Arrays.copyOf(object.alpha, object.alpha.length);
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final int uid) {
        if ((uid < 0) || (uid > 65535)) {
            throw new IllegalArgumentException(Strings.IDENTIFIER_RANGE);
        }
        identifier = uid;
    }

    /**
     * Returns the width of the image in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the image in pixels.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the image data.
     */
    public byte[] getImage() {
        return image;
    }

    /**
     * Returns the alpha channel data.
     */
    public byte[] getAlpha() {
        return alpha;
    }

    /**
     * Sets the image data.
     * 
     * @param bytes
     *            an array of bytes containing the image table. Must not be
     *            null.
     */
    public void setImage(final byte[] bytes) {
        image = bytes;
        decodeInfo();
    }

    /**
     * Sets the alpha channel data with the zlib compressed data.
     * 
     * @param bytes
     *            array of bytes containing zlib encoded alpha channel. Must not
     *            be null.
     */
    public void setAlpha(final byte[] bytes) {
        alpha = bytes;
    }

    public DefineJPEGImage3 copy() {
        return new DefineJPEGImage3(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, image.length, alpha.length);
    }

    public int prepareToEncode(final SWFEncoder coder, final Context context) {
        length = 6;
        length += image.length;
        length += alpha.length;

        return (length > 62 ? 6 : 2) + length;
    }

    public void encode(final SWFEncoder coder, final Context context)
            throws CoderException {
        final int start = coder.getPointer();

        if (length >= 63) {
            coder.writeWord((MovieTypes.DEFINE_JPEG_IMAGE_3 << 6) | 0x3F, 2);
            coder.writeWord(length, 4);
        } else {
            coder.writeWord((MovieTypes.DEFINE_JPEG_IMAGE_3 << 6) | length, 2);
        }
        final int end = coder.getPointer() + (length << 3);

        coder.writeWord(identifier, 2);
        coder.writeWord(image.length, 4);
        coder.writeBytes(image);
        coder.writeBytes(alpha);

        if (coder.getPointer() != end) {
            throw new CoderException(getClass().getName(), start >> 3, length,
                    (coder.getPointer() - end) >> 3);
        }
    }

    private void decodeInfo() {
        final FLVDecoder coder = new FLVDecoder(image);

        if (coder.readWord(2, false) == 0xffd8) {
            int marker;

            do {
                marker = coder.readWord(2, false);

                if ((marker & 0xff00) == 0xff00) {
                    if ((marker >= 0xffc0) && (marker <= 0xffcf)
                            && (marker != 0xffc4) && (marker != 0xffc8)) {
                        coder.adjustPointer(24);
                        height = coder.readWord(2, false);
                        width = coder.readWord(2, false);
                        break;
                    } else {
                        coder
                                .adjustPointer((coder.readWord(2, false) - 2) << 3);
                    }
                }

            } while ((marker & 0xff00) == 0xff00);
        }
    }
}