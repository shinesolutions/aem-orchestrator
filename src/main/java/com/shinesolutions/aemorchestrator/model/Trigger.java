package com.shinesolutions.aemorchestrator.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Trigger {
    
    @JsonProperty("MetricName")
    private String metricName;
    
    @JsonProperty("Namespace")
    private String namespace;
    
    @JsonProperty("StatisticType")
    private String statisticType;
    
    @JsonProperty("Statistic")
    private String statistic;
    
    @JsonProperty("Unit")
    private String unit;
    
    @JsonProperty("ComparisonOperator")
    private String comparisonOperator;
    
    @JsonProperty("TreatMissingData")
    private String treatMissingData;
    
    @JsonProperty("EvaluateLowSampleCountPercentile")
    private String evaluateLowSampleCountPercentile;

    @JsonProperty("Period")
    private int period;

    @JsonProperty("EvaluationPeriods")
    private int evaluationPeriods;

    @JsonProperty("Threshold")
    private double threshold;

    @JsonProperty("Dimensions")
    private List<Dimension> dimensions;

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getStatisticType() {
        return statisticType;
    }

    public void setStatisticType(String statisticType) {
        this.statisticType = statisticType;
    }

    public String getStatistic() {
        return statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getComparisonOperator() {
        return comparisonOperator;
    }

    public void setComparisonOperator(String comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }

    public String getTreatMissingData() {
        return treatMissingData;
    }

    public void setTreatMissingData(String treatMissingData) {
        this.treatMissingData = treatMissingData;
    }

    public String getEvaluateLowSampleCountPercentile() {
        return evaluateLowSampleCountPercentile;
    }

    public void setEvaluateLowSampleCountPercentile(String evaluateLowSampleCountPercentile) {
        this.evaluateLowSampleCountPercentile = evaluateLowSampleCountPercentile;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getEvaluationPeriods() {
        return evaluationPeriods;
    }

    public void setEvaluationPeriods(int evaluationPeriods) {
        this.evaluationPeriods = evaluationPeriods;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<Dimension> dimensions) {
        this.dimensions = dimensions;
    }
    
}
