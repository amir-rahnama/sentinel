**Sentinel** is project written in Java to perform real-time stream mining on Twitter Public Stream using [SAMOA](http://samoa-project.net/) and [Apache Storm](https://storm.incubator.apache.org/). Sentinel is a distributed system that aims to use new distributed algorithms. Currently Sentinel only supports real-time distributed classifications. See ```Tasks``` section for details on how to work with Sentinel.
 

##Components
### Twitter Stream Instance 
This component implements SAMOA's InstanceStreamClass  which gets stream from ```StreamAPIReader``` and performs sketching, filtering and etc.

### Twitter Stream API Reader
This components connects to Twitter Public Stream API and reads instances and keeps the instances in an adaptive sliding window.

### Model
Represents an MVC style model, set of core attributes, setters and getters.

### Processors
Processors perform text normalization to Tweets such as removing emoticons, URLs and Twitter Specific Characters.

### Sketch
Sketching algorithms such as SpaceSavings keep a summary of the text in-memory so that real-time stream mining could become possible. Also, they enable online approaches to data stream mining which are more adaptive than hold-out approaches, e.g. batch analysis of stream data.

### Feature Reducer
This components transforms tweet texts into an sparse feature vectors and only keeps frequent features in memory only. 


### Language Detector
This component uses the classification approach for detecting language of a tweet according to [Language Detection Library for Java](https://code.google.com/p/language-detection/).


## Usage
Sentinel is a module of a bigger project. In order to use Sentinel, you need to run it with Apache Storm and SAMOA. Read the information at [https://github.com/ambodi/samoa](https://github.com/ambodi/samoa).



## Install
Clone [SAMOA fork for Sentinel](https://github.com/ambodi/samoa)
```
git clone https://github.com/ambodi/samoa
```

Clone [Sentinel]
```
git clone https://github.com/ambodi/sentinel
```
Put Sentinel under 
```
samoa-api/src/main/java/com/yahoo/labs/samoa/sentinel
```

Add ```twitter4j.properties``` file in the root of the project. More info at [Twitter 4J's Documentation on Generic properties](http://twitter4j.org/en/configuration.html "Title")

## Build

```
mvn clean install
```

Local Cluster: 
```
mvn package 
```

Apache Storm Cluster:
```
mvn -Pstorm package
```


## Tasks

### Real-time Sentiment Analysis on Twitter Public Stream 
#### Run via Bash
Using Vertical Hoeffding Tree as a distributed parallel classification algorithm, you can perform sentiment analysis on [Twitter Public Stream](https://dev.twitter.com/docs/streaming-apis/streams/public) with Prequential Evaluation Task. 

To perform sentiment analysis on a sample of 100000 tweets in real-time with 4 parallel nodes in your local cluster, run

```
bin/samoa local target/SAMOA-Local-0.2.0-SNAPSHOT.jar "PrequentialEvaluation -d /tmp/dump.csv -i 1000000 -f 100000 -l (classifiers.trees.VerticalHoeffdingTree -p 4) -s com.yahoo.labs.samoa.sentinel.model.TwitterStreamInstance"
```

Or if you run it in Apache Storm, run

```
bin/samoa storm target/SAMOA-Storm-0.2.0-SNAPSHOT.jar "PrequentialEvaluation -d /tmp/dump.csv -i 1000000 -f 100000 -l (classifiers.trees.VerticalHoeffdingTree -p 4) -s com.yahoo.labs.samoa.sentinel.model.TwitterStreamInstance"
```
### Configuration by Code
Put the following code under ```samoa-local(samoa-storm)/src/main/java/com/yahoo/labs/samoa/```:

    public static void main( String[] args ) {
        PrequentialEvaluation pe = new PrequentialEvaluation();
        pe.setFactory(new SimpleComponentFactory());

        pe.dumpFileOption.setValueViaCLIString("/tmp/dump.csv");
        pe.instanceLimitOption.setValue(50);
        pe.sampleFrequencyOption.setValue(5);
        pe.learnerOption.setValueViaCLIString("classifiers.trees.VerticalHoeffdingTree -p 1");
        pe.streamTrainOption.setValueViaCLIString(TwitterStreamInstance.class.getName());

        pe.init();
    }

Run ```mvn -X exec:java -Dexec.mainClass=com.yahoo.labs.samoa.app```


This is preferred if you are developing and want to make use of debug mode.  
<!--
  Copyright (c) 2013 Yahoo! Inc. All Rights Reserved.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. See accompanying LICENSE file.
-->


## License

The use and distribution terms for this software are covered by the
Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html).