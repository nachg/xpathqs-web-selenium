package org.xpathqs.web.selenium.factory

import io.github.bonigarcia.wdm.WebDriverManager
import io.github.bonigarcia.wdm.config.DriverManagerType
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.xpathqs.log.Log
import org.xpathqs.web.selenium.constants.Global

class DriverFactory(
    val type: DriverManagerType = DriverManagerType.CHROME,
    val version: String = "104.0.5112.79",
    val options: ChromeOptions = ChromeOptions()
) {

    fun create(): WebDriver {
        try {
            WebDriverManager.getInstance(type).driverVersion(version).setup()
        } catch (e: Exception) {
            Log.error(e.message ?: e.toString())
        }
        val res = if(type == DriverManagerType.CHROME) {
            ChromeDriver(options)
        } else {
            val driverClass = Class.forName(type.browserClass())
            driverClass.getDeclaredConstructor().newInstance() as WebDriver
        }

        Global.webDriver.set(res)

        return res
    }

    companion object {
        val default: DriverFactory
            get() = DriverFactory(
                options = getCapabilities()
            )

        private fun getCapabilities(): ChromeOptions {

            val options = ChromeOptions()
            options.addArguments("--allow-insecure-localhost")
            options.addArguments("--disable-web-security")
            options.addArguments("--allow-running-insecure-content")
            options.setCapability(ChromeOptions.CAPABILITY, options)
            options.setCapability("acceptInsecureCerts", true)


            return options
        }
    }
}