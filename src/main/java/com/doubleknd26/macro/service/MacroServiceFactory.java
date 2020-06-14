package com.doubleknd26.macro.service;

import com.doubleknd26.exercise.macro.MacroConfig;
import com.doubleknd26.exercise.macro.service.mask.CoupangMaskMacroService;
import com.doubleknd26.exercise.macro.util.MacroType;
import com.doubleknd26.exercise.macro.util.ServiceName;

public class MacroServiceFactory {

	public static MacroService create(MacroConfig.ServiceConfig config) {
		if (MacroType.MASK == config.getType()) {
			return createMaskMacroService(config);
		} else {
			throw new RuntimeException("Unknown Macro Type: " + config.getType());
		}
	}

	private static MacroService createMaskMacroService(MacroConfig.ServiceConfig config) {
		if (ServiceName.COUPANG == config.getName()) {
			return new CoupangMaskMacroService(config);
		} else {
			throw new RuntimeException(String.format("Unknown Service Name of %s: %s",
					config.getType(),  config.getName()));
		}
	}
}
