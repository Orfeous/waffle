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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 * @author Gabor Racz (Orfeous)
 * @github https://github.com/Orfeous
 */
public class RequestWrapper extends HttpServletRequestWrapper {

    private final String newRequestBody;

    private BufferedReader modifiedReader;

    private boolean getInputStreamCalled;

    private boolean getReaderCalled;

    protected RequestWrapper(HttpServletRequest request, Map<String, String> keyStore) throws ServletException {
        super(request);
        Map<String, String[]> paramMap = request.getParameterMap();
        if (paramMap == null) {
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader = request.getReader();
                if (reader != null) {
                    String s;
                    while ((s = reader.readLine()) != null) {
                        sb.append(s);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(RequestWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
            paramMap = getQueryMap(sb.toString());
        }
        if (!paramMap.isEmpty()) {
            StringBuilder newSb = new StringBuilder();
            for (String s : paramMap.keySet()) {
                for (String param : paramMap.get(s)) {
                    if (keyStore.containsKey(s)) {
                        String randomStr = keyStore.get(s);
                        newSb.append(randomStr);
                    } else {
                        newSb.append(s);
                    }
                    newSb.append("=").append(param).append("&");
                }
            }
            newRequestBody = newSb.toString();
        } else {
            newRequestBody = "";
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (getReaderCalled) {
            throw new IllegalStateException("getReader already called");
        }

        getInputStreamCalled = true;
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.newRequestBody.getBytes());

        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener rl) {
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (modifiedReader != null) {
            return modifiedReader;
        }
        if (getInputStreamCalled) {
            throw new IllegalStateException("getInputStreamCalled already called");
        }
        getReaderCalled = true;
        modifiedReader = new BufferedReader(new InputStreamReader(this.getInputStream(), this.getRequest().getCharacterEncoding()));
        return modifiedReader;
    }

    @Override
    public String getQueryString() {
        return this.newRequestBody;
    }

    @Override
    public String getParameter(String name) {
        Map<String, String[]> paramMap = getQueryMap(this.newRequestBody);
        String value = null;
        if (paramMap.containsKey(name)) {
            value = paramMap.get(name)[0];
        }
        return value;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Map<String, String[]> paramMap = getQueryMap(this.newRequestBody);
        return Collections.enumeration(paramMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        Map<String, String[]> paramMap = getQueryMap(this.newRequestBody);
        String[] values = paramMap.get(name);
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return getQueryMap(this.newRequestBody);
    }

    protected Map<String, String[]> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String[]> map = new HashMap<>();
        if (params.length > 0) {
            for (String param : params) {
                if (param.split("=").length > 1) {
                    String name = param.split("=")[0];
                    String value = param.split("=")[1];
                    try {
                        name = URLDecoder.decode(name, "UTF-8");
                        value = URLDecoder.decode(value, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(RequestWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (map.containsKey(name)) {
                        List<String> values = Arrays.asList(map.get(name));
                        values.add(value);
                        map.remove(name);
                        map.put(name, (String[]) values.toArray());
                    } else {
                        String[] values = new String[1];
                        values[0] = value;
                        map.put(name, values);
                    }
                }
            }
        }
        return map;
    }
}
