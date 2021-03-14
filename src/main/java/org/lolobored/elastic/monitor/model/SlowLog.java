package org.lolobored.elastic.monitor.model;

import lombok.Data;

@Data
public class SlowLog {
  private String file;
  private String index;
  private String query;
}
