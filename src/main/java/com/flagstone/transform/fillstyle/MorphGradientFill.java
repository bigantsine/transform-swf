/*
 * MorphGradientFill.java
 * Transform
 *
 * Copyright (c) 2001-2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.flagstone.transform.fillstyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.SWF;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.CoordTransform;

/**
 * MorphGradientFill defines how a colour gradient changes across an area filled
 * in a shape as it is morphed. {@link GradientFill} has a description of colour
 * gradients.
 *
 * @see MorphGradient
 * @see GradientFill
 */
//TODO(class)
public final class MorphGradientFill implements FillStyle {

    /** Format string used in toString() method. */
    private static final String FORMAT = "MorphGradientFill: { start=%s;"
            + " end=%s; gradients=%s }";

    /** Code used to identify the fill style when it is encoded. */
    private int type;
    private CoordTransform startTransform;
    private CoordTransform endTransform;
    private List<MorphGradient> gradients;

    private transient int count;

    /**
     * Creates and initialises an MorphGradientFill fill style using values
     * encoded in the Flash binary format.
     *
     * @param fillType the value used to identify the fill style when it is
     * encoded.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @param context
     *            a Context object used to manage the decoders for different
     *            type of object and to pass information on how objects are
     *            decoded.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public MorphGradientFill(final int fillType, final SWFDecoder coder,
            final Context context) throws IOException {
        type = fillType;
        startTransform = new CoordTransform(coder);
        endTransform = new CoordTransform(coder);
        count = coder.readByte() & SWF.MAX_GRADIENTS;

        gradients = new ArrayList<MorphGradient>(count);

        for (int i = 0; i < count; i++) {
            gradients.add(new MorphGradient(coder, context));
        }
    }

    /**
     * Creates a MorphGradientFill object specifying the type of fill, starting
     * and ending coordinate transforms and the array of gradient records.
     *
     * @param gradientType
     *            identifies whether the gradient is rendered linearly or
     *            radially.
     * @param start
     *            the coordinate transform mapping the gradient square onto
     *            physical coordinates at the start of the morphing process.
     * @param end
     *            the coordinate transform mapping the gradient square onto
     *            physical coordinates at the end of the morphing process.
     * @param grads
     *            an array of MorphGradient objects defining the control points
     *            for the gradient.
     */
    public MorphGradientFill(final GradientType gradientType,
            final CoordTransform start,
            final CoordTransform end,
            final List<MorphGradient> grads) {
        setType(gradientType);
        setStartTransform(start);
        setEndTransform(end);
        setGradients(grads);
    }

    /**
     * Creates and initialises a MorphGradientFill fill style using the values
     * copied from another MorphGradientFill object.
     *
     * @param object
     *            a MorphGradientFill fill style from which the values will be
     *            copied.
     */
    public MorphGradientFill(final MorphGradientFill object) {
        type = object.type;
        startTransform = object.startTransform;
        endTransform = object.endTransform;

        gradients = new ArrayList<MorphGradient>(object.gradients.size());

        for (final MorphGradient gradient : object.gradients) {
            gradients.add(gradient.copy());
        }
    }

    /**
     * Add a MorphGradient object to the array of gradient objects.
     *
     * @param aGradient
     *            an MorphGradient object. Must not be null.
     * @return this object.
     */
    public MorphGradientFill add(final MorphGradient aGradient) {
        if (aGradient == null) {
            throw new IllegalArgumentException();
        }
        if (gradients.size() == SWF.MAX_GRADIENTS) {
            throw new IllegalStateException(
                    "Maximum number of gradients exceeded.");
        }
        gradients.add(aGradient);
        return this;
    }

    public GradientType getType() {
        GradientType value;
        if (type == FillStyleTypes.LINEAR_GRADIENT) {
            value = GradientType.LINEAR;
        } else {
            value = GradientType.RADIAL;
        }
        return value;
    }

    public void setType(final GradientType gradientType) {
        if (gradientType == GradientType.LINEAR) {
            type = FillStyleTypes.LINEAR_GRADIENT;
        } else {
            type = FillStyleTypes.RADIAL_GRADIENT;
        }
    }

    /**
     * Get the coordinate transform mapping the gradient square onto
     * physical coordinates at the start of the morphing process.
     *
     * @return the starting transform for the gradient.
     */
    public CoordTransform getStartTransform() {
        return startTransform;
    }

    /**
     * Get the coordinate transform mapping the gradient square onto
     * physical coordinates at the end of the morphing process.
     *
     * @return the final transform for the gradient.
     */
    public CoordTransform getEndTransform() {
        return endTransform;
    }

    /**
     * Get the array of MorphGradients defining the control points for the
     * gradient.
     *
     * @return the array of points that define the gradient.
     */
    public List<MorphGradient> getGradients() {
        return gradients;
    }

    /**
     * Sets the coordinate transform mapping the gradient square onto physical
     * coordinates at the start of the morphing process.
     *
     * @param aTransform
     *            the starting coordinate transform. Must not be null.
     */
    public void setStartTransform(final CoordTransform aTransform) {
        if (aTransform == null) {
            throw new IllegalArgumentException();
        }
        startTransform = aTransform;
    }

    /**
     * Sets the coordinate transform mapping the gradient square onto physical
     * coordinates at the end of the morphing process.
     *
     * @param aTransform
     *            the ending coordinate transform. Must not be null.
     */
    public void setEndTransform(final CoordTransform aTransform) {
        if (aTransform == null) {
            throw new IllegalArgumentException();
        }
        endTransform = aTransform;
    }

    /**
     * Sets the array of control points that define the gradient. The final
     * array should contain at least two control points. Up to Flash 7 the array
     * can contain up to 8 control points. For Flash 8 onwards this limit was
     * increased to 15.
     *
     * @param anArray
     *            an array of MorphGradient objects. Must not be null.
     */
    public void setGradients(final List<MorphGradient> anArray) {
        if (anArray == null) {
            throw new IllegalArgumentException();
        }
        if (anArray.size() > SWF.MAX_GRADIENTS) {
            throw new IllegalStateException(
                    "Maximum number of gradients exceeded.");
        }
        gradients = anArray;
    }

    /** {@inheritDoc} */
    public MorphGradientFill copy() {
        return new MorphGradientFill(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, startTransform, endTransform, gradients);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        count = gradients.size();
        return 2 + startTransform.prepareToEncode(context)
                + endTransform.prepareToEncode(context) + (count * 10);
        // CHECKSTYLE:ON
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(type);
        startTransform.encode(coder, context);
        endTransform.encode(coder, context);
        coder.writeByte(count);

        for (final MorphGradient gradient : gradients) {
            gradient.encode(coder, context);
        }
    }
}
