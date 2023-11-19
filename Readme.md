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
```json
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
}```
<hr></hr>

<div>Apis</div>

<ul>
<li>/dump
    <div>To dump a single json object</div>
</li>

</ul>
