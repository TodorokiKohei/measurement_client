package measurement.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import measurement.client.nats.NatsDriver;

public class Measurement {

    private CommandLine cmd = null;
    public static final Logger logger = Logger.getLogger(Measurement.class.getName());

    private void parseArgs(String[] args){
        Options options = new Options();
        options.addOption(Option.builder("c")
            .longOpt("config")
            .argName("config")
            .hasArg()
            .build()
        );
        options.addOption(Option.builder("l")
            .longOpt("log-level")
            .argName("log-level")
            .hasArg()
            .build()
        );
        options.addOption(Option.builder("d")
            .longOpt("driver")
            .argName("driver")
            .hasArg()
            .required()
            .build()
        );
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            logger.warning("Failed to parse argument.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initLogger(){
        if (cmd.getOptionValue("log-level") == null){
            logger.setLevel(Level.INFO);
        }else{
            logger.setLevel(Level.parse(cmd.getOptionValue("log-level")));
        }
    }

    public Measurement(String[] args){
        parseArgs(args);
        initLogger();
        logger.info("Initialization is completed.");

        Driver driver = null;
        if (cmd.getOptionValue("d").equals("nats")){
            driver = new NatsDriver(cmd.getOptionValue("config"));
        }else{
            logger.warning("The -d or --driver setting is not valid.");
            System.exit(1);
        }

        logger.info("Prepare connected clients.");
        driver.setupClients();

        logger.info("Start measurement.");
        driver.startMeasurement();

        logger.info("Stop measurement.");
        driver.stopMeasurement();

        logger.info("Terminate client connection.");
        driver.treadownClients();
    }

    public static void main(String[] args) {
        new Measurement(args);
    }
}
