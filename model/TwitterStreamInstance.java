package com.yahoo.labs.samoa.sentinel.model;

/*
 * #%L
 * SAMOA
 * %%
 * Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
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

import com.github.javacliparser.FileOption;
import com.github.javacliparser.StringOption;
import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import com.yahoo.labs.samoa.moa.core.Example;
import com.yahoo.labs.samoa.moa.core.InstanceExample;
import com.yahoo.labs.samoa.moa.core.ObjectRepository;
import com.yahoo.labs.samoa.moa.options.AbstractOptionHandler;
import com.yahoo.labs.samoa.moa.options.ClassOption;
import com.yahoo.labs.samoa.moa.streams.InstanceStream;
import com.yahoo.labs.samoa.moa.tasks.TaskMonitor;
import com.yahoo.labs.samoa.sentinel.processors.PipeProcessor;
import com.yahoo.labs.samoa.sentinel.sketch.Sketch;
import com.yahoo.labs.samoa.sentinel.util.FilterTfIdf;

import java.io.*;
import java.util.ArrayList;


public class TwitterStreamInstance extends AbstractOptionHandler implements
    InstanceStream {

    private static final long serialVersionUID = 1L;
    private Tweet tweet = new Tweet();

    protected Writer writer;
    protected BufferedReader reader;
    protected boolean isTrainingOption = false;

    private TwitterStreamAPIReader twitterStreamReader = new TwitterStreamAPIReader();
    protected static InstancesHeader streamHeader;
    protected String lastTweetRead;

    protected boolean isReadingFile;
    protected boolean hasMoreInstances = true;
    protected static FilterTfIdf filterTfIdf;
    protected PipeProcessor processor = new PipeProcessor();
    protected int numInstances = 0;

    public StringOption languageFilterOption = new StringOption("languageFilter", 'l',
        "Filter by language.", "en");
    public ClassOption sketchOption = new ClassOption("sketch", 's',
        "Sketch algorithm to use.", Sketch.class, "SpaceSaving");
    public StringOption queryStringOption = new StringOption("queryString", 'q',
        "Query string to use for obtaining tweets.", "obama");
    public FileOption tweetFileOption = new FileOption("tweetFile", 'f',
        "Destination TWEET file.", null, "tweet", true);
    public FileOption inputTweetFileOption = new FileOption("inputTweetFile", 'i',
        "Input TWEET file.", null, "tweet", true);


    public TwitterStreamInstance(String query, String language, boolean isTraining)
    {
        queryStringOption.setValue(query);
        languageFilterOption.setValue(language);
        isTrainingOption = isTraining;
    }

    public void shutdown()
    {
        twitterStreamReader.shutdown();

        try
        {
            if (this.writer != null)
            {
                writer.close();
            }
            if (this.reader != null)
            {
                reader.close();
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(
                "Failed writing to file ", ex);
        }
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor taskMonitor, ObjectRepository objectRepository)
    {
        preapreReaderWriters();
        filterByLanguage();
        setStreamHeader();
        initFilterTfIdf();
    }

    protected boolean readNextTweetFromFile()
    {
        try
        {
            if (lastTweetRead != null)
            {
                lastTweetRead = reader.readLine();

                if (lastTweetRead != null)
                {
                    tweet = processor.processTweets(lastTweetRead, languageFilterOption.getValue());
                    String cleanedMessage = tweet.getCleanedMessage();
                    if (cleanedMessage != null && !cleanedMessage.equals("") && !cleanedMessage.equals(" "))
                    {
                        lastTweetRead = cleanedMessage + "," + tweet.getEmotionType();
                    }
                    return true;
                }
            }
            if (lastTweetRead == null)
            {
                reader.close();
                reader = null;
            }

            return false;
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(
                "TwitterStream failed to read instance from stream.", ioe);
        }
    }

    private void preapreReaderWriters()
    {
        File destFile = tweetFileOption.getFile();
        File inputFile = inputTweetFileOption.getFile();

        try
        {
            if (destFile != null)
            {
                writer = new BufferedWriter(new FileWriter(destFile));
            }
            if (inputFile != null)
            {
                reader = new BufferedReader(new FileReader(inputFile));
                lastTweetRead = "";
                hasMoreInstances = readNextTweetFromFile();
                isReadingFile = true;
            }
            else
            {
                isReadingFile = false;
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failed writing to file " + destFile, ex);
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

    private void filterByLanguage()
    {
        twitterStreamReader.setLanguage(languageFilterOption.getValue());
        twitterStreamReader.initStream();

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
            twitterStreamReader.filter(queryTrain);
        }
        else
        {
            twitterStreamReader.filter(queryTest);
        }
    }


    @Override
    public InstancesHeader getHeader()
    {
        return this.streamHeader;
    }

    @Override
    public long estimatedRemainingInstances()
    {
        return 0;
    }

    @Override
    public boolean hasMoreInstances()
    {
        return this.hasMoreInstances;
    }

    @Override
    public Example<Instance> nextInstance()
    {
        Instance inst = null;
        if (this.isReadingFile)
        {
            inst = checkIfThereIsAnyInstance();
        }
        else
        {
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
        }
        return new InstanceExample(inst);
    }

    @Override
    public boolean isRestartable()
    {
        return false;
    }

    @Override
    public void restart()
    {
    }

    @Override
    public void getDescription(StringBuilder stringBuilder, int i)
    {
    }

    public Instance checkIfThereIsAnyInstance()
    {
        Instance inst = null;
        String m = "";
        numInstances++;

        if (this.isReadingFile)
        {
            m = this.lastTweetRead;
            this.hasMoreInstances = readNextTweetFromFile();
            if (m != null && !m.equals(""))
            {
                inst = this.filterTfIdf.filter(m, this.getHeader());
            }

        }
        else if (this.twitterStreamReader.size() > 0)
        {
            m = this.twitterStreamReader.getAndRemove(0);
            inst = this.filterTfIdf.filter(m, this.getHeader());
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

    public String[] getSketch(int n)
    {
        return filterTfIdf.getTopTokens(n);
    }

    public String[] getPosSketch(int n)
    {
        return filterTfIdf.getTopPosTokens(n);
    }

    public String[] getNegSketch(int n)
    {
        return filterTfIdf.getTopNegTokens(n);
    }


}
