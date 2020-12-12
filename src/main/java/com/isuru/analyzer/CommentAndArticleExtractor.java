package com.isuru.analyzer;

import com.isuru.bean.Comment;
import com.isuru.bean.NewsArticle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Extract comments from NewsArticle.
 */
public class CommentAndArticleExtractor {

    private static final Logger logger = Logger.getLogger("Aggregator");

    private static final String SEPARATOR = ",";
    private static final String NEW_LINE = "\n";

    private  static String inputPath = "./corpus/raw_data/lankadeepa";
    private  static String savePath = "./corpus/analyzed/lankadeepa/with_article/lankadeepa_comments_with_article_2.csv";

    private StringBuilder aggregateComments = new StringBuilder().append("docid,article,comment\n");

    public static void main(String[] args) {
        File folder = new File(inputPath);
        File[] listOfFiles = folder.listFiles();
        CommentAndArticleExtractor extractor = new CommentAndArticleExtractor();
        long totalArticlesExtracted = 0;

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                logger.info("File " + listOfFiles[i].getName());
                extractor.aggregateWithFileName(listOfFiles[i]);
            } else if (listOfFiles[i].isDirectory()) {
                logger.info("Directory " + listOfFiles[i].getName());
            }
        }
        logger.info("Finished processing, writing to file" +savePath + " , #artices = " + totalArticlesExtracted);
        extractor.writeToFiles(savePath);
    }

    private void aggregateWithFileName(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(NewsArticle.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            NewsArticle newsArticle = (NewsArticle) jaxbUnmarshaller.unmarshal(file);
            String fileName = file.getName().split(".xml")[0];
            String article_body = newsArticle.getBody();
            article_body = filter_text(article_body);

            aggregateComments
                    .append(newsArticle.getArticleId()).append(SEPARATOR)
                    .append(article_body).append(SEPARATOR);

            for (Comment comment : newsArticle.getComments()) {
                String comment_text = filter_text(comment.getPhrase());
                aggregateComments
                            .append(comment_text)
                            .append(';');
            }
            aggregateComments.append(NEW_LINE);



        } catch (JAXBException e) {
            e.printStackTrace();
        }
        logger.info("Processed file " + file.getName() );
    }


    private void writeToFiles(String savepath) {
        try (FileWriter fileWriter = new FileWriter(savepath)) {
            fileWriter.write(aggregateComments.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String filter_text(String text){
        text = text.replace(SEPARATOR, ";")
                .replace(NEW_LINE, ". ")
                .replace(",", " ")
                .replace(":", " ")
                .replace("-", " ")
                .replace("!", " ")
                .replace(";", " ")
                .replace("[", " ")
                .replace("]", " ")
                .replace("(", " ")
                .replace(")", " ")
                .replace("\\", " ")
                .replace("/", " ")
                .replace("\"", " ")
                .replace("'", " ")
                .replace("?", " ")
                .replaceAll("(\\h+)"," ")
                .replaceAll("\\s+", " ")
                .replaceAll("[a-zA-Z]", " ")
                .trim();
        return text;
    }
}
