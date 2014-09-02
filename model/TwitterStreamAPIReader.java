/**
 * Created with IntelliJ IDEA.
 * User: amir
 * Date: 2014-08-27
 * Time: 11:54
 */
package com.yahoo.labs.samoa.sentinel.model;

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

import com.yahoo.labs.samoa.sentinel.processors.PipeProcessor;
import twitter4j.*;

import java.util.ArrayList;


public class TwitterStreamAPIReader implements TwitterStreamReader
{
    private TwitterStream twitterStream;
    private StatusListener listener;

    private String language = "en";

    int sizeTweetList = 0;
    protected  ArrayList<Tweet> listOfTweets;
    protected PipeProcessor soap;

    public TwitterStreamAPIReader() {
        twitterStream = new TwitterStreamFactory().getInstance();
        soap = new PipeProcessor();
        listOfTweets = new ArrayList<Tweet>();
    }

    @Override
    public void initStream()
    {
        setUpStreamListener();
        // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
        //twitterStream.sample();
    }

    public void filter(String[] query) {
        twitterStream.filter(getFilterQuery(query));
    }

    public FilterQuery getFilterQuery(String[] trackAll) {
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.track(trackAll);

        return filterQuery;
    }

    public TwitterStream getStream() {
        return twitterStream;
    }

    public void add(Status status) {
        if (sizeTweetList < 500) {
            addToList(status);
        } else {
            pop();
        }
    }

    public void addToList(Status status) {
        Tweet tweet = soap.processTweets(status.getText(), language);
        String tweetMessage = tweet.getCleanedMessage();

        //if (tweet.getLanguage().equals(language)) {
        if (tweetMessage != null && !tweetMessage.equals("") && !tweetMessage.equals(" ")) {
            synchronized (listOfTweets) {
                listOfTweets.add(tweet);
                sizeTweetList++;
            }
        }
    }

    public void pop () {
        synchronized (listOfTweets)
        {
            listOfTweets.remove(0);
            sizeTweetList--;
        }
    }

    public int size() {
        synchronized (listOfTweets) {
            return listOfTweets.size();
        }
    }

    @Override
    public void setLanguage(String language)
    {
        this.language = language;
    }

    @Override
    public String getAndRemove(int position)
    {
        Tweet tweet = listOfTweets.get(position);
        String tweetMessage = tweet.getCleanedMessage() + "," + tweet.getEmotionType();

        synchronized (listOfTweets)
        {
            listOfTweets.remove(position);
            sizeTweetList--;
        }

        return tweetMessage;
    }

    public void shutdown() {
        this.twitterStream.shutdown();
    }

    private void setUpStreamListener() {
        listener = new StatusListener() {

            public void onStatus(Status status) {
                add(status);
            }
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }
            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }
        };

        twitterStream.addListener(listener);
    }
}
