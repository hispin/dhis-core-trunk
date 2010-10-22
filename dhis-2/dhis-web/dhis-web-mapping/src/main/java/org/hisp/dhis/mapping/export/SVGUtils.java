package org.hisp.dhis.mapping.export;

/*
 * Copyright (c) 2004-2010, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

/**
 * @author Tran Thanh Tri
 */
public class SVGUtils
{
    public static void convertToPNG( StringBuffer buffer, OutputStream out, Integer width, Integer height )
        throws TranscoderException, IOException
    {
        if ( width == null || width < 10 )
        {
            width = 500;
        }

        if ( height == null || height < 10 )
        {
            height = 500;
        }

        PNGTranscoder t = new PNGTranscoder();

        t.addTranscodingHint( PNGTranscoder.KEY_HEIGHT, new Float( height ) );
        t.addTranscodingHint( PNGTranscoder.KEY_WIDTH, new Float( width ) );
        t.addTranscodingHint( JPEGTranscoder.KEY_BACKGROUND_COLOR, Color.WHITE );

        TranscoderInput input = new TranscoderInput( new StringReader( buffer.toString() ) );

        TranscoderOutput output = new TranscoderOutput( out );

        t.transcode( input, output );
    }

    public static void convertToJPEG( StringBuffer buffer, OutputStream out, Integer width, Integer height )
        throws TranscoderException, IOException
    {
        if ( width == null || width < 10 )
        {
            width = 500;
        }

        if ( height == null || height < 10 )
        {
            height = 500;
        }

        JPEGTranscoder t = new JPEGTranscoder();

        t.addTranscodingHint( JPEGTranscoder.KEY_HEIGHT, new Float( height ) );
        t.addTranscodingHint( JPEGTranscoder.KEY_WIDTH, new Float( width ) );
        t.addTranscodingHint( JPEGTranscoder.KEY_BACKGROUND_COLOR, Color.WHITE );

        TranscoderInput input = new TranscoderInput( new StringReader( buffer.toString() ) );

        TranscoderOutput output = new TranscoderOutput( out );

        t.transcode( input, output );
    }
}
