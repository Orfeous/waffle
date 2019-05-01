# Waffle
Java - Web Application Firewall

## Overview
A super simple solution to protect your java web app.

### hu.orfeous.waffle.ResponseFilter
This filter automaticly regenerates all input and form elements with random values to prevent autoscraping.

### hu.orfeous.waffle.XSSFilter
This filter prevents [XSS](https://en.wikipedia.org/wiki/Cross-site_scripting) by filtering out harmful parts from the request.

### Usage
```
    <filter>
        <filter-name>WAFfle</filter-name>
        <filter-class>hu.orfeous.waffle.ResponseFilter</filter-class>
    </filter>
    <filter>
        <filter-name>WAFfleXSS</filter-name>
        <filter-class>hu.orfeous.waffle.XSSFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>WAFfle</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    <filter-mapping>
        <filter-name>WAFfleXSS</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```
