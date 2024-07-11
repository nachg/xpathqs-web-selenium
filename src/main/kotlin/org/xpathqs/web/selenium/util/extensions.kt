package org.xpathqs.web.selenium.util

import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.driver.extensions.waitForVisible
import org.xpathqs.web.selenium.constants.Global
import org.xpathqs.web.selenium.driver.toWebElement
import java.time.Duration

fun <T : BaseSelector> T.waitForClickable() {
    this.waitForVisible(Duration.ofSeconds(2))
    this.toWebElement()
    val wait = WebDriverWait(Global.webDriver.get(), Duration.ofSeconds(2))
    wait.until (ExpectedConditions.elementToBeClickable(this.toWebElement()))
}