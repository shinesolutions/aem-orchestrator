package com.shinesolutions.aemorchestrator.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code Trigger} of a CloudWatch alarm message, describing the alarmed metric, its threshold
 * and evaluation, and its {@code Dimensions}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Trigger {

  private final String metricName;
  private final String namespace;
  private final String statisticType;
  private final String statistic;
  private final String unit;
  private final String comparisonOperator;
  private final String treatMissingData;
  private final String evaluateLowSampleCountPercentile;
  private final int period;
  private final int evaluationPeriods;
  private final double threshold;
  private final List<Dimension> dimensions;

  /**
   * Creates a trigger; also used by Jackson to map the alarm message JSON.
   *
   * <p>{@code dimensions} is copied into an unmodifiable list; {@code null} is kept as {@code
   * null}.
   *
   * <p>Immutable to fix SpotBugs EI_EXPOSE_REP: Jackson builds it through this constructor instead
   * of setters, using the same JSON property names as before.
   */
  @JsonCreator
  public Trigger(
      @JsonProperty("MetricName") String metricName,
      @JsonProperty("Namespace") String namespace,
      @JsonProperty("StatisticType") String statisticType,
      @JsonProperty("Statistic") String statistic,
      @JsonProperty("Unit") String unit,
      @JsonProperty("ComparisonOperator") String comparisonOperator,
      @JsonProperty("TreatMissingData") String treatMissingData,
      @JsonProperty("EvaluateLowSampleCountPercentile") String evaluateLowSampleCountPercentile,
      @JsonProperty("Period") int period,
      @JsonProperty("EvaluationPeriods") int evaluationPeriods,
      @JsonProperty("Threshold") double threshold,
      @JsonProperty("Dimensions") List<Dimension> dimensions) {
    this.metricName = metricName;
    this.namespace = namespace;
    this.statisticType = statisticType;
    this.statistic = statistic;
    this.unit = unit;
    this.comparisonOperator = comparisonOperator;
    this.treatMissingData = treatMissingData;
    this.evaluateLowSampleCountPercentile = evaluateLowSampleCountPercentile;
    this.period = period;
    this.evaluationPeriods = evaluationPeriods;
    this.threshold = threshold;
    this.dimensions =
        dimensions == null ? null : Collections.unmodifiableList(new ArrayList<>(dimensions));
  }

  public String getMetricName() {
    return metricName;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getStatisticType() {
    return statisticType;
  }

  public String getStatistic() {
    return statistic;
  }

  public String getUnit() {
    return unit;
  }

  public String getComparisonOperator() {
    return comparisonOperator;
  }

  public String getTreatMissingData() {
    return treatMissingData;
  }

  public String getEvaluateLowSampleCountPercentile() {
    return evaluateLowSampleCountPercentile;
  }

  public int getPeriod() {
    return period;
  }

  public int getEvaluationPeriods() {
    return evaluationPeriods;
  }

  public double getThreshold() {
    return threshold;
  }

  /** Returns the alarm's dimensions as an unmodifiable list, or {@code null} if absent. */
  public List<Dimension> getDimensions() {
    return dimensions;
  }
}
