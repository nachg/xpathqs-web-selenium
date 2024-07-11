package org.xpathqs.web.selenium.driver

import org.openqa.selenium.*
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait
import org.openqa.selenium.support.ui.WebDriverWait
import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.base.findAnnotation
import org.xpathqs.core.selector.base.hasAnnotation
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.block.findWithAnnotation
import org.xpathqs.core.selector.extensions.text
import org.xpathqs.driver.extensions.click
import org.xpathqs.driver.extensions.ms
import org.xpathqs.driver.extensions.wait
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.log.Log
import org.xpathqs.web.selenium.constants.Global
import java.time.Duration

open class SeleniumWebDriver(
    protected val driver: WebDriver
) : ISeleniumWebDriver {
    override val pageSource: String
        get() = driver.pageSource

    override fun clear(selector: ISelector, clickSelector: ISelector) {
        click(clickSelector)

        val elem = selector.toWebElement()
        var prevValue = elem.getAttribute("value")
        var curValue = prevValue
        var eqCounter = 0
        while (curValue?.isNotEmpty() == true) {
            elem.sendKeys(Keys.BACK_SPACE)
            curValue = elem.getAttribute("value")
            if(prevValue == curValue) {
                eqCounter++
                if(eqCounter > 100) {
                    eqCounter = 0
                    click(clickSelector)
                }
            }
            prevValue = curValue
        }
    }

    override fun click(x: Int, y: Int) {
        Actions(driver).moveByOffset(x, y).click().build().perform()
    }

    override fun click(selector: ISelector) {
        try {
            if(selector is BaseSelector) {
                if(selector.hasAnnotation(UI.Widgets.DropdownItem::class)) {
                    eval {
                        (driver as JavascriptExecutor).executeScript(
                            "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});",
                            selector.toWebElement()
                        )
                        wait(100.ms, "delay after clicking at DropDown item")
                    }
                }
            }
            selector.toWebElement().click()
            /*try {
                selector.toWebElement().click()
            }catch (e: ElementClickInterceptedException) {
                val elem = selector.toWebElement()
                val p: Point = elem.location
                val actions = Actions(driver)
                actions.moveToElement(elem).click().perform()
            }*/
        } catch (e: Exception) {
            val t = WebDriverWait(driver, Global.WAIT_FOR_ELEMENT_TIMEOUT)

            var elem = driver.findElements(By.xpath(selector.toXpath())).firstOrNull {
                it.isDisplayed
            }

            if(elem == null) {
                wait(2000.ms, "delay when there is no element to click on")
                elem = driver.findElements(By.xpath(selector.toXpath())).firstOrNull {
                    it.isDisplayed
                }

                if(elem == null) {
                    throw Exception("No displayed element of $selector")
                }
            }

            eval {
                (driver as JavascriptExecutor).executeScript(
                    "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});",
                    elem)
                wait(500.ms, "delay after scroll in click")
            }
            eval {
                t.until(ExpectedConditions.elementToBeClickable(elem))
            }
            eval {
                t.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector.toXpath())))
            }
            try {
                elem.click()
            }catch (e: Exception) {
                val elem = selector.toWebElement()
                try {
                    elem.click()
                } catch (e: ElementClickInterceptedException) {
                    val p: Point = elem.location
                    val actions = Actions(driver)
                    actions.moveToElement(elem).click().perform()
                }
            }
        }
    }

    override fun input(selector: ISelector, value: String) {
        if(selector is Block) {
            val select = selector.findWithAnnotation(UI.Widgets.Select::class)
            if(select != null) {
                selector.findWithAnnotation(UI.Widgets.OptionItem::class)!!.text(value).click()
                return
            }
            if(selector.findWithAnnotation(UI.Widgets.Input::class) != null){
                input(selector.findWithAnnotation(UI.Widgets.Input::class)!!, value)
                return
            }
        }

        try {
            selector.toWebElement().sendKeys(value)
        } catch (e: Exception) {
            Log.error("Got exception in sendkeys")
            click(selector)
            selector.toWebElement().sendKeys(value)
        }
        val ms = (selector as? BaseSelector)?.findAnnotation<UI.Widgets.Input>()?.afterInputDelayMs ?: 0
        if(ms > 0) {
            wait(ms.ms, "Sleep for $ms after input into $selector")
        }
    }

    private fun eval(l: ()->Unit) {
        try {
            l()
        } catch (e: Exception) {
        }
    }

    override fun open(url: String) {
        driver.get(url)
    }

    override fun submit(selector: ISelector) {
        selector.toWebElement().submit()
    }

    override fun switchTab() {
        val currentHandle = driver.windowHandle
        val otherHandle = driver.windowHandles.filter { it != currentHandle }.firstOrNull()
        otherHandle?.let {
            driver.switchTo().window(it)
        }
    }
}

fun ISelector.toWebElement(): WebElement {
    return try {
        val driver = Global.webDriver.get()
        val wait = FluentWait(driver)
            .withTimeout(Duration.ofSeconds(5))
            .pollingEvery(Duration.ofMillis(250))
            .ignoring(NoSuchElementException::class.java)

        wait.until {
            it.findElement(
                By.xpath(this.toXpath())
            )
        }
    } catch (e: Exception) {
        Log.error("Selector $name can't be found")
        throw e
    }
}

