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

import com.github.vatbub.openthesaurus.MouseState.InsideWindow
import com.github.vatbub.openthesaurus.MouseState.OutsideWindow
import com.github.vatbub.openthesaurus.apiclient.DataProvider
import com.github.vatbub.openthesaurus.apiclient.Response
import com.github.vatbub.openthesaurus.apiclient.ResponseWithAntonyms
import com.github.vatbub.openthesaurus.apiclient.ResponseWithBaseForms
import com.github.vatbub.openthesaurus.apiclient.ResponseWithSimilarTerms
import com.github.vatbub.openthesaurus.apiclient.ResponseWithSubstringTerms
import com.github.vatbub.openthesaurus.apiclient.ResultTerm
import com.github.vatbub.openthesaurus.logging.logger
import com.github.vatbub.openthesaurus.preferences.PreferenceKeys.AutoSearchFromClipboard
import com.github.vatbub.openthesaurus.preferences.PreferenceKeys.DataSource
import com.github.vatbub.openthesaurus.preferences.PreferenceKeys.SearchLanguage
import com.github.vatbub.openthesaurus.preferences.preferences
import com.github.vatbub.openthesaurus.util.get
import com.sun.glass.ui.ClipboardAssistance
import java.io.StringWriter
import java.util.logging.Level
import java.util.logging.LogRecord
import javafx.animation.Animation.Status.RUNNING
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.util.Duration
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang3.exception.ExceptionUtils

class MainView : AutoCloseable {
    companion object {
        private const val idleOpacity = 0.5
    }

    @FXML
    private lateinit var anchorPane: AnchorPane

    @FXML
    private lateinit var treeView: TreeView<String>

    @FXML
    private lateinit var progressIndicator: ProgressIndicator

    @FXML
    private lateinit var snackBarTextField: Label

    @FXML
    private lateinit var searchField: TextField

    @FXML
    private lateinit var snackBar: HBox

    @FXML
    private lateinit var forwardButton: Button

    @FXML
    private lateinit var backButton: Button

    @FXML
    private lateinit var settingsButton: Button

    private val currentSearchTermProperty: StringProperty = SimpleStringProperty()
    private val currentResponseProperty: ObjectProperty<Response> = SimpleObjectProperty(null)

    private var mouseStateProperty: ObjectProperty<MouseState> = SimpleObjectProperty(InsideWindow)

    private val windowOpacityAnimationTimelineProperty: ObjectProperty<Timeline> = SimpleObjectProperty(null)

    private val termHistory = TermHistory()
    private var disableHistoryForNextUpdate: Boolean = false

    private var treeItemIndex = mutableMapOf<TreeItem<String>, ResultTerm>()

    @Suppress("SENSELESS_COMPARISON")
    @FXML
    fun initialize() {
        assert(anchorPane != null) { "fx:id=\"anchorPane\" was not injected: check your FXML file 'MainView.fxml'." }
        assert(treeView != null) { "fx:id=\"treeView\" was not injected: check your FXML file 'MainView.fxml'." }
        assert(progressIndicator != null) { "fx:id=\"progressIndicator\" was not injected: check your FXML file 'MainView.fxml'." }
        assert(snackBarTextField != null) { "fx:id=\"snackBarTextField\" was not injected: check your FXML file 'MainView.fxml'." }
        assert(searchField != null) { "fx:id=\"searchField\" was not injected: check your FXML file 'MainView.fxml'." }
        assert(snackBar != null) { "fx:id=\"snackBar\" was not injected: check your FXML file 'MainView.fxml'." }
        assert(forwardButton != null) { "fx:id=\"forwardButton\" was not injected: check your FXML file 'MainView.fxml'." }
        assert(backButton != null) { "fx:id=\"backButton\" was not injected: check your FXML file 'MainView.fxml'." }
        assert(settingsButton != null) { "fx:id=\"backButton\" was not injected: check your FXML file 'MainView.fxml'." }

        progressIndicator.isVisible = false

        initSnackBar()

        setEmptyTreeView()

        subscribeToClipboard()

        mouseStateProperty.addListener { _, _, newValue ->
            when (newValue) {
                InsideWindow -> animateWindowOpacity(1.0, Duration(100.0))
                else -> animateWindowOpacity(idleOpacity, Duration(5000.0))
            }
        }

        windowOpacityAnimationTimelineProperty.addListener { _, oldValue, newValue ->
            oldValue?.pause()
            newValue?.play()
        }

        Platform.runLater {
            val dimensions = backButton.height - 8
            backButton.graphic = ImageView(
                Image(
                    javaClass.getResourceAsStream("back.png"),
                    dimensions,
                    dimensions,
                    false,
                    true
                )
            )
            forwardButton.graphic = ImageView(
                Image(
                    javaClass.getResourceAsStream("forward.png"),
                    dimensions,
                    dimensions,
                    false,
                    true
                )
            )
            settingsButton.graphic = ImageView(
                Image(
                    javaClass.getResourceAsStream("settings.png"),
                    dimensions,
                    dimensions,
                    false,
                    true
                )
            )
        }

        backButton.disableProperty().bind(termHistory.canGoBackProperty.not())
        forwardButton.disableProperty().bind(termHistory.canGoForwardProperty.not())

        currentSearchTermProperty.addListener { _, _, newValue ->
            searchField.text = newValue
            if (newValue == null) {
                currentResponseProperty.set(null)
                return@addListener
            }
            if (!disableHistoryForNextUpdate)
                termHistory.appendToHistory(newValue)
            disableHistoryForNextUpdate = false
            requestSynonyms(newValue)
        }

        currentResponseProperty.addListener { _, _, newValue ->
            progressIndicator.isVisible = false
            if (newValue == null)
                setEmptyTreeView()
            else {
                showThesaurusResult(newValue)
                if (mouseStateProperty.get() == OutsideWindow)
                    highlightWindow()
            }
        }
    }

