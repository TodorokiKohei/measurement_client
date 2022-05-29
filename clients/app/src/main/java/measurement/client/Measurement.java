package measurement.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import measurement.client.base.AbstractDriver;
import measurement.client.base.CommonPubConfigs;
import measurement.client.base.CommonSubConfigs;
import measurement.client.base.MeasurementConfigs;
import measurement.client.jetstream.JetStreamDriver;
import measurement.client.kafka.KafkaDriver;
import measurement.client.nats.NatsDriver;

public class Measurement {

    private static final String defaultOutputDir = "results";
    private CommandLine cmd = null;
    public static final Logger logger = Logger.getLogger(Measurement.class.getName());

    // 引数の追加や解析を行う
    private void parseArgs(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("c")
                .longOpt("config")
                .argName("config")
                .hasArg()
                .build());
        options.addOption(Option.builder("l")
                .longOpt("log-level")
                .argName("log-level")
                .hasArg()
                .build());
        options.addOption(Option.builder("d")
                .longOpt("driver")
                .argName("driver")
                .hasArg()
                .required()
                .build());
        options.addOption(Option.builder("o")
                .longOpt("output")
                .argName("output")
                .hasArg()
                .build());
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            logger.warning("Failed to parse argument.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    // loggerのレベルやフォーマットを設定する
    private void initLogger() {
        // levelの設定（デフォルトではINFO）
        if (cmd.getOptionValue("log-level") == null) {
            logger.setLevel(Level.INFO);
        } else {
            logger.setLevel(Level.parse(cmd.getOptionValue("log-level")));
        }
    }

    public Measurement(String[] args) {
        parseArgs(args);
        initLogger();

        // Driver(計測を実行する処理が記述されたクラス)を引数を元に作成
        AbstractDriver driver = null;
        if (cmd.getOptionValue("d").equals("jetstream")) {
            driver = new JetStreamDriver();
        } else if (cmd.getOptionValue("d").equals("kafka")) {
            driver = new KafkaDriver();
        } else if (cmd.getOptionValue("d").equals("nats")){
            driver = new NatsDriver();
        }else {
            logger.warning("The -d or --driver setting is not valid.");
            System.exit(1);
        }

        // 共通コンフィグを取得
        MeasurementConfigs<? extends CommonPubConfigs, ? extends CommonSubConfigs> configs = driver
                .loadConfigs(cmd.getOptionValue("config"));
        driver.setCommonConfigs(configs);

        logger.info("Setup connected clients.");
        Boolean isCompleted = driver.setupClients();
        if (!isCompleted) {
            logger.warning("Client setup failed. Terminate measurement.");
            System.exit(1);
        }
        driver.setupRecoder(cmd.getOptionValue("output", defaultOutputDir));

        logger.info("Start measurement.");
        driver.startMeasurement();

        driver.waitForMeasurement();

        logger.info("Stop measurement.");
        driver.stopMeasurement();

        logger.info("Terminate client connection.");
        driver.treadownClients();

        logger.info("Record client result.");
        driver.recordResults(cmd.getOptionValue("output", defaultOutputDir));
    }

    public static void main(String[] args) {
        new Measurement(args);
    }
}
