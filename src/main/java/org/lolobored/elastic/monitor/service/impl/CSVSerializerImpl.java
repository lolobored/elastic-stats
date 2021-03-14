package org.lolobored.elastic.monitor.service.impl;

import org.apache.commons.io.FileUtils;
import org.lolobored.elastic.monitor.ElasticSearchStats;
import org.lolobored.elastic.monitor.model.QueryStat;
import org.lolobored.elastic.monitor.service.CSVSerializer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class CSVSerializerImpl implements CSVSerializer {
  @Override
  public void outputCSV(List<QueryStat> stats) throws IOException {
    StringBuilder csv= new StringBuilder();
    if (!stats.isEmpty()){
      String header= "File;Query;";
      QueryStat stat = stats.get(0);
      for (int i=0; i< stat.getTimes().size(); i++){
        header+= "Time Iteration "+(i+1)+";";
      }
      header+= "Average";
      csv.append(header).append("\n");
    }

    for (QueryStat stat : stats) {
      csv.append(stat.getFile()).append(";");
      csv.append(stat.getQueryNumber()).append(";");
      for (Long time : stat.getTimes()) {
        csv.append(time).append(";");
      }
      csv.append(stat.getAverageTime()).append("\n");
    }
    FileUtils.write(new File(ElasticSearchStats.RESULT_CSV), csv.toString(),"UTF-8");
  }
}
