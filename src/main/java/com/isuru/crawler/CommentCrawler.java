package com.isuru.crawler;

import com.isuru.bean.NewsArticle;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.json.simple.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class CommentCrawler extends WebCrawler {
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp3|zip|gz))$");

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.97 Safari/537.36";


    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.startsWith("https://www.gossiplankanews.com/");
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        logger.info("URL: " + url);
        String[] urlParts = url.split("/");
        String fileName = urlParts[urlParts.length - 1];
        int numberOfComments = 0;


        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Document doc = Jsoup.parse(html);
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            String title = Jsoup.parse(doc.getElementsByClass("post").html()).getElementsByClass("title").text();
            String text2 = doc.getElementById("Blog1").childNode(40).childNode(0).attr("Data");

//            ScriptEngineManager mgr = new ScriptEngineManager();
////            ScriptEngine se = mgr.getEngineByName("JavaScript");
////
////            try {
////                se.eval(text2);
////            } catch (ScriptException e) {
////                e.printStackTrace();
////            }

            List<String> parameters = new ArrayList<String>();

            Pattern p = Pattern.compile("^\\s*var\\s+(.*?)\\s*;?$");
            Matcher m = p.matcher(text2);
            if (m.find()) {
                parameters.addAll(Arrays.asList(m.group(1).split("\\s*;\\s*")));
            }


            String idcomments_acct = parameters.get(0).replaceFirst(".*=","").replaceAll("\'","");
            String idcomments_post_id= parameters.get(1).replaceFirst(".*=","").replaceAll("\'","");
            Document comment_doc = null;


            try {

                String theurl = "https://www.intensedebate.com/js/bloggerTemplateCommentWrapper2.php?acct="+idcomments_acct+"&postid="+idcomments_post_id;
                logger.info("Loading " + theurl);

                Connection.Response resp = Jsoup.connect(theurl)
                        .timeout(5000)
                        .referrer("")
                        .userAgent(USER_AGENT)
                        .method(Connection.Method.GET)
                        .execute();
                comment_doc = resp.parse();
            }
            catch (IOException e){
                logger.info(e.getMessage());

            }

            String comment_doc_html = comment_doc.html();
            Document comment_doc_parsed = Jsoup.parse(comment_doc_html);

            try {
                numberOfComments = Integer.parseInt(comment_doc_parsed.getElementById("\\'idc-commentcount\\'").text());
            }
            catch (Exception e){
                logger.info(e.toString());
            }




            logger.info("Text length: " + text.length());
            logger.info("Html length: " + html.length());
            logger.info("Number of outgoing links: " + links.size());


            int beginingIndex = 0;
//            int noOfComments = 0;
//            if (html.contains("අදහස් (")) {
//                beginingIndex = html.indexOf("අදහස් (");
//                noOfComments = Integer.parseInt(html.substring(beginingIndex + 7, beginingIndex + 8));
//            }

            if (numberOfComments > 0) {
                NewsArticle article = CommentExtractor.extractNews(title, comment_doc_html, url);
//                CommentExtractor.saveTxttest("testfile","test_content");
                CommentExtractor.saveXML(fileName, article);
////            	 CommentExtractor.saveJson(fileName, article);
//                CommentExtractor.saveTxt(fileName, article);
//                CommentExtractor.saveAnn(fileName, article);

                logger.info(article.toString());
            }
        }
    }
}
