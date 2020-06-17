package com.isuru.crawler;



import com.isuru.bean.Comment;
import com.isuru.bean.NewsArticle;
import org.apache.poi.hslf.record.CString;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CommentExtractor {
    private static final Logger logger = Logger.getLogger("CommentExtractor");

    public static NewsArticle extractNews(String title, String html, String url) {
//        NewsArticle newsArticle = new NewsArticle();
//        List<Comment> commentList = new ArrayList<>();
//        StringBuilder body = new StringBuilder();
//        String articleAuthor;
//        String articleDate;


        Document doc = Jsoup.parse(html.replaceAll("\\&quot;",""));


        NewsArticle article = new NewsArticle();
        List<Comment> commentList = new ArrayList<>();



        Elements comments = doc.getElementsByClass("\\idc-c-t-inner\\");
        for (Element e:comments){
            String author = "DummyAuthor";
            String date = "DummyDate";
            String commentPhrase = e.text();
            Comment comment = new Comment(author, date, commentPhrase);
            commentList.add(comment);
        }

        article.setComments(commentList);
        article.setTitle(title);
        article.setUrl(url);
        article.setAuthor("articleAuthor");
        article.setDate("articleDate");
        article.setBody("dody");

        return article;



    }

    public static void saveXML(String fileName, NewsArticle article) {
        File output = new File("corpus/GossipLanka/raw_data_4/" + fileName + ".xml");
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(NewsArticle.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(article, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveTxt(String fileName, NewsArticle article) {
        File output = new File("corpus/GossipLanka/raw_data_4/" + fileName + ".txt");
        try {
            output.createNewFile();
            FileWriter writer = new FileWriter(output);
            writer.write(article.toTxt());
            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    public static void saveTxttest(String fileName, String article) {
//        File output = new File("corpus/GossipLanka/raw_data/" + fileName + ".txt");
//        File outputComments = new File("./test/out/txt/comments/" + fileName + ".txt");
//        try {
//            output.createNewFile();
//            FileWriter writer = new FileWriter(output);
//            writer.write(article);
//            writer.flush();
//            writer.close();
////
////            output.createNewFile();
////            writer = new FileWriter(outputComments);
////            writer.write(CString);
////            writer.flush();
////            writer.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }


    public static void saveJson(String fileName, NewsArticle article) {
        File output = new File("./test/out/json/" + fileName + ".json");
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void saveAnn(String fileName, NewsArticle article) {
        File output = new File("./test/out/ann/" + fileName + ".ann");
        try {
            output.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        (new CommentExtractor()).testJsoup();
    }

    public void testJsoup() {
        File input = new File("./test/test1.html");
        File output = new File("./test/test1.xml");

        NewsArticle article = new NewsArticle();
        try {
            Document doc = Jsoup.parse(input, "UTF-8");

            Element title = doc.select("title").first();
            article.setTitle(title.text());
            logger.info(title.text());

            Element articleMeta = doc
                    .select("[style=\"font-size:12px; color:#c28282; padding:5px 5px; text-align:left;\"]").first();
            String[] tempSplit = articleMeta.text().split("\\|");
            article.setDate(tempSplit[0]);
            article.setAuthor(tempSplit[1]);
            logger.info(articleMeta.text());
            logger.info(tempSplit[0] + " " + tempSplit[1].replaceAll("[.,]", "").trim());

            Elements bodyContent = doc.getElementsByClass("entry-content").first().select("p");
            article.setBody("");
            for (Element e : bodyContent) {
                article.setBody(article.getBody() + e.text());
                logger.info(e.text());
            }

            Elements comments = doc
                    .select("[style=\"border: 1px #f8f8f8 solid; float: left; margin: 5px 0px; padding: 5px; "
                            + "color: #000000; width: 640px; height: auto; background: #f9f9f9;\"]");
            for (Element e : comments) {
                Comment comment = new Comment();
                comment.setPhrase(e.select("span").text());
                comment.setAuthor(e.select("p").first().text().substring(0, e.select("p").first().text().length() - 19).trim());
                article.addComment(comment);

                logger.info(e.select("p").text().substring(0, e.select("p").text().length() - 19).trim());
                logger.info(e.select("span").text());
            }

            JAXBContext jaxbContext = JAXBContext.newInstance(NewsArticle.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(article, output);
            jaxbMarshaller.marshal(article, System.out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
