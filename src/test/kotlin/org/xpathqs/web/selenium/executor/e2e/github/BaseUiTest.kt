package org.xpathqs.web.selenium.executor.e2e.github

import org.openqa.selenium.WebDriver
import org.xpathqs.core.constants.CoreGlobalProps
import org.xpathqs.core.reflection.scanPackage
import org.xpathqs.driver.navigation.NavExecutor
import org.xpathqs.log.BaseLogger
import org.xpathqs.log.Log
import org.xpathqs.log.Logger
import org.xpathqs.log.printers.StreamLogPrinter
import org.xpathqs.log.printers.args.NoArgsProcessor
import org.xpathqs.log.printers.args.StyleArgsProcessor
import org.xpathqs.log.printers.args.TimeArgsProcessor
import org.xpathqs.log.printers.body.BodyProcessorImpl
import org.xpathqs.log.printers.body.HierarchyBodyProcessor
import org.xpathqs.log.printers.body.StyledBodyProcessor
import org.xpathqs.log.style.Style
import org.xpathqs.log.style.StyledString
import org.xpathqs.web.WebPage
import org.xpathqs.web.selenium.constants.Global
import org.xpathqs.web.selenium.driver.SeleniumWebDriver
import org.xpathqs.web.selenium.executor.ExecutorFactory
import org.xpathqs.web.selenium.executor.e2e.github.base.GitHubNavigator
import org.xpathqs.web.selenium.executor.e2e.google.Nav
import org.xpathqs.web.selenium.factory.DriverFactory


open class BaseUiTest() {
    init {
        org.xpathqs.core.constants.Global.update(
            CoreGlobalProps("configDefault.yml")
        )
    }

    protected lateinit var driver: WebDriver

    fun setup() {
        System.setProperty("i18n", "ru")
        driver = DriverFactory().create()
        val webDriver = SeleniumWebDriver(driver)

        val executor = NavExecutor(
            ExecutorFactory(driver, webDriver).getCached(),
            GitHubNavigator
        )

        Global.init(executor)

        var consoleLog = Logger(
            streamPrinter = StreamLogPrinter(
                argsProcessor =
                StyleArgsProcessor(
                    TimeArgsProcessor(
                        NoArgsProcessor()
                    ),
                    Style(textColor = 60)
                ),
                bodyProcessor =
                StyledBodyProcessor(
                    HierarchyBodyProcessor(
                        BodyProcessorImpl()
                    ),
                    level1 = Style(textColor = 48),
                    level2 = Style(textColor = 40),
                    level3 = Style(textColor = 35)
                ),
                writer = System.out
            )
        )

        Log.log = BaseLogger(arrayListOf(consoleLog), StyledString.defaultStyles)
        "org.xpathqs.web.selenium.executor.e2e.github".scanPackage()

        GitHubNavigator.initNavigations()

    }
}