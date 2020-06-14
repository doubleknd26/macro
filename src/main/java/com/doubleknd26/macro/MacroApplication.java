package com.doubleknd26.macro;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.doubleknd26.macro.service.MacroService;
import com.doubleknd26.macro.service.MacroServiceFactory;
import com.doubleknd26.macro.util.MacroType;
import com.doubleknd26.macro.util.MessageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MacroApplication {
	
	@Parameter(
		names={ "--config-path" },
		required = false, 
		description = "This is a configuration file path.")
	private static String configPath = "com/doubleknd26/macro/config/prod.yml";
	
	@Parameter(
		names={ "--macro-type" },
		required = true,
		converter = MacroTypeConverter.class,
		description = "This program run differently based on macro type.")	
	private static MacroType macroType;

	private static final Logger logger = LogManager.getLogger();
	private ExecutorService executorService;
	private MacroConfig config;
	private int numThreads;
	
	private void init() throws FileNotFoundException{
		config = new MacroConfig(configPath, macroType);
		numThreads = config.getServiceConfigs().size();
		logger.info("thread num will be used: " + numThreads);
		
		MessageService.createInstance(config.getMessageServiceUrl(), config.getMessageServiceChannel());
		logger.info("messageService is started.");

		executorService = Executors.newFixedThreadPool(numThreads);
		Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
	}
	
	private void start() {
		logger.info(this.getClass().getSimpleName() + " is started");
		config.getServiceConfigs().forEach(config -> {
			MacroService service = MacroServiceFactory.create(config);
			executorService.submit(service::start);
		});
		executorService.shutdown();
	}

	/**
	 * NOTE: Use std here since the logger may have been reset by its JVM shutdown hook.
	 */
	private void stop() {
		if (executorService != null) {
			try {
				if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
					executorService.shutdownNow();
				}
			} catch (InterruptedException e) {
				System.err.println(e);
				executorService.shutdownNow();
			}
		}
		String endMessage = "MacroApplication is finished.";  
		try {
			MessageService.getInstance().noti(endMessage);
		} catch (NullPointerException e) {
			// do nothing
		}
		System.out.println(endMessage);
	}
	
	public static void main(String[] args) throws Exception {
		MacroApplication macroApp = new MacroApplication();
		new JCommander(macroApp, args);
		macroApp.init();
		macroApp.start();
	}
}
