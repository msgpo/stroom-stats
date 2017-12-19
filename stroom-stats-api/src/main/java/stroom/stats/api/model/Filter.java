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

/**
 * A pair of regular expression filters (inclusion and exclusion) to apply to the field.  Either or both can be supplied
 */
@ApiModel(description = "A pair of regular expression filters (inclusion and exclusion) to apply to the field.  Either or both can be supplied")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2017-12-19T09:26:40.688Z")
public class Filter {
  @SerializedName("includes")
  private String includes = null;

  @SerializedName("excludes")
  private String excludes = null;

  public Filter includes(String includes) {
    this.includes = includes;
    return this;
  }

   /**
   * Only results matching this filter will be included
   * @return includes
  **/
  @ApiModelProperty(example = "^[0-9]{3}$", value = "Only results matching this filter will be included")
  public String getIncludes() {
    return includes;
  }

  public void setIncludes(String includes) {
    this.includes = includes;
  }

  public Filter excludes(String excludes) {
    this.excludes = excludes;
    return this;
  }

   /**
   * Only results NOT matching this filter will be included
   * @return excludes
  **/
  @ApiModelProperty(example = "^[0-9]{3}$", value = "Only results NOT matching this filter will be included")
  public String getExcludes() {
    return excludes;
  }

  public void setExcludes(String excludes) {
    this.excludes = excludes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Filter filter = (Filter) o;
    return Objects.equals(this.includes, filter.includes) &&
        Objects.equals(this.excludes, filter.excludes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(includes, excludes);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Filter {\n");
    
    sb.append("    includes: ").append(toIndentedString(includes)).append("\n");
    sb.append("    excludes: ").append(toIndentedString(excludes)).append("\n");
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

