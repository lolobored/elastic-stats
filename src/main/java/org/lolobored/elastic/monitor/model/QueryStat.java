package org.lolobored.elastic.monitor.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class QueryStat {
  private String file;
  private String query;
  private Integer queryNumber;
  private Integer iterations;
  private List<Long> times= new ArrayList<>();
  private BigDecimal averageTime;
}
