package org.lolobored.elastic.monitor.service;

import org.lolobored.elastic.monitor.model.QueryStat;

import java.io.IOException;
import java.util.List;

public interface CSVSerializer {
  void outputCSV(List<QueryStat> stats) throws IOException;
}
