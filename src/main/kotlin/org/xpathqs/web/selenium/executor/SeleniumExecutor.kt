package org.xpathqs.web.selenium.executor

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.selector.Selector
import org.xpathqs.driver.actions.WaitForSelectorAction
import org.xpathqs.driver.actions.WaitForSelectorDisappearAction
import org.xpathqs.driver.executor.ActionExecMap
import org.xpathqs.driver.executor.Decorator
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.web.selenium.constants.Global
import java.util.concurrent.TimeUnit


open class SeleniumExecutor(
    webDriver: WebDriver,
    origin: Decorator
) : SeleniumBaseExecutor(webDriver, origin) {

    override val actions = ActionExecMap().apply {
        set(WaitForSelectorAction(Selector()).name) {
            executeAction(it as WaitForSelectorAction)
        }
        set(WaitForSelectorDisappearAction(Selector()).name) {
            executeAction(it as WaitForSelectorDisappearAction)
        }
    }

    override fun getAttr(selector: BaseSelector, attr: String, model: IBaseModel?): String {
        val elem = selector.webElement
        return if (attr == Global.TEXT_ARG) elem.text else elem.getAttribute(attr)
    }

    override fun getAttrs(selector: BaseSelector, attr: String, model: IBaseModel?): Collection<String> {
        return selector.webElements.map { it.getAttribute(attr) }
    }

    override fun isPresent(selector: ISelector): Boolean {
        webDriver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS)
        val res = selector.webElements.isNotEmpty()
        webDriver.manage().timeouts().implicitlyWait(Global.WAIT_FOR_ELEMENT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)

        return res
    }

    override fun getElementsCount(selector: ISelector): Int {
        return selector.webElements.size
    }

    protected open fun executeAction(action: WaitForSelectorAction) {
        val wait = WebDriverWait(webDriver, Global.WAIT_FOR_ELEMENT_TIMEOUT)
        wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                    action.selector.toXpath()
                )
            )
        )
    }

    protected open fun executeAction(action: WaitForSelectorDisappearAction) {
        val wait = WebDriverWait(webDriver, Global.WAIT_FOR_ELEMENT_TIMEOUT)
        wait.until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(
                    action.selector.toXpath()
                )
            )
        )
    }
}