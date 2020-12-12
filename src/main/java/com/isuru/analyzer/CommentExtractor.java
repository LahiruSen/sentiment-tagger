package com.isuru.analyzer;

import com.isuru.bean.Comment;
import com.isuru.bean.NewsArticle;
import com.isuru.bean.Sentiment;

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
public class CommentExtractor {
    private static final Logger logger = Logger.getLogger("Aggregator");

    private static final String inputPath = "./corpus/raw_data/gossip_lanka/raw_data_4";
    private  static String savePath = "./corpus/analyzed/gossip_lanka/gossip_lanka_comments_all_isuru_preprocessing_from_isuru.csv";


    private static final String SEPARATOR = ",";
    private static final String NEW_LINE = "\n";
    private StringBuilder aggregateComments = new StringBuilder().append("docid,comment\n");

    public static void main(String[] args) {
        File folder = new File(inputPath);
        File[] listOfFiles = folder.listFiles();
        CommentExtractor extractor = new CommentExtractor();
        long totalCommentsAdded = 0;

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                logger.info("File " + listOfFiles[i].getName());
                totalCommentsAdded += extractor.aggregateWithFileName(listOfFiles[i]);
            } else if (listOfFiles[i].isDirectory()) {
                logger.info("Directory " + listOfFiles[i].getName());
            }
        }
        logger.info("Finished processing, writing to file, " + savePath + "#comments = " + totalCommentsAdded);
        extractor.writeToFiles(savePath);
    }

    private int aggregateWithFileName(File file) {
        int usefulComments = 0;
        int uselessComments = 0;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(NewsArticle.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            NewsArticle newsArticle = (NewsArticle) jaxbUnmarshaller.unmarshal(file);
            String fileName = file.getName().split(".xml")[0];

            for (Comment comment : newsArticle.getComments()) {

//                Sentiment sentiment = comment.getSentiment();
//                int sentimentValue ;
//                switch(sentiment.toString()) {
//                    case "NEGATIVE":
//                        sentimentValue = 2;
//                        break;
//                    case "NEUTRAL":
//                        sentimentValue = 3;
//                        break;
//                    case "POSITIVE":
//                        sentimentValue = 4;
//                        break;
//                    case "CONFLICT":
//                        sentimentValue = 5;
//                        break;
//                    default:
//                        sentimentValue = 1;
//                }

                usefulComments++;
                String filtered_comment = filter_text(comment.getPhrase());

                // apply this filter only for gossip lanka comments
                if (filtered_comment.trim().split(" ").length > 10) {
                    aggregateComments
                            .append(newsArticle.getArticleId())
                            .append(SEPARATOR)
                            .append(filtered_comment)
//                            .append(SEPARATOR)
//                            .append(sentimentValue)
                            .append(NEW_LINE);
                }
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        logger.info("Processed file " + file.getName() + " : " +
                "with comments " + usefulComments + ", " + uselessComments);
        return usefulComments;
    }

    private void writeToFiles(String savePath) {
        try (FileWriter fileWriter = new FileWriter(savePath)) {
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
                .replaceAll("\\s+", " ") //replace whitespaces
                .replaceAll("[a-zA-Z]", " ")
                .trim();
        return text;
    }

}
