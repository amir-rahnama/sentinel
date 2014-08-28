/**
 * Created with IntelliJ IDEA.
 * User: amir
 * Date: 2014-08-27
 * Time: 11:56
 */
package com.yahoo.labs.samoa.sentinel.processors;

/*
 * #%L
 * SAMOA
 * %%
 * Copyright (C) 2013 - 2014 Yahoo! Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import com.yahoo.labs.samoa.sentinel.model.Tweet;
import com.yahoo.labs.samoa.sentinel.util.LanguageDetector;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PipeProcessor
{
    private String message;

    private static LanguageDetector languageDetector;
    private Tweet tweet;
    private String languageFilter;

    public PipeProcessor() {    }

    public Tweet processTweets(String tweetMessage, String langFilter) {
        tweet = null;
        tweet = new Tweet();
        tweet.setOriginalMessage(tweetMessage);
        message = tweetMessage;
        languageFilter = langFilter;

        cleanWhiteSpace();
        removeEmotionsFromMessage();
        takeOutTwitterSpecificCharacters();
        setFilteredMessage();

        return tweet;
    }

    private void cleanWhiteSpace() {
        message = message.replaceAll("[\\n\\r]", "");
        message = message.replaceAll("[^a-z\\sA-Z!0-9#@()\\[\\]\\{\\}:-=;]", "");
        message = message.replaceAll("\\s{2,}", "");
    }

    private void removeEmotionsFromMessage() {
        boolean result;

        Pattern pHappy = Pattern.compile("=\\)|=D|:\\)|=P|:P|:\\]|=\\]|;\\)|:\\}|:p|=p|: P|:D|=d");
        Pattern pSad = Pattern.compile("=\\(|:-\\(|:\\(|:\\{|:\\[|=\\[|D:|=\\{");

        StringBuffer sb = new StringBuffer();

        Matcher m = pHappy.matcher(message);
        result = m.find();
        tweet.setEmotionType("N");

        while (result) {
            m.appendReplacement(sb, "");
            tweet.setEmotionType("Happy");
            result = m.find();
        }

        if (tweet.getEmotionType().equals("Happy")) {
            m.appendTail(sb);
        } else {
            m = pSad.matcher(message);
            result = m.find();

            while (result) {
                m.appendReplacement(sb, "");
                tweet.setEmotionType("Sad");
                result = m.find();
            }
            m.appendTail(sb);
        }

        message = sb.toString();
    }

    private void detectLanguage() {
        ArrayList<Language> detect;
        if (message.length() > 0) {
            try {
                if (languageDetector == null) {
                    languageDetector = new LanguageDetector();
                }

                detect = languageDetector.detectLangs(message);
                tweet.setLanguage(detect.get(0).lang);
                tweet.setLanguageProbability(detect.get(0).prob);

            } catch (LangDetectException e) {
                tweet.setLanguage("");
            }
        }
    }

    private void setFilteredMessage () {
        if (languageFilter.equals("")) {
            tweet.setCleanedMessage(message);
        }
        else {
            detectLanguage();
            String temp = message.replaceAll("\\s+", "");
            if (!message.equals("") && !temp.equals(""))
            {
                if (tweet.getLanguageProbability() > tweet.getMinimumProbabilityThreshold())
                {
                    tweet.setCleanedMessage(message);
                }
            }
        }
    }

    private void takeOutTwitterSpecificCharacters() {
        message = message.replaceAll("[^a-z\\sA-Z#@]", "");

        Pattern p = Pattern.compile("\\s@.+?\\s|^@.+?\\s|\\s#.+?\\s|^#.+?\\s");

        StringBuffer sb = new StringBuffer();

        Matcher match = p.matcher(message);
        boolean result = match.find();

        while (result) {
            match.appendReplacement(sb, " ");
            result = match.find();
        }

        match.appendTail(sb);
        message = sb.toString();
    }
}
