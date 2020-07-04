package com.doubleknd26.macro.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class WebDriverWrapper {
	private static final Logger logger = LogManager.getLogger();
	private static final int RETRY_CNT = 2;
	
	private WebDriverWait waitDriver;
	private ChromeOptions options;
	private ChromeDriver driver;


	public WebDriverWrapper(String userAgent, boolean isHeadless) {
		setOptions(userAgent, isHeadless);
		this.driver = new ChromeDriver(options);
		this.driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
		this.driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
		this.driver.manage().deleteAllCookies();
		this.waitDriver = new WebDriverWait(driver, 1);
	}
	
	private void setOptions(String userAgent, boolean isHeadless) {
		ChromeOptions options = new ChromeOptions();
		options.setHeadless(isHeadless);
		if (isHeadless) {
			// override user-agent to avoid some access denied issue.	
			// Some web site block the user-agent with HeadlessChrome.
			// https://stackoverflow.com/questions/54432980
			options.addArguments(String.format("user-agent=%s", userAgent));
		}
		options.setExperimentalOption("excludeSwitches", new String[]{
				"enable-automation"
		});
		// I suffered issue about 'Timed out receiving message from renderer: -0.100'
		// PageLoadStrategy.NONE is an experimental option to solve above issue. ref)
		// https://stackoverflow.com/questions/48450594/selenium-timed-out-receiving-message-from-renderer
		options.setPageLoadStrategy(PageLoadStrategy.NONE);
		this.options = options;
	}
	
	public ChromeOptions getOptions() {
		return options;
	}

	public void quit() {
		driver.quit();
	}

	public void get(String url) {
		Consumer<String> task = driver::get;
		retry(task, url);
	}

	public WebElement findElement(By by) {
		WebElement element = retry(ExpectedConditions.presenceOfElementLocated(by));
		if (element == null) {
			throw new RuntimeException("failed to find element: " + by);
		}
		return element;
	}

	public WebElement findElement(WebElement element, By by) {
		WebElement element1 = retry(ExpectedConditions.presenceOfNestedElementLocatedBy(element, by));
		if (element1 == null) {
			throw new RuntimeException("failed to find element:" + by);
		}
		return element1;
	}

	public List<WebElement> findElements(By by) {
		List<WebElement> elements = retry(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
		if (elements == null) {
			throw new RuntimeException("failed to find elements: " + by);
		}
		return elements;
	}

	public void sendKeyToElement(By by, String key) {
		WebElement element = retry(ExpectedConditions.visibilityOfElementLocated(by));
		Consumer<String> task = element::sendKeys;
		retry(task, key);
	}

	public WebElement findClickableElement(By by) {
		WebElement element = retry(ExpectedConditions.elementToBeClickable(by));
		if (element == null) {
			throw new RuntimeException("failed to find clickable element: " + by);
		}
		return element;
	}

	/**
	 * use it when click makes new page load.
	 * should be clickable.
	 * @param element
	 */
	public void clickAndWait(WebElement element) {
		clickAndWait(element, 2);
	}

	public void clickAndWait(WebElement element, int seconds) {
		element.click();
		wait(seconds);
	}

	/**
	 * https://stackoverflow.com/questions/6521270/webdriver-check-if-an-element-exists
	 * @param element
	 * @param by
	 * @return
	 */
	public boolean isWebElementExists(WebElement element, By by) {
		for (int i = 0; i< RETRY_CNT; i++) {
			try {
				return !element.findElements(by).isEmpty();
			} catch (StaleElementReferenceException e) {}
		}
		return false;
	}

	// https://stackoverflow.com/questions/12967541/how-to-avoid-staleelementreferenceexception-in-selenium
	// It is used to prevent StaleElementReferenceException.
	// TODO: Remove after making service stable without it.
	public void wait(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private <T> void retry(Consumer<T> task, T param) {
		for (int i = 0; i< RETRY_CNT; i++) {
			try {
				task.accept(param);
				break;
			} catch (Exception e) {
			}
		}
	}

	private <T, R> R retry(ExpectedCondition<R> condition) {
		return retry(condition, RETRY_CNT);
	}

	private <T, R> R retry(ExpectedCondition<R> condition, int retryCnt) {
		R response = null;
		for (int i = 0; i < retryCnt; i++) {
			try {
				response = waitDriver.until(condition);
				break;
			} catch (Exception e) {
			}
		}
		return response;
	}
}