    private fun setEmptyTreeView() {
        treeView.root = TreeItem(App.stringResources["results.noSearch"])
    }

    @FXML
    fun treeViewOnMouseClicked(event: MouseEvent) {
        if (event.forwardOrBackButtonPressed()) {
            anchorPaneOnMouseClicked(event)
            return
        }

        if (event.clickCount != 2) return
        if (currentResponseProperty.get() == null) return
        val selectedTreeItem = treeView.selectionModel?.selectedItem ?: return
        if (!selectedTreeItem.isLeaf) return
        val selectedTerm = treeItemIndex[selectedTreeItem] ?: return
        val clipboardContent = ClipboardContent()
        clipboardContent.putString(selectedTerm.term)
        Clipboard.getSystemClipboard().setContent(clipboardContent)

        doSnackBarAnimation(App.stringResources["results.copiedToClipboard"].format(selectedTerm.term))
    }

    @FXML
    fun searchButtonOnAction() {
        val term = searchField.text
        currentSearchTermProperty.set(null)
        currentSearchTermProperty.set(term)
    }

    @FXML
    fun goBack() {
        disableHistoryForNextUpdate = true
        currentSearchTermProperty.set(termHistory.goBack())
    }

    @FXML
    fun goForward() {
        disableHistoryForNextUpdate = true
        currentSearchTermProperty.set(termHistory.goForward())
    }

    @FXML
    fun anchorPaneOnMouseClicked(event: MouseEvent) {
        when (event.button) {
            MouseButton.BACK -> {
                if (termHistory.canGoBackProperty.get())
                    goBack()
            }

            MouseButton.FORWARD -> {
                if (termHistory.canGoForwardProperty.get())
                    goForward()
            }

            else -> return
        }
    }

    private fun MouseEvent.forwardOrBackButtonPressed(): Boolean =
        this.button == MouseButton.BACK || this.button == MouseButton.FORWARD

    @FXML
    fun anchorPaneOnMouseEntered() {
        mouseStateProperty.set(InsideWindow)
    }

    @FXML
    fun anchorPaneOnMouseExited() {
        mouseStateProperty.set(OutsideWindow)
    }

    @FXML
    fun settingsButtonOnAction() {
        SettingsView.newInstance().show()
    }

    private fun subscribeToClipboard() {
        val clipboard = Clipboard.getSystemClipboard()
        object : ClipboardAssistance(com.sun.glass.ui.Clipboard.SYSTEM) {
            override fun contentChanged() {
                if (!preferences[AutoSearchFromClipboard]) return
                if (!clipboard.hasString()) return
                currentSearchTermProperty.set(clipboard.string)
            }
        }
    }

