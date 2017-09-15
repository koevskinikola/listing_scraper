import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.URL;

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

    public void parse() {

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.waitForBackgroundJavaScript(3000);

        try {
            WebRequest webRequest = new WebRequest(new URL(this.searchUrl));
            webRequest.setCharset("utf-8");
            HtmlPage htmlPage = webClient.getPage(webRequest);
            HtmlAnchor nxtLink = (HtmlAnchor)htmlPage.getByXPath("//*[@id='ctl00_ContentPlaceHolder1_DataPager1']/div/ul/li[@class='selected']/following-sibling::li/a").get(0);
            htmlPage = nxtLink.click();

            int count = 3;
            while(count > 0) {
                // Wait for javascript to catch up.
                count = webClient.waitForBackgroundJavaScript(3000);
            }
            System.out.println(htmlPage.asXml());
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            Document doc = Jsoup.connect(this.searchUrl)
//                    .userAgent("Mozilla")
//                    .get();
//            System.out.println(doc.toString());
//            Elements businessLinks = doc.select("div.result h2.fontot a.companyname");
//            Element viewstate = doc.select("input#__VIEWSTATE").first();
//            Element viewstateGenerator = doc.select("input#__VIEWSTATEGENERATOR").first();
//            Element eventValidation = doc.select("input#__EVENTVALIDATION").first();
//
//            postData.put("__VIEWSTATE", viewstate.attr("value"));
//            postData.put("__VIEWSTATEGENERATOR", viewstateGenerator.attr("value"));
//            postData.put("__EVENTVALIDATION", eventValidation.attr("value"));
//
//
//            for (Element pageEl : businessLinks) {
//                String pageLink = this.siteUrl + pageEl.attr("href");
//                System.out.println(pageLink);
//                Document pDoc = Jsoup.connect(pageLink)
//                        .userAgent("Mozilla")
//                        .get();
//            }
//
//            System.out.println("PAGE 2");
//
//            Connection.Response response = Jsoup.connect(this.searchUrl)
//                    .userAgent("Mozilla/5.0")
//                    .method(Connection.Method.POST)
//                    .data(postData)
//                    .followRedirects(true)
//                    .execute();
//
//            Document doc2 = response.parse();
//            Elements businessLinks2 = doc2.select("div.result h2.fontot a.companyname");
//            for (Element pageEl : businessLinks2) {
//                String pageLink = this.siteUrl + pageEl.attr("href");
//                System.out.println(pageLink);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

}
