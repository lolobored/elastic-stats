package org.lolobored.elastic.monitor;

import org.apache.commons.io.FileUtils;
import org.lolobored.elastic.monitor.model.QueryStat;
import org.lolobored.elastic.monitor.model.SlowLog;
import org.lolobored.elastic.monitor.service.CSVSerializer;
import org.lolobored.elastic.monitor.service.QueriesRunner;
import org.lolobored.elastic.monitor.service.SlowLogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.Console;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SpringBootApplication
public class ElasticSearchStats implements ApplicationRunner {
  private Logger logger = LoggerFactory.getLogger(ElasticSearchStats.class);
  public static String ERROR_LOG= "Errors.log";
  public static String RESULT_CSV= "Results.csv";

  @Autowired
  SlowLogParser slowLogParser;
  @Autowired
  QueriesRunner queriesRunner;
  @Autowired
  CSVSerializer csvSerializer;

  public static void main(String[] args)  {
    SpringApplication application = new SpringApplication(ElasticSearchStats.class);
    application.setWebApplicationType(WebApplicationType.NONE);
    application.run(args);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {

    if (!args.containsOption("elastic")) {
      logger.error("Option --elastic is mandatory and should contain the URL to Elastic Search master node");
      System.exit(-1);
    }
    if (!args.containsOption("user")) {
      logger.error("Option --user is mandatory and should contain the username to elastic search");
      System.exit(-1);
    }
    if (!args.containsOption("dir")) {
      logger.error("Option --dir is mandatory and should contain the path to the directory where the slow logs are");
      System.exit(-1);
    }

    Console console = System.console();

    Integer iterations= 10;
    String elasticUrl= args.getOptionValues("elastic").get(0);
    String userName= args.getOptionValues("user").get(0);
    String password="";
    if (!args.containsOption("password")) {
      char[] passwordArray = console.readPassword("Enter ES password for " + userName + ": ");
      password = new String(passwordArray);
    }
    else{
      password = args.getOptionValues("password").get(0);
    }

    if (args.containsOption("iterations")) {
      iterations= Integer.parseInt(args.getOptionValues("iterations").get(0));
    }

    //remove existing files
    FileUtils.deleteQuietly(new File(RESULT_CSV));
    FileUtils.deleteQuietly(new File(ERROR_LOG));

    String slowLogDirectory = args.getOptionValues("dir").get(0);
    Collection<File> logFiles = FileUtils.listFiles(new File(slowLogDirectory), new String[]{"log"}, false);
    List<QueryStat> stats= new ArrayList<>();
    for (File logFile : logFiles) {
      logger.info("Processing queries from "+logFile.getName());
      List<SlowLog> queries = slowLogParser.parseSlowLogs(logFile);
      stats.addAll(queriesRunner.runQueries(logFile.getName(), queries, elasticUrl, userName, password, iterations));
    }
    csvSerializer.outputCSV(stats);


  }
}
