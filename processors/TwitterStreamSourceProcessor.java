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

import com.yahoo.labs.samoa.core.ContentEvent;
import com.yahoo.labs.samoa.core.EntranceProcessor;
import com.yahoo.labs.samoa.core.Processor;
import com.yahoo.labs.samoa.moa.core.ObjectRepository;
import com.yahoo.labs.samoa.moa.options.AbstractOptionHandler;
import com.yahoo.labs.samoa.moa.tasks.TaskMonitor;
import com.yahoo.labs.samoa.sentinel.model.Tweet;
import com.yahoo.labs.samoa.sentinel.sketch.Sketch;
import com.yahoo.labs.samoa.sentinel.util.FeatureReducer;
import com.yahoo.labs.samoa.sentinel.util.FilterTfIdf;
import moa.core.InstancesHeader;
import com.yahoo.labs.samoa.moa.options.ClassOption;
import moa.options.FileOption;
import moa.options.StringOption;
import moa.streams.InstanceStream;
import twitter4j.*;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.ArrayList;


public class TwitterStreamSourceProcessor extends AbstractOptionHandler implements EntranceProcessor
{
    private TwitterStream twitterStream;
    private StatusListener listener;

    private String language = "en";
    protected static FilterTfIdf filterTfIdf;

    int sizeTweetList = 0;
    protected  ArrayList<Tweet> listOfTweets;
    protected FeatureReducer soap;
    private final long maxInst;
    protected boolean isTrainingOption = false;
    protected static InstancesHeader streamHeader;
    protected Writer writer;
    protected int numInstances = 0;

    public StringOption languageFilterOption = new StringOption("languageFilter", 'l',
        "Filter by language.", "en");
    public ClassOption sketchOption = new ClassOption("sketch", 's',
        "Sketch algorithm to use.", Sketch.class, "SpaceSaving");
    public StringOption queryStringOption = new StringOption("queryString", 'q',
        "Query string to use for obtaining tweets.", "obama");

    public FileOption tweetFileOption = new FileOption("tweetFile", 'f',
        "Destination TWEET file.", "testtt", "tweet", true);



    public TwitterStreamSourceProcessor(long maxInst, String query, String language, boolean isTraining) {
        this.maxInst = maxInst;
        twitterStream = new TwitterStreamFactory().getInstance();
        soap = new FeatureReducer();
        listOfTweets = new ArrayList<Tweet>();

        queryStringOption.setValue(query);
        languageFilterOption.setValue(language);
        isTrainingOption = isTraining;

        //initStream();
    }

    //@Override
    public void initStream()
    {
        setListener();
        twitterStream.addListener(listener);
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
        if (sizeTweetList < maxInst) {
            addToList(status);
        }
        else {
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

    public int size() {
        synchronized (listOfTweets) {
            return listOfTweets.size();
        }
    }

    //@Override
    public void setLanguage(String language)
    {
        this.language = language;
    }

    public void shutdown() {
        this.twitterStream.shutdown();
    }

    private void setListener() {
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
    }

    @Override
    public boolean process(ContentEvent event)
    {
        return false;
    }

    @Override
    public void onCreate(int id)
    {
        setListener();
        twitterStream.addListener(listener);

        filterByLanguage();
        setStreamHeader();
        initFilterTfIdf();
    }

    @Override
    public Processor newProcessor(Processor processor)
    {
        TwitterStreamSourceProcessor tssp = (TwitterStreamSourceProcessor) processor;
        return new TwitterStreamSourceProcessor(tssp.maxInst, queryStringOption.getValue(), languageFilterOption.getValue(), isTrainingOption);
    }

    @Override
    public boolean isFinished()
    {
        return numInstances > maxInst;
    }

    @Override
    public boolean hasNext()
    {
        return numInstances <= maxInst;
    }

    @Override
    public ContentEvent nextEvent()
    {
        return new TwitterStreamContentEvent(nextInstance(), false);
    }

    private void filterByLanguage()
    {
        setLanguage(languageFilterOption.getValue());

        String[] queryTest = {queryStringOption.getValue()};
        String[] queryTrain = {"=(", ":-(", ":(", ":{", ":[", "={", "=[", "=)", "=D", ":)", "=P", ":P", "=]", ";)"};
        if (queryStringOption.getValue() != "")
        {
            for (int i = 0; i < queryTrain.length; i++)
            {
                queryTrain[i] = queryStringOption.getValue() + " " + queryTrain[i];
            }
        }

        if (isTrainingOption)
        {
            filter(queryTrain);
        }
        else
        {
            filter(queryTest);
        }
    }

    private void setStreamHeader()
    {
        ArrayList<String> classVal = new ArrayList<String>();
        classVal.add("H");
        classVal.add("S");

        Attribute classAtt = new Attribute("class", classVal);

        ArrayList<Attribute> wekaAtt = new ArrayList<Attribute>();
        wekaAtt.add(classAtt);

        streamHeader = new InstancesHeader(new Instances(
            getCLICreationString(InstanceStream.class), wekaAtt, 0));
        streamHeader.setClassIndex(0);
    }

    private void initFilterTfIdf()
    {
        if (filterTfIdf == null)
        {
            Sketch sketch = (Sketch) getPreparedClassOption(sketchOption);
            filterTfIdf = new FilterTfIdf(sketch);
        }
    }

    public Instance nextInstance()
    {
        Instance inst = null;

            boolean isTweetReady = false;
            while (isTweetReady == false)
            {
                inst = checkIfThereIsAnyInstance();
                if (inst == null)
                {
                    try
                    {
                        Thread.sleep(500);
                        System.out.println("waiting...");
                    }
                    catch (InterruptedException x)
                    {
                    }
                }
                else
                {
                    isTweetReady = true;
                }
            }

        return inst;
    }

    public Instance checkIfThereIsAnyInstance()
    {
        Instance inst = null;
        String m = "";
        numInstances++;


        if (listOfTweets.size() > 0)
        {
            m =getAndRemove(0);
            inst = this.filterTfIdf.filter(m, this.streamHeader);
            if (this.writer != null)
            {
                try
                {
                    writer.write(m);
                    writer.write("\n");
                }
                catch (Exception ex)
                {
                    throw new RuntimeException(
                        "Failed writing to file ", ex);
                }
            }
        }
        if (inst != null)
        {
            System.out.println("CHECK " + m + " ");
        }

        return inst;
    }

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

    public void pop () {
        synchronized (listOfTweets)
        {
            listOfTweets.remove(0);
            sizeTweetList--;
        }
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor, ObjectRepository repository)
    {

    }

    @Override
    public void getDescription(StringBuilder sb, int indent)
    {

    }

    private void preapreReaderWriters() throws IOException
    {
        File destFile = tweetFileOption.getFile();

        if (destFile != null)
        {
            writer = new BufferedWriter(new FileWriter(destFile));
        }
    }

}
