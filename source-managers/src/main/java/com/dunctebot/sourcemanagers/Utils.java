/*
 * Copyright 2021 Duncan "duncte123" Sterken
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dunctebot.sourcemanagers;

import org.apache.http.HttpRequest;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class Utils {
    public static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/114.0";

    public static String urlDecode(String in) {
        return URLDecoder.decode(in, Charset.defaultCharset());
    }

    public static String urlEncode(String in) {
        return URLEncoder.encode(in, Charset.defaultCharset());
    }

    public static String decryptXor(String input, String key) {
        final StringBuilder sb = new StringBuilder();

        while (key.length() < input.length()) {
            key += key;
        }

        for (int i = 0; i < input.length(); i += 1) {
            final int value1 = input.charAt(i);
            final int value2 = key.charAt(i);

            final int xorValue = value1 ^ value2;

            sb.append((char) xorValue);
        }

        return sb.toString();
    }

    public static boolean isURL(String url) {
        return url.matches("^https?:\\/\\/[-a-zA-Z0-9+&@#\\/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#\\/%=~_|]");
    }

    public static void fakeChrome(HttpRequest request) {
        fakeChrome(request, false);
    }

    public static void fakeChrome(HttpRequest request, boolean isVideo) {
        request.setHeader("Connection", "keep-alive");
        request.setHeader("DNT", "1");
        request.setHeader("Upgrade-Insecure-Requests", "1");
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,video/mp4,image/avif,image/webp,*/*;q=0.8");
//        request.setHeader("Accept-Encoding", "gzip, deflate, br");
        request.setHeader("Accept-Encoding", "none");
        request.setHeader("TE", "trailers");
        request.setHeader("Accept-Language", "en-US,en;q=0.9");

        if (isVideo) {
            request.setHeader("Sec-Fetch-Dest", "empty");
        } else {
            request.setHeader("Sec-Fetch-Dest", "document");
        }

        request.setHeader("Sec-Fetch-Mode", "cors");
        request.setHeader("Sec-Fetch-Site", "same-site");
        request.setHeader("User-Agent", USER_AGENT);
    }

}
