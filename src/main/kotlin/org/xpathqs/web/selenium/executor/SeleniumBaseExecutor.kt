package org.xpathqs.web.selenium.executor

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.selector.Selector
import org.xpathqs.driver.actions.ClickAction
import org.xpathqs.driver.actions.ScreenShotAction
import org.xpathqs.driver.executor.ActionExecMap
import org.xpathqs.driver.executor.Decorator
import org.xpathqs.driver.extensions.screenshot
import org.xpathqs.log.Log
import org.xpathqs.web.selenium.util.Screenshot
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


open class SeleniumBaseExecutor(
    val webDriver: WebDriver,
    origin: Decorator
) : Decorator(origin) {

    override val actions = ActionExecMap().apply {
        set(ClickAction(Selector()).name) {
            executeAction(it as ClickAction)
        }
        set(ScreenShotAction(Selector()).name) {
            executeAction(it as ScreenShotAction)
        }
    }

    protected open fun executeAction(action: ClickAction) {
        Log.step(action.toStyledString()) {
            action.on.screenshot()

            if(action.moveMouse) {
                val el = action.on.toWebElement()
                val builder = Actions(webDriver)
                builder.moveToElement(el).click(el)
                builder.perform()
            } else {
                driver.click(action.on)
            }
        }
    }

    open fun getAbsY(driver: WebDriver, element: WebElement?): Int {
        val y = (driver as JavascriptExecutor).executeScript(
            "return arguments[0].getBoundingClientRect().top", element
        )
        val res = y as? Long ?: y as Double
        return res.toInt()
    }


    open fun getAbsX(driver: WebDriver, element: WebElement?): Int {
        val x = (driver as JavascriptExecutor).executeScript(
            "return arguments[0].getBoundingClientRect().left", element
        )
        val res = x as? Long ?: x as Double
        return res.toInt()
    }

    protected open fun executeAction(action: ScreenShotAction) {
        if(disableAllScreenshots || !enableScreenshots) {
            return
        }

        val sc = Screenshot(webDriver)
        val bi = if(action.boundRect) {
            val elem = action.sel.toWebElement()
            (webDriver as JavascriptExecutor).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});",
                elem)
            //Thread.sleep(1000)
            val r = action.sel.toWebElement().rect

            val absY = getAbsY(webDriver, action.sel.toWebElement())
            val absX = getAbsX(webDriver, action.sel.toWebElement())

            r.y = absY
            r.x = absX
            sc.take(r)
        } else sc.take()

        val baos = ByteArrayOutputStream()
        ImageIO.write(bi, "png", baos)
        val bytes = baos.toByteArray()

        Log.attachment(bytes.toTypedArray())
    }

    protected val ISelector.webElement: WebElement
        get() {
            return webDriver.findElement(By.xpath(this.toXpath()))
        }

    protected val ISelector.webElements: Collection<WebElement>
        get() {
            return webDriver.findElements(By.xpath(this.toXpath()))
        }

    private fun ISelector.toWebElement(): WebElement {
        try {
            return webDriver.findElement(By.xpath(this.toXpath()))
        } catch (e: Exception) {
            Log.error("Element can't be found by xpath:")
            Log.xpath(this.toXpath())
            throw e
        }
    }

    companion object {
        var enableScreenshots = true//(System.getenv("enableScreenshots") ?: "false").toBoolean()
        var disableAllScreenshots = true
    }
}