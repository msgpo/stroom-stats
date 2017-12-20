/*
 * Stroom Stats API
 * APIs for interacting with Stroom Stats.
 *
 * OpenAPI spec version: v1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package stroom.stats.api.model;

import java.util.Objects;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import stroom.stats.api.model.OffsetRange;
import stroom.stats.api.model.TableSettings;

/**
 * A definition for how to return the raw results of the query in the SearchResponse, e.g. sorted, grouped, limited, etc.
 */
@ApiModel(description = "A definition for how to return the raw results of the query in the SearchResponse, e.g. sorted, grouped, limited, etc.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2017-12-19T13:37:48.590Z")
public class ResultRequest {
  @SerializedName("componentId")
  private String componentId = null;

  @SerializedName("mappings")
  private List<TableSettings> mappings = new ArrayList<TableSettings>();

  @SerializedName("requestedRange")
  private OffsetRange requestedRange = null;

  @SerializedName("openGroups")
  private List<String> openGroups = new ArrayList<String>();

  /**
   * The style of results required. FLAT will provide a FlatResult object, while TABLE will provide a TableResult object
   */
  public enum ResultStyleEnum {
    @SerializedName("FLAT")
    FLAT("FLAT"),
    
    @SerializedName("TABLE")
    TABLE("TABLE");

    private String value;

    ResultStyleEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  @SerializedName("resultStyle")
  private ResultStyleEnum resultStyle = null;

  /**
   * The fetch mode for the query. NONE means fetch no data, ALL means fetch all known results, CHANGES means fetch only those records not see in previous requests
   */
  public enum FetchEnum {
    @SerializedName("NONE")
    NONE("NONE"),
    
    @SerializedName("CHANGES")
    CHANGES("CHANGES"),
    
    @SerializedName("ALL")
    ALL("ALL");

    private String value;

    FetchEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  @SerializedName("fetch")
  private FetchEnum fetch = null;

  public ResultRequest componentId(String componentId) {
    this.componentId = componentId;
    return this;
  }

   /**
   * The ID of the component that will receive the results corresponding to this ResultRequest
   * @return componentId
  **/
  @ApiModelProperty(example = "null", required = true, value = "The ID of the component that will receive the results corresponding to this ResultRequest")
  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public ResultRequest mappings(List<TableSettings> mappings) {
    this.mappings = mappings;
    return this;
  }

  public ResultRequest addMappingsItem(TableSettings mappingsItem) {
    this.mappings.add(mappingsItem);
    return this;
  }

   /**
   * Get mappings
   * @return mappings
  **/
  @ApiModelProperty(example = "null", required = true, value = "")
  public List<TableSettings> getMappings() {
    return mappings;
  }

  public void setMappings(List<TableSettings> mappings) {
    this.mappings = mappings;
  }

  public ResultRequest requestedRange(OffsetRange requestedRange) {
    this.requestedRange = requestedRange;
    return this;
  }

   /**
   * Get requestedRange
   * @return requestedRange
  **/
  @ApiModelProperty(example = "null", required = true, value = "")
  public OffsetRange getRequestedRange() {
    return requestedRange;
  }

  public void setRequestedRange(OffsetRange requestedRange) {
    this.requestedRange = requestedRange;
  }

  public ResultRequest openGroups(List<String> openGroups) {
    this.openGroups = openGroups;
    return this;
  }

  public ResultRequest addOpenGroupsItem(String openGroupsItem) {
    this.openGroups.add(openGroupsItem);
    return this;
  }

   /**
   * TODO
   * @return openGroups
  **/
  @ApiModelProperty(example = "null", required = true, value = "TODO")
  public List<String> getOpenGroups() {
    return openGroups;
  }

  public void setOpenGroups(List<String> openGroups) {
    this.openGroups = openGroups;
  }

  public ResultRequest resultStyle(ResultStyleEnum resultStyle) {
    this.resultStyle = resultStyle;
    return this;
  }

   /**
   * The style of results required. FLAT will provide a FlatResult object, while TABLE will provide a TableResult object
   * @return resultStyle
  **/
  @ApiModelProperty(example = "null", required = true, value = "The style of results required. FLAT will provide a FlatResult object, while TABLE will provide a TableResult object")
  public ResultStyleEnum getResultStyle() {
    return resultStyle;
  }

  public void setResultStyle(ResultStyleEnum resultStyle) {
    this.resultStyle = resultStyle;
  }

  public ResultRequest fetch(FetchEnum fetch) {
    this.fetch = fetch;
    return this;
  }

   /**
   * The fetch mode for the query. NONE means fetch no data, ALL means fetch all known results, CHANGES means fetch only those records not see in previous requests
   * @return fetch
  **/
  @ApiModelProperty(example = "null", value = "The fetch mode for the query. NONE means fetch no data, ALL means fetch all known results, CHANGES means fetch only those records not see in previous requests")
  public FetchEnum getFetch() {
    return fetch;
  }

  public void setFetch(FetchEnum fetch) {
    this.fetch = fetch;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultRequest resultRequest = (ResultRequest) o;
    return Objects.equals(this.componentId, resultRequest.componentId) &&
        Objects.equals(this.mappings, resultRequest.mappings) &&
        Objects.equals(this.requestedRange, resultRequest.requestedRange) &&
        Objects.equals(this.openGroups, resultRequest.openGroups) &&
        Objects.equals(this.resultStyle, resultRequest.resultStyle) &&
        Objects.equals(this.fetch, resultRequest.fetch);
  }

  @Override
  public int hashCode() {
    return Objects.hash(componentId, mappings, requestedRange, openGroups, resultStyle, fetch);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultRequest {\n");
    
    sb.append("    componentId: ").append(toIndentedString(componentId)).append("\n");
    sb.append("    mappings: ").append(toIndentedString(mappings)).append("\n");
    sb.append("    requestedRange: ").append(toIndentedString(requestedRange)).append("\n");
    sb.append("    openGroups: ").append(toIndentedString(openGroups)).append("\n");
    sb.append("    resultStyle: ").append(toIndentedString(resultStyle)).append("\n");
    sb.append("    fetch: ").append(toIndentedString(fetch)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
  
}
