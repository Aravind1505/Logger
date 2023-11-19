<h2>Guide</h2>

<div>Application runs on port 3000, localhost</div>

<hr></hr>

<div>Required dependencies</div>
<ul>
    <li>Spark</li>
    <li>Elastic Search</li>
    <li>Jackson core</li>
    <li>slf4j</li>
    <li>docker</li>
</ul>
<hr></hr>

<div>
First Initiate docker with :
</div>

<code>sudo docker run -d --name elastic-test -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.8.2</code>
<hr></hr>

<div>
To reset docker container : 
<li>docker kill "containerId"</li>
<li>docker container prune </li>
</div>
<hr></hr>

<div>Log Format</div>
<code>
{
	"level": "error",
	"message": "Failed to connect to DB",
	"resourceId": "server-1234",
	"timestamp": "2023-09-15T08:00:00Z",
	"traceId": "abc-xyz-123",
    	"spanId": "span-456",
    	"commit": "5e5342f",
    	"metadata": {
        	"parentResourceId": "server-0987"
    	}
}
</code>
<hr></hr>

<div>Huge Dump</div>
<div>Enclose objects under [] with ',' after each object</div>
<hr></hr>

<div>Apis</div>

<ul>
<li>/dump
	<div>POST Request</div>
    	<div>To dump a single json object</div>
</li>

<li>/dumpLarge
	<div>POST Request</div>
	<div>To dump a json file/array of json objects</div>
</li>
<li>/search
	<div>GET Request</div>
	<div>To fetch within the data posted by /dump, /dumpLarge </div>
</li>
</ul>
<hr></hr>

<div>Fields for search api</div>
<ul>
<li><code>level</code> : info, debug, error</li>
<li><code>message</code> : message string</li>
<li><code>resourceId</code> : resourceId</li>
<li><code>timestamp</code> : datetime in the given format <code>YYYY-MM-DDTHH:MM:SS.SSSSSSZ</code></li>
<li><code>spanId</code> : spanId</li>
<li><code>commit</code> : commitId</li>
<li><code>parentResourceId</code> : parentResourceId</li>
<li><code>traceId</code> : traceId</li>
<li><code>range</code> : value of this key does not matter however with this should come two additional keys <code>from</code>, <code>to</code>. Both having datetime in the timestamp field format</li>
</ul>

<div>Do not include timestamp && {range,to,from} fields in the same query, it will return unexpected results.</div>
<div>Including <code>range</code> field without <code>to</code>, <code>from</code> fields may halt the code.</div>

<hr></hr>

<div>Example Search Queries</div>

<div><code>http://localhost:3000/search?level=info</code></div>
<div><code>http://localhost:3000/search?level=info&message=db</code></div>
<div><code>http://localhost:3000/search?level=info& timestamp=2023-11-19T</code></div>
<div><code>http://localhost:3000/search?level=info& timestamp=2023-11-19</code></div>
<div><code>http://localhost:3000/search?level=info& timestamp=2023-11-19T14:03:26</code></div>

<div>Ignore space between & and timestamp in the above examples.</div>

<hr></hr>

The current application includes two servers one where data is ingested, search and the other server is elastic container. However we can improve the applications scale by segregating them into separate servers, one for searching, one for data dump, one for elastic container.

<hr></hr>
<h2>Current Issues</h2>

<li>No check for timestamp and range fields.</li>
<li>No format check for timestamp, to, from fields.</li>
<li>Data is ingested as is given</li>
<li>If in large dataset, the format is not expected, not a single object gets ingested and an error is thrown.</li>



