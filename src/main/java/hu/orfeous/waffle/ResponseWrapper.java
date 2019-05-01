/*
 * The MIT License
 *
 * Copyright 2019 Gabor Racz (https://github.com/Orfeous).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hu.orfeous.waffle;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 * @author Gabor Racz (Orfeous)
 * @github https://github.com/Orfeous
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

    private final CharArrayWriter cWriter;
    private PrintWriter pWriter;
    private boolean getOutputStreamCalled;
    private boolean getWriterCalled;

    protected ResponseWrapper(HttpServletResponse response) {
        super(response);
        cWriter = new CharArrayWriter();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (getWriterCalled) {
            throw new IllegalStateException("getWriter already called");
        }

        getOutputStreamCalled = true;
        return super.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (pWriter != null) {
            return pWriter;
        }
        if (getOutputStreamCalled) {
            throw new IllegalStateException("getOutputStream already called");
        }
        getWriterCalled = true;
        pWriter = new PrintWriter(cWriter);
        return pWriter;
    }

    @Override
    public void setStatus(int sc) {
        super.setStatus(sc);
    }

    @Override
    public void setLocale(Locale loc) {
        super.setLocale(loc);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        super.setCharacterEncoding(charset);
    }

    @Override
    public void setContentType(String type) {
        super.setContentType(type);
    }

    @Override
    public String toString() {
        String tempString = null;
        if (pWriter != null) {
            tempString = cWriter.toString();
        }
        return tempString;
    }
}
