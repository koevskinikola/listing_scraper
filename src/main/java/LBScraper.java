import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
    private String host;
    private int port;

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
//            Page proxyPage = webClient.getPage("http://proxy.minjja.lt/api/?type=http");
//            WebResponse response = proxyPage.getWebResponse();
//            if (response.getContentType().equals("application/json")) {
//                String jsonProxy = response.getContentAsString();
//                Map<String, String> jsonMap = new Gson().fromJson(jsonProxy, new TypeToken<Map<String,String>>() {}.getType());
//
//                System.out.println("Proxy: " + jsonMap.get("ip") + ":" + jsonMap.get("port"));
//                this.host = jsonMap.get("ip");
//                this.port = Integer.valueOf(jsonMap.get("port"));
//                System.setProperty("http.proxyHost", jsonMap.get("ip"));
//                System.setProperty("http.proxyPort", jsonMap.get("port"));
//                ProxyConfig proxyConfig = new ProxyConfig(this.host, this.port);
//                webClient.getOptions().setProxyConfig(proxyConfig);
//            }

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

                pageNum--;
            }

            List<SubscriberBean> subscribers = new ArrayList<SubscriberBean>();
            for (String link : profileLinks) {
                SubscriberBean sb = parseSubscriber(link);

                if(!sb.isEmpty()) {
                    subscribers.add(sb);
                }
            }

            Writer writer = new FileWriter("subscribers.csv");
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            beanToCsv.write(subscribers);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> parseProfileLinks(String page) {
        Document listPage = Jsoup.parse(page);

        Elements profileLinks = listPage.select("div.comline h3 a");

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

            Elements nameElements = doc.select("h1[itemprop='name']");
            Elements emailElements = doc.select("span#ctl00_ContentPlaceHolder1_lblComWeb a[href^='mailto']");

            if (nameElements.isEmpty() || emailElements.isEmpty())
                return new SubscriberBean("", "");
            else
                return new SubscriberBean(emailElements.first().text(), nameElements.first().text());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new SubscriberBean("", "");
    }

}
