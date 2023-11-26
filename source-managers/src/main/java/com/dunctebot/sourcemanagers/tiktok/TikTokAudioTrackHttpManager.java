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

package com.dunctebot.sourcemanagers.tiktok;

import com.sedmelluq.discord.lavaplayer.tools.http.HttpContextFilter;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.stream.Collectors;

import static com.dunctebot.sourcemanagers.Utils.fakeChrome;

public class TikTokAudioTrackHttpManager implements AutoCloseable {
//    private String cookie = null;

    protected final HttpInterfaceManager httpInterfaceManager;
//    private final CookieSpec cookieSpec = new BrowserCompatSpec(); // DefaultCookieSpec does not parse (was BrowserCompatSpec)
    private final CookieStore cookieStore = new BasicCookieStore();

    public TikTokAudioTrackHttpManager() {
        httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();

        httpInterfaceManager.configureBuilder((builder) -> {
            builder.setDefaultCookieStore(cookieStore);
        });

        httpInterfaceManager.setHttpContextFilter(new TikTokFilter());
    }

    public HttpInterface getHttpInterface() {
        return httpInterfaceManager.getInterface();
    }

    /*protected void loadCookies() throws IOException, MalformedCookieException {
        try (final HttpInterface httpInterface = httpInterfaceManager.getInterface()) {
            final HttpGet httpGet = new HttpGet("https://www.tiktok.com/");

            try (final CloseableHttpResponse response = httpInterface.execute(httpGet)) {
                final CookieOrigin origin = new CookieOrigin(".tiktok.com", 443, "/", true);

                final List<Cookie> cookies = new ArrayList<>();

                for (final Header header : response.getHeaders("Set-Cookie")) {
                    cookies.addAll(cookieSpec.parse(header, origin));
                }

                this.cookie = cookies.stream()
                    .map((c) -> c.getName() + '=' + c.getValue())
                    .collect(Collectors.joining("; "));
            }
        }
    }*/

    @Override
    public void close() throws Exception {
        this.httpInterfaceManager.close();
    }

    private class TikTokFilter implements HttpContextFilter {
        @Override
        public void onContextOpen(HttpClientContext context) {
            context.setCookieStore(cookieStore);
        }

        @Override
        public void onContextClose(HttpClientContext context) {
            // Not used
        }

        @Override
        public void onRequest(HttpClientContext context, HttpUriRequest request, boolean isRepetition) {
            // set standard headers
            final boolean isVideo = request.getURI().getPath().contains("video");
            fakeChrome(request, isVideo);

            final String testCookie = context.getCookieStore()
                .getCookies()
                .stream()
                .map((c) -> c.getName() + '=' + c.getValue())
                .collect(Collectors.joining("; "));

            request.setHeader("Cookie", testCookie);
            request.setHeader("Referer", "https://www.tiktok.com/");
        }

        @Override
        public boolean onRequestResponse(HttpClientContext context, HttpUriRequest request, HttpResponse response) {
            return false;
        }

        @Override
        public boolean onRequestException(HttpClientContext context, HttpUriRequest request, Throwable error) {
            return false;
        }
    }
}
