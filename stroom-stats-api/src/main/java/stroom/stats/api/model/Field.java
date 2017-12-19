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
import stroom.stats.api.model.Filter;
import stroom.stats.api.model.Format;
import stroom.stats.api.model.Sort;

/**
 * Describes a field in a result set. The field can have various expressions applied to it, e.g. SUM(), along with sorting, filtering, formatting and grouping
 */
@ApiModel(description = "Describes a field in a result set. The field can have various expressions applied to it, e.g. SUM(), along with sorting, filtering, formatting and grouping")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2017-12-19T09:26:40.688Z")
public class Field {
  @SerializedName("name")
  private String name = null;

  @SerializedName("expression")
  private String expression = null;

  @SerializedName("sort")
  private Sort sort = null;

  @SerializedName("filter")
  private Filter filter = null;

  @SerializedName("format")
  private Format format = null;

  @SerializedName("group")
  private Integer group = null;

  public Field name(String name) {
    this.name = name;
    return this;
  }

   /**
   * The name of the field for display purposes
   * @return name
  **/
  @ApiModelProperty(example = "null", value = "The name of the field for display purposes")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Field expression(String expression) {
    this.expression = expression;
    return this;
  }

   /**
   * The expression to use to generate the value for this field
   * @return expression
  **/
  @ApiModelProperty(example = "SUM(${count})", required = true, value = "The expression to use to generate the value for this field")
  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public Field sort(Sort sort) {
    this.sort = sort;
    return this;
  }

   /**
   * Get sort
   * @return sort
  **/
  @ApiModelProperty(example = "null", value = "")
  public Sort getSort() {
    return sort;
  }

  public void setSort(Sort sort) {
    this.sort = sort;
  }

  public Field filter(Filter filter) {
    this.filter = filter;
    return this;
  }

   /**
   * Get filter
   * @return filter
  **/
  @ApiModelProperty(example = "null", value = "")
  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public Field format(Format format) {
    this.format = format;
    return this;
  }

   /**
   * Get format
   * @return format
  **/
  @ApiModelProperty(example = "null", value = "")
  public Format getFormat() {
    return format;
  }

  public void setFormat(Format format) {
    this.format = format;
  }

  public Field group(Integer group) {
    this.group = group;
    return this;
  }

   /**
   * If this field is to be grouped then this defines the level of grouping, with 0 being the top level of grouping, 1 being the next level down, etc.
   * @return group
  **/
  @ApiModelProperty(example = "null", value = "If this field is to be grouped then this defines the level of grouping, with 0 being the top level of grouping, 1 being the next level down, etc.")
  public Integer getGroup() {
    return group;
  }

  public void setGroup(Integer group) {
    this.group = group;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Field field = (Field) o;
    return Objects.equals(this.name, field.name) &&
        Objects.equals(this.expression, field.expression) &&
        Objects.equals(this.sort, field.sort) &&
        Objects.equals(this.filter, field.filter) &&
        Objects.equals(this.format, field.format) &&
        Objects.equals(this.group, field.group);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, expression, sort, filter, format, group);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Field {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    expression: ").append(toIndentedString(expression)).append("\n");
    sb.append("    sort: ").append(toIndentedString(sort)).append("\n");
    sb.append("    filter: ").append(toIndentedString(filter)).append("\n");
    sb.append("    format: ").append(toIndentedString(format)).append("\n");
    sb.append("    group: ").append(toIndentedString(group)).append("\n");
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