    private fun requestSynonyms(term: String) = GlobalScope.launch {
        Platform.runLater { progressIndicator.isVisible = true }
        val result = preferences[DataSource].requestTerm(term, preferences[SearchLanguage]).leftOr {
            logger.info(
                "An error happened while requesting synonyms from Open thesaurus. Response code: ${it.responseCode}; Response content: ${it.responseContent}",
                it.throwable
            )
            Platform.runLater {
                progressIndicator.isVisible = false
                currentResponseProperty.set(null)

                val snackBarText = if (it.responseContent == null)
                    App.stringResources["results.apiErrorNoAdditionalMessage"]
                else
                    App.stringResources["results.apiErrorWithAdditionalMessage"].format(it.responseContent)
                doSnackBarAnimation(snackBarText)
            }
            return@launch
        }

        Platform.runLater { currentResponseProperty.set(result) }
    }

    private fun showThesaurusResult(result: Response) {
        val searchTerm = currentSearchTermProperty.get() ?: ""

        val root = TreeItem(App.stringResources["results.root"].format(searchTerm))
        root.isExpanded = true

        val synonymTreeItemText =
            if (result.synonymSets.isNotEmpty()) App.stringResources["results.synonymsNode"]
            else App.stringResources["results.synonymsNode.noSynonymsFound"]
        val synonymsRoot = TreeItem(synonymTreeItemText)
        val synonymTreeItems = result.synonymSets
            .distinct()
            .associateBy { term -> term.treeItem() }
        synonymsRoot.children.addAll(synonymTreeItems.keys)
        synonymsRoot.isExpanded = true
        root.children.add(synonymsRoot)
        val newTreeItemIndex = mutableMapOf(*synonymTreeItems.toList().toTypedArray())

        if (result is ResponseWithAntonyms) {
            val antonymsRoot = TreeItem(App.stringResources["results.antonymsNode"])
            val antonymTreeItems = result.antonyms
                .distinct()
                .associateBy { term -> term.treeItem() }

            antonymsRoot.children.addAll(antonymTreeItems.keys)
            antonymsRoot.isExpanded = true
            root.children.add(antonymsRoot)
            newTreeItemIndex.putAll(antonymTreeItems)
        }

        if (result is ResponseWithSimilarTerms) {
            val similarTermsRoot = TreeItem(App.stringResources["results.similarTermsNode"])
            val similarTreeItems = result.similarTerms
                .distinct()
                .sortedBy { it.distance }
                .associateBy { term -> term.treeItem() }

            similarTermsRoot.children.addAll(similarTreeItems.keys)
            similarTermsRoot.isExpanded = true
            root.children.add(similarTermsRoot)
            newTreeItemIndex.putAll(similarTreeItems)
        }

        if (result is ResponseWithSubstringTerms) {
            val substringTermsRoot = TreeItem(App.stringResources["results.substringTermsNode"].format(searchTerm))
            val substringTreeItems = result.substringTerms
                .distinct()
                .associateBy { term -> term.treeItem() }

            substringTermsRoot.children.addAll(substringTreeItems.keys)
            substringTermsRoot.isExpanded = true
            root.children.add(substringTermsRoot)
            newTreeItemIndex.putAll(substringTreeItems)
        }

        if (result is ResponseWithBaseForms) {
            val baseFormsRoot = TreeItem(App.stringResources["results.baseFormNode"])
            val baseFormTreeItems = result.baseForms
                .distinct()
                .associateBy { term -> term.treeItem() }

            baseFormsRoot.children.addAll(baseFormTreeItems.keys)
            baseFormsRoot.isExpanded = true
            root.children.add(baseFormsRoot)
            newTreeItemIndex.putAll(baseFormTreeItems)
        }

        treeView.root = root
        treeItemIndex = newTreeItemIndex
    }

    private fun ResultTerm.treeItem(): TreeItem<String> =
        TreeItem(if (additionalNote == null) term else "$term ($additionalNote)")

    private fun initSnackBar() {
        snackBar.opacity = 0.0
    }

    private fun doSnackBarAnimation(
        text: String,
        moveDuration: Duration = Duration(100.0),
        stayDuration: Duration = Duration(3000.0)
    ) {
        snackBarTextField.text = text

        val upValue1 = KeyValue(snackBar.opacityProperty(), 1.0)
        val upKeyFrame1 = KeyFrame(moveDuration, upValue1)

        val upKeyFrame2 = KeyFrame(moveDuration + stayDuration, upValue1)

        val downValue = KeyValue(snackBar.opacityProperty(), 0.0)
        val downKeyFrame = KeyFrame(moveDuration + stayDuration + moveDuration, downValue)

        Timeline(upKeyFrame1, upKeyFrame2, downKeyFrame).play()
    }

