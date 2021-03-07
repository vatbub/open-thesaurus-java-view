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

import com.github.vatbub.openthesaurus.apiclient.DataProvider
import com.github.vatbub.openthesaurus.preferences.PreferenceKeys.AutoSearchFromClipboard
import com.github.vatbub.openthesaurus.preferences.PreferenceKeys.DataSource
import com.github.vatbub.openthesaurus.preferences.PreferenceKeys.FilterAutoSendFromClipboard
import com.github.vatbub.openthesaurus.preferences.PreferenceKeys.GuiLanguage
import com.github.vatbub.openthesaurus.preferences.PreferenceKeys.SearchLanguage
import com.github.vatbub.openthesaurus.preferences.preferences
import com.github.vatbub.openthesaurus.util.get
import com.github.vatbub.openthesaurus.util.supportedLocales
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import javafx.util.StringConverter
import java.util.*


class SettingsView {
    companion object {
        fun newInstance(): SettingsView {
            val fxmlLoader =
                FXMLLoader(SettingsView::class.java.getResource("SettingsView.fxml"), App.stringResources)
            val root = fxmlLoader.load<Parent>()
            val controller = fxmlLoader.getController<SettingsView>()
            controller.stage = Stage()

            val scene = Scene(root)

            with(controller.stage) {
                title = App.stringResources["settings.windowTitle"]
                // icons.add(Image(javaClass.getResourceAsStream("icon.png")))
                minWidth = root.minWidth(0.0) + 70
                minHeight = root.minHeight(0.0) + 70
                isAlwaysOnTop = true

                this.scene = scene
            }


            return controller
        }
    }

    private lateinit var stage: Stage

    fun show() {
        stage.show()
    }

    @FXML
    private lateinit var filterClipboardSearchCheckBox: CheckBox

    @FXML
    private lateinit var root: GridPane

    @FXML
    private lateinit var clipboardSearchEnabledCheckBox: CheckBox

    @FXML
    private lateinit var guiLanguageChoiceBox: ChoiceBox<Locale>

    @FXML
    private lateinit var dataSourceChoiceBox: ChoiceBox<DataProvider>

    @FXML
    private lateinit var privacyButton: Button

    @FXML
    private lateinit var aboutButton: Button

    @FXML
    private lateinit var searchLanguageChoiceBox: ChoiceBox<Locale>

    @FXML
    fun initialize() {
        dataSourceChoiceBox.converter = object : StringConverter<DataProvider>() {
            override fun toString(dataProvider: DataProvider): String = dataProvider.screenName

            override fun fromString(string: String): DataProvider {
                TODO("Not yet implemented")
            }
        }
        guiLanguageChoiceBox.converter = LocaleConverter
        searchLanguageChoiceBox.converter = LocaleConverter

        guiLanguageChoiceBox.items = FXCollections.observableArrayList(App.stringResources.supportedLocales)
        dataSourceChoiceBox.items = FXCollections.observableArrayList(*DataProvider.knownImplementations.toTypedArray())

        guiLanguageChoiceBox.value = preferences[GuiLanguage]
        dataSourceChoiceBox.value = preferences[DataSource]

        updateSearchLanguageChoiceBox()

        clipboardSearchEnabledCheckBox.isSelected = preferences[AutoSearchFromClipboard]
        filterClipboardSearchCheckBox.isSelected = preferences[FilterAutoSendFromClipboard]

        guiLanguageChoiceBox.valueProperty().addListener { _, _, newValue ->
            preferences[GuiLanguage] = newValue
        }
        dataSourceChoiceBox.valueProperty().addListener { _, _, newValue ->
            preferences[DataSource] = newValue
            updateSearchLanguageChoiceBox()
        }
        searchLanguageChoiceBox.valueProperty().addListener { _, _, newValue ->
            preferences[SearchLanguage] = newValue
        }
        clipboardSearchEnabledCheckBox.selectedProperty().addListener { _, _, newValue ->
            preferences[AutoSearchFromClipboard] = newValue
        }
        filterClipboardSearchCheckBox.selectedProperty().addListener { _, _, newValue ->
            preferences[FilterAutoSendFromClipboard] = newValue
        }
    }

    private fun updateSearchLanguageChoiceBox(dataProvider: DataProvider = preferences[DataSource]) {
        searchLanguageChoiceBox.items = FXCollections.observableArrayList(dataProvider.supportedLocales)
        val savedLocale = preferences[SearchLanguage]
        if (savedLocale in dataProvider.supportedLocales)
            searchLanguageChoiceBox.value = savedLocale
        else {
            searchLanguageChoiceBox.value = dataProvider.supportedLocales.first()
            preferences[SearchLanguage] = dataProvider.supportedLocales.first()
        }
    }

    private object LocaleConverter : StringConverter<Locale>() {
        override fun toString(locale: Locale): String =
            "${locale.getDisplayLanguage(Locale.getDefault())} (${locale.getDisplayLanguage(Locale.ENGLISH)})"

        override fun fromString(string: String?): Locale {
            TODO("Not yet implemented")
        }
    }
}
