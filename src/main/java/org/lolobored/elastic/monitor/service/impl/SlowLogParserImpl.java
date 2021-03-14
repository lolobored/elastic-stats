package org.lolobored.elastic.monitor.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.lolobored.elastic.monitor.model.SlowLog;
import org.lolobored.elastic.monitor.service.SlowLogParser;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Service
public class SlowLogParserImpl implements SlowLogParser {
  @Override
  public List<SlowLog> parseSlowLogs(File slowLogPath) throws IOException {
    List<SlowLog> result = new ArrayList<>();
    String content= FileUtils.readFileToString(slowLogPath, Charset.defaultCharset());
    String[] queries = StringUtils.split(content, "\n");

    for (String query : queries) {
      if (query.contains("index.search.slowlog.query")) {
        SlowLog slowLog = new SlowLog();
        String[] queryParams = StringUtils.split(query, " ");
        String index = StringUtils.substringBetween(queryParams[2], "[", "]");
        slowLog.setIndex(index);
        String esQuery = StringUtils.substringAfter(queryParams[9], "source[");
        esQuery = StringUtils.substringBeforeLast(esQuery, "]");

        //remove unmanaged query part for ES7
        esQuery = StringUtils.replace(esQuery, "\"disable_coord\":false,", "");

        slowLog.setQuery(esQuery);
        slowLog.setFile(slowLogPath.getName());
        result.add(slowLog);
      }
    }
    return result;
  }
}
