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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Gabor Racz (Orfeous)
 * @github https://github.com/Orfeous
 */
public class ResponseFilter implements Filter {

    protected FilterConfig config;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc) throws IOException, ServletException {
        Map<String, String> keyStore;

        try {
            if (req instanceof HttpServletRequest) {
                HttpSession st = ((HttpServletRequest) req).getSession();
                keyStore = (Map<String, String>) st.getAttribute("keyStore");
                if (keyStore == null) {
                    keyStore = new HashMap<>();
                }
                ServletRequest newRequest = new RequestWrapper((HttpServletRequest) req, keyStore);
                ServletResponse newResponse = new ResponseWrapper((HttpServletResponse) resp);

                fc.doFilter(newRequest, newResponse);

                String html = newResponse.toString();

                if (html != null) {
                    Document doc = Jsoup.parseBodyFragment(html);
                    randomize(doc, "input[name]", "name", keyStore, true);
                    randomize(doc, "input[id]", "id", keyStore, false);
                    randomize(doc, "form[id]", "id", keyStore, false);
                    resp.getWriter().write(doc.html());
                }
                st.setAttribute("keyStore", keyStore);
            }
        } catch (ServletException se) {
            if (resp instanceof HttpServletResponse) {
                String str = "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n"
                        + "<html><head>\n"
                        + "<title>403 - Forbidden</title>\n"
                        + "</head><body>\n"
                        + "<h1>Forbidden</h1>\n"
                        + "<hr>\n"
                        + "<address>Protected by <a href=https://github.com/Orfeous/waffle>WAFfle by Orfeous</a></address>\n"
                        + "</body></html>";
                resp.getWriter().write(str);
                ((HttpServletResponse) resp).setStatus(403);
            } else {
                Logger.getLogger(ResponseFilter.class.getName()).log(Level.SEVERE, null, se);
            }
        }
    }

    @Override
    public void destroy() {
        this.config = null;
    }

    private void randomize(Document doc, String selector, String attribute, Map<String, String> keyStore,
            boolean saveInStore) {
        Elements names = doc.select(selector);
        names.forEach((ele) -> {
            String name = ele.attr(attribute);
            if (keyStore.containsKey(name)) {
                String origName = keyStore.get(name);
                keyStore.remove(name);
                name = origName;
            }
            String s = UUID.randomUUID().toString();
            ele.attr(attribute, s);
            if (saveInStore) {
                keyStore.put(s, name);
            }
        });
    }
}
