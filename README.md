Sentinel
=========
Sentinel consists of set of classes to perform real-time stream mining on Twitter Public Stream using [SAMOA](http://samoa-project.net/). 
 

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



## License

The use and distribution terms for this software are covered by the
Apache GNU General Public License, Version 3.0 (http://www.gnu.org/licenses/gpl-3.0.html).




