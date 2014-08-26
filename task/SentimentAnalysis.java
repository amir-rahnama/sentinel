package com.yahoo.labs.samoa.sentinel.task;

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

import com.github.javacliparser.Configurable;
import com.yahoo.labs.samoa.sentinel.processors.TwitterStreamSourceProcessor;
import com.yahoo.labs.samoa.sentinel.processors.TwitterStreamDestinationProcessor;
import com.yahoo.labs.samoa.tasks.Task;
import com.yahoo.labs.samoa.topology.ComponentFactory;
import com.yahoo.labs.samoa.topology.Stream;
import com.yahoo.labs.samoa.topology.Topology;
import com.yahoo.labs.samoa.topology.TopologyBuilder;

import java.util.logging.Logger;

public class SentimentAnalysis implements Task, Configurable
{
    private final static Logger logger = Logger.getLogger(SentimentAnalysis.class.getName());
    private TopologyBuilder builder;
    private Topology sentimentTopology;
    private TwitterStreamSourceProcessor sourceProcessor;
    private TwitterStreamDestinationProcessor destProcessor;

    /*public IntOption parallelismOption = new IntOption("parallelismOption", 'p', "Number of destination Processors", 1, 1, 20);

    public StringOption evaluationNameOption = new StringOption("evalutionName", 'n', "Identifier of the evaluation", "SentimentTask"
        + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

    public IntOption instanceLimitOption = new IntOption("instanceLimit", 'i', "Maximum number of instances to generate (-1 = no limit).", 1000000, -1,
        Integer.MAX_VALUE);

    public StringOption queryTrainOption = new StringOption("queryTrain", 's',
        "Query string to use for obtaining tweets to train the classifier.", "ipad");

    public StringOption queryTestOption = new StringOption("queryTest", 'w',
        "Query string to use for obtaining tweets to test the classifier.", "iphone");

    public StringOption languageFilterOption = new StringOption("languageFilter", 'a',
        "Filter by language.", "en");*/


    @Override
    public void init()
    {
        //sourceProcessor = new TwitterStreamSourceProcessor(instanceLimitOption.getValue(), queryTrainOption.getValue(), languageFilterOption.getValue(), true);
        sourceProcessor = new TwitterStreamSourceProcessor(10, "", "", true);
        builder.addEntranceProcessor(sourceProcessor);

        Stream stream = builder.createStream(sourceProcessor);

        destProcessor = new TwitterStreamDestinationProcessor();
        //builder.addProcessor(destProcessor, parallelismOption.getValue());
        //builder.addProcessor(destProcessor, parallelismOption.getValue());
        builder.addProcessor(destProcessor, 3);
        builder.connectInputShuffleStream(stream, destProcessor);

        sentimentTopology = builder.build();
        logger.info("Successfully built the topology");
    }

    @Override
    public Topology getTopology()
    {
        return sentimentTopology;
    }

    @Override
    public void setFactory(ComponentFactory factory)
    {
        builder = new TopologyBuilder(factory);
        logger.info("Sucessfullyx instantiating TopologyBuilder");
        //builder.initTopology(evaluationNameOption.getValue());
        //logger.info("Sucessfully initializing SAMOA topology with name {}" + evaluationNameOption.getValue());
        builder.initTopology("HEllo world");
        logger.info("Sucessfully initializing SAMOA topology with name {}" + "hello wrold");
    }
}
