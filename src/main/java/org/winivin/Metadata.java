package org.winivin;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "parentResourceId"
})
@Generated("jsonschema2pojo")
public class Metadata {

    @JsonProperty("parentResourceId")
    String parentResourceId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    public Metadata(){}

    public Metadata(String parentResourceId) {
        this.parentResourceId = parentResourceId;
    }

    @JsonProperty("parentResourceId")
    public String getParentResourceId() {
        return parentResourceId;
    }

    @JsonProperty("parentResourceId")
    public void setParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