    private fun highlightWindow(
        fadeUpDuration: Duration = Duration(100.0),
        holdDuration: Duration = Duration(5000.0),
        fadeDownDuration: Duration = Duration(1000.0)
    ) {
        animateWindowOpacity(1.0, fadeUpDuration)
        animateWindowOpacity(idleOpacity, fadeDownDuration, holdDuration, true)
    }

    private fun animateWindowOpacity(
        targetOpacity: Double,
        moveDuration: Duration = Duration(100.0),
        delay: Duration = Duration.ZERO,
        waitForCurrentAnimation: Boolean = false
    ) {
        val currentAnimation = windowOpacityAnimationTimelineProperty.get()
        if (waitForCurrentAnimation && currentAnimation != null && currentAnimation.status == RUNNING) {
            currentAnimation.setOnFinished {
                animateWindowOpacity(targetOpacity, moveDuration, delay, false)
            }
            return
        }

        val currentStage = App.instance.currentStage ?: return
        val delayKeyValue = KeyValue(currentStage.opacityProperty(), currentStage.opacity)
        val delayKeyFrame = KeyFrame(delay, delayKeyValue)
        val keyValue = KeyValue(currentStage.opacityProperty(), targetOpacity)
        val keyFrame = KeyFrame(delay + moveDuration, keyValue)
        windowOpacityAnimationTimelineProperty.set(Timeline(delayKeyFrame, keyFrame))
    }

    private operator fun Duration.plus(other: Duration): Duration = this.add(other)

    fun showException(record: LogRecord) {
        val alertType = when (record.level) {
            Level.CONFIG -> Alert.AlertType.INFORMATION
            Level.INFO -> Alert.AlertType.INFORMATION
            Level.WARNING -> Alert.AlertType.WARNING
            Level.SEVERE -> Alert.AlertType.ERROR
            else -> Alert.AlertType.INFORMATION
        }

        val alert = Alert(alertType)
        alert.title = when (record.level) {
            Level.CONFIG -> App.stringResources["error.title.config"]
            Level.INFO -> App.stringResources["error.title.info"]
            Level.WARNING -> App.stringResources["error.title.warning"]
            Level.SEVERE -> App.stringResources["error.title.error"]
            else -> App.stringResources["error.title.info"]
        }
        alert.headerText = when (record.level) {
            Level.CONFIG -> App.stringResources["error.headerText.config"]
            Level.INFO -> App.stringResources["error.headerText.info"]
            Level.WARNING -> App.stringResources["error.headerText.warning"]
            Level.SEVERE -> App.stringResources["error.headerText.error"]
            else -> App.stringResources["error.headerText.info"]
        }

        val throwable = record.thrown
        val rootCause = if (throwable == null) null else ExceptionUtils.getRootCause(throwable)!!

        val contentTextBuilder = StringBuilder(record.message)
        if (rootCause != null) {
            contentTextBuilder.append("${rootCause.javaClass.name}: ${rootCause.localizedMessage}")
            val stringWriter = StringWriter()
            stringWriter.write(rootCause.stackTraceToString())

            val label = Label(App.stringResources["error.stacktraceLabel"])
            val textArea = TextArea(stringWriter.toString())
            with(textArea) {
                isWrapText = false
                isEditable = false
                maxWidth = Double.MAX_VALUE
                maxHeight = Double.MAX_VALUE
            }
            GridPane.setVgrow(textArea, Priority.ALWAYS)
            GridPane.setHgrow(textArea, Priority.ALWAYS)

            val expandableContent = GridPane()
            with(expandableContent) {
                maxWidth = Double.MAX_VALUE
                add(label, 0, 0)
                add(textArea, 0, 1)
            }

            alert.dialogPane.expandableContent = expandableContent
        }

        alert.contentText = contentTextBuilder.toString()

        alert.show()
    }

    override fun close() {
        DataProvider.knownImplementations.forEach {
            if (it is AutoCloseable)
                it.close()
        }
    }
}

private enum class MouseState {
    InsideWindow, OutsideWindow
}
