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

    private static final String SEPARATOR = ",";
    private static final String NEW_LINE = "\n";
//    private StringBuilder aggregateComments = new StringBuilder().append("docid,comment,label\n");
private StringBuilder aggregateComments = new StringBuilder().append("docid,comment\n");
    private  int comment_number = 1;
    private static String filePath_GossipLanka = "./corpus/GossipLanka/raw_data_4";
    private static String filePath_Lankadeepa = "./corpus/tagged";
    private static String filePath_Lankadeepa_all = "./corpus/raw_data";
    private static boolean binary_sentiment = true;
    private static boolean advancedFiltering = false;
    private static boolean extractWithoutSentiment = true;
    private static String savePath = "./corpus/analyzed/lankadeepa_all_2.csv";


    public static void main(String[] args) {
        File folder = new File(filePath_Lankadeepa_all);
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
        logger.info("Finished processing, writing to file " + savePath + " , #comments = " + totalCommentsAdded);
        extractor.writeToFiles();
    }

    private int aggregateWithFileName(File file) {
        int usefulComments = 0;
        int uselessComments = 0;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(NewsArticle.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            NewsArticle newsArticle = (NewsArticle) jaxbUnmarshaller.unmarshal(file);
            long articleId = newsArticle.getArticleId();
//            String fileName = file.getName().split(".txt")[0];

            for (Comment comment : newsArticle.getComments()) {
                if (comment.getSentiment().equals(Sentiment.POSITIVE) ||
                        comment.getSentiment().equals(Sentiment.NEGATIVE) || extractWithoutSentiment) {
                    boolean shouldExtract = true;
                    String[] words = comment.getPhrase().trim().split(" ");
                    Sentiment sentiment = comment.getSentiment();
                    int sentimentValue = 10;

                    if(!extractWithoutSentiment){
                        if (binary_sentiment) {
                            switch (sentiment.toString()) {
                                case "NEGATIVE":
                                    sentimentValue = 0;
                                    break;
                                default:
                                    sentimentValue = 1;
                            }

                        } else {
                            switch (sentiment.toString()) {
                                case "NEGATIVE":
                                    sentimentValue = 0;
                                    break;
                                case "NEUTRAL":
                                    sentimentValue = 3;
                                    break;
                                case "POSITIVE":
                                    sentimentValue = 1;
                                    break;
                                case "CONFLICT":
                                    sentimentValue = 5;
                                    break;
                                default:
                                    sentimentValue = 10;
                            }
                        }
                    }


                    if (advancedFiltering){
                        if (words.length <= 10) {
                            shouldExtract = false;
                        }

                        for (String word : words) {
                            if (word.matches("[a-zA-Z]+\\.?")) {
                                uselessComments += 1;
                                logger.info("this comment contains English words.");
                                shouldExtract = false;
                                break;
                            }
                        }
                    }


                    if (shouldExtract) {
                        usefulComments += 1;
                        String filtered_comment = "";
                        char[] chars = comment.getPhrase().toCharArray();
                        for (char ch : chars) {
                            int n = (int) ch;
                            if ((n >= 3456 && n <= 3583) || n == 63 || n == 32 || (n >= 48 && n <= 57) || n == 46) {
                                // 63 for question mark, 32 for space, 46 for fullstop, 48-57 decimal numbers
                                filtered_comment += ch;
                            }
                        }
                        StringBuilder sentiment_component = new StringBuilder().append("");
                        if(!extractWithoutSentiment){
                            sentiment_component
                                    .append(SEPARATOR)
                                    .append(sentimentValue);
                        }


                        StringBuilder new_comment = new StringBuilder()
                                .append(comment_number)
                                .append(SEPARATOR)
                                .append(filtered_comment.replace("."," "))
                                .append(sentiment_component)
                                .append(NEW_LINE);

                        aggregateComments.append(new_comment.toString());
                        comment_number += 1;

                    }
                }
        }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        logger.info("Processed file " + file.getName() + " : " +
                "with comments " + usefulComments + ", " + uselessComments);
        return usefulComments;
    }

    private void writeToFiles() {
        String commentFile = savePath;

        try (FileWriter fileWriter = new FileWriter(commentFile)) {
            fileWriter.write(aggregateComments.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public static void main(String[] args) {
        String s = "මේවා ආණ්ඩුවේ වැරදි නොව  නිලධාරීන්ගේ නොහොබිනා වැඩ  මෙවන් " +
                "නිලධාරීන් සෑම  කාර්ය්\u200Dයාලයකම ඉන්නවා මොවුන්  අතයටින් මුදල්  දෙනතුරු මේවායේ වැඩ නොකෙරෙයි";

        String s1 = s.replaceAll("  ", " ");

        StringBuilder s2 = new StringBuilder();
        s2.append(s.replace(".", " ")
                .replace(",", " ")
                .replace(":", " ")
                .replace("!", " ")
                .replace(";", " ")
                .replace("\"", " ")
                .replace("'", " ")
                .replaceAll("  ", " ")
                .replaceAll("  ", " ")
                .replaceAll("\\s+", " "));

        String s3 = s.replace("\u00A0", " ");

        String s4 = s.replaceAll("(\\h+)"," ");

        System.out.println(s);
        System.out.println(s1);
        System.out.println(s2.toString());
        System.out.println(s3);
        System.out.println(s4);
    }*/
}
