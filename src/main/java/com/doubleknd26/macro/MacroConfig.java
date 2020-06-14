package com.doubleknd26.macro;

import com.doubleknd26.macro.util.ServiceName;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import com.doubleknd26.macro.util.MacroType;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MacroConfig {
	private Map<String, String> defaultConfig;
	private List<ServiceConfig> serviceConfigs;

	@SuppressWarnings("unchecked")
	public MacroConfig(String configPath, MacroType macroType) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		InputStream inputStream = new FileInputStream(configPath);
		Map map = (Map) yaml.load(inputStream);
		
		setDefaultConfig((Map<String, String>) map.get("DEFAULT"));
		setServiceConfigs((Map<String, List<Map<String, Object>>>) map.get("SERVICE"), macroType);
	}

	private void setDefaultConfig(Map<String, String> config) {
		this.defaultConfig = config;
	}

	private void setServiceConfigs(Map<String, List<Map<String, Object>>> configs, MacroType macroType) {
		this.serviceConfigs = new ArrayList<>();
		for (Map<String, Object> map : configs.get(macroType.getName())) {
			serviceConfigs.add(ServiceConfig.builder()
					.type(macroType)
					.name(ServiceName.fromString(String.valueOf(map.get("name"))))
					.mainPageUrl(String.valueOf(map.get("main_page_url")))
					.loginPageUrl(String.valueOf(map.get("login_page_url")))
					.macroPageUrl(String.valueOf(map.get("macro_page_url")))
					.id(String.valueOf(map.get("id")))
					.pw(String.valueOf(map.get("pw")))
					.userAgent(String.valueOf(map.get("user_agent")))
					.isHeadless((Boolean) map.get("headless"))
					.build());
		}
	}

	public String getMessageServiceUrl() {
		return defaultConfig.get("slack_web_hook_url");
	}
	
	public String getMessageServiceChannel() {
		return defaultConfig.get("slack_channel");
	}
	
	public List<ServiceConfig> getServiceConfigs() {
		return serviceConfigs;
	}
	
	@Getter
	@Builder
	@ToString
	public static class ServiceConfig {
		private MacroType type;
		private ServiceName name;
		private String mainPageUrl;
		private String loginPageUrl;
		private String macroPageUrl;
		private String id;
		private String pw;
		private String userAgent;
		private boolean isHeadless;
	}
}
