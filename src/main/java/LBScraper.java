import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A local business aggregator page scraper
 */
public class LBScraper {
    private String siteUrl;
    private String searchUrl;

    public LBScraper(String searchString) {
        this.siteUrl = "http://www.abv.mk/";
        this.searchUrl = "http://www.abv.mk/search.aspx?s=" + searchString;
    }

    public void parse(int pageNum) {

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.waitForBackgroundJavaScript(3000);

        try {
            // Use proxy for page parsing
            Page proxyPage = webClient.getPage("http://proxy.minjja.lt/api/?type=http");
            WebResponse response = proxyPage.getWebResponse();
            if (response.getContentType().equals("application/json")) {
                String jsonProxy = response.getContentAsString();
                Map<String, String> jsonMap = new Gson().fromJson(jsonProxy, new TypeToken<Map<String,String>>() {}.getType());
                ProxyConfig proxyConfig = new ProxyConfig(jsonMap.get("ip"), Integer.valueOf(jsonMap.get("port")));
                webClient.getOptions().setProxyConfig(proxyConfig);
            }

            // Change charset for non-latin alphabet pages
            WebRequest webRequest = new WebRequest(new URL(this.searchUrl));
            webRequest.setCharset("utf-8");
            HtmlPage htmlPage = webClient.getPage(webRequest);

            List<String> profileLinks = new ArrayList<String>();
            while(pageNum > 0) {

                profileLinks.addAll(parseProfileLinks(htmlPage.asXml()));

                HtmlAnchor nxtLink = (HtmlAnchor) htmlPage.getByXPath("//*[@id='ctl00_ContentPlaceHolder1_DataPager1']/div/ul/li[@class='selected']/following-sibling::li/a").get(0);
                htmlPage = nxtLink.click();
                int count = 3;
                while (count > 0) {
                    // Wait for javascript to catch up.
                    count = webClient.waitForBackgroundJavaScript(3000);
                }
            }

            List<SubscriberBean> subscribers = new ArrayList<SubscriberBean>();
            for (String link : profileLinks) {

                subscribers.add(parseSubscriber(link));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> parseProfileLinks(String page) {
        Document listPage = Jsoup.parse(page);
        Elements profileLinks = listPage.select("div.result h2.fontot a.companyname");

        List<String> profilesList = new ArrayList<String>();
        for (Element pageEl : profileLinks) {
            String pageLink = this.siteUrl + pageEl.attr("href");
            profilesList.add(pageLink);
        }

        return profilesList;
    }

    private SubscriberBean parseSubscriber(String profileLink) {

        try {
            Document doc = Jsoup.connect(profileLink)
                    .userAgent("Mozilla")
                    .get();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
