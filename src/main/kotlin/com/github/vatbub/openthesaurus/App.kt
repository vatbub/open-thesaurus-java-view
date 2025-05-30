/*-
 * #%L
 * Open Thesaurus Java View
 * %%
 * Copyright (C) 2016 - 2021 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.openthesaurus

import com.github.vatbub.openthesaurus.logging.LoggingHandlers
import com.github.vatbub.openthesaurus.logging.exceptionHandler
import com.github.vatbub.openthesaurus.preferences.PreferenceKeys.GuiLanguage
import com.github.vatbub.openthesaurus.preferences.preferences
import com.github.vatbub.openthesaurus.util.XmlResourceBundleControl
import com.github.vatbub.openthesaurus.util.get
import java.util.ResourceBundle
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import kotlinx.coroutines.ExperimentalCoroutinesApi

class App private constructor(callLaunch: Boolean, private vararg val args: String?) : Application() {
    companion object {
        const val appId = "com.github.vatbub.openthesaurus"

        lateinit var instance: App
            private set

        val stringResources: ResourceBundle by lazy {
            ResourceBundle.getBundle(
                "com.github.vatbub.openthesaurus.stringsxml",
                preferences[GuiLanguage],
                XmlResourceBundleControl
            )
        }

        fun actualMain(vararg args: String) {
            Thread.setDefaultUncaughtExceptionHandler(exceptionHandler)
            LoggingHandlers.initializeIfUninitialized()
            App(true, *args)
        }
    }

    @Suppress("unused")
    constructor() : this(false, null)

    init {
        if (callLaunch)
            launch(*args)
    }

    var currentStage: Stage? = null
        private set
    var controllerInstance: MainView? = null
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun start(primaryStage: Stage) {
        instance = this
        currentStage = primaryStage

        val fxmlLoader = FXMLLoader(javaClass.getResource("MainView.fxml"), stringResources)
        val root = fxmlLoader.load<Parent>()
        controllerInstance = fxmlLoader.getController()

        val scene = Scene(root)
        primaryStage.title = stringResources["mainWindow.title"]
        // primaryStage.icons.add(Image(javaClass.getResourceAsStream("icon.png")))
        primaryStage.minWidth = root.minWidth(0.0) + 70
        primaryStage.minHeight = root.minHeight(0.0) + 70
        primaryStage.isAlwaysOnTop = true

        primaryStage.scene = scene

        primaryStage.setOnCloseRequest {
            controllerInstance?.close()
        }

        primaryStage.show()
    }
}
