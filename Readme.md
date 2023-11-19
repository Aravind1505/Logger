<h2>Guide</h2>

<div>Application runs on port 3000, localhost</div>

<div>Required dependencies</div>
<li>Spark</li>
<li>Elastic Search</li>
<li>Jackson core</li>
<li>slf4j</li>
<li>docker</li>

<div>
First Initiate docker with :
</div>

<code>
sudo docker run -d --name elastic-test -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.8.2
</code>

<div>
To reset docker container : 
<li>docker kill "containerId"</li>
<li>docker container prune </li>
</div>

<div>Apis</div>

<ul>
<li>/dump
    <div>To dump a single json object</div>
</li>

</ul>