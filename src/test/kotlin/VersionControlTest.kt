import com.intellij.driver.sdk.ui.UiText
import com.intellij.driver.sdk.ui.components.UiComponent
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitFound
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.ide.starter.config.ConfigurationStorage
import com.intellij.ide.starter.config.splitMode
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.junit5.hyphenateWithClass
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.GitHubProject
import com.intellij.ide.starter.runner.CurrentTestMethod
import com.intellij.ide.starter.runner.Starter
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.time.Duration.Companion.minutes
import com.intellij.driver.client.Remote
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

// source: https://docs.oracle.com/javase/8/docs/api/javax/swing/JCheckBox.html
@Remote("javax.swing.JCheckBox")
interface JCheckBoxRef {
    fun isSelected(): Boolean
}

class VersionControlTest {
    @ParameterizedTest(name = "split-mode={0}")
    @ValueSource(booleans = [false, true])
    fun sampleTest(splitMode: Boolean) {
        ConfigurationStorage.splitMode(splitMode)

        // derive test name from the class name
        val testName = CurrentTestMethod.hyphenateWithClass()

        // set ide version explicitly to match dependencies
        // build version is 252.28539.54
        val ideVersion = "2025.2.6.2"

        val ide = IdeInfo(
            productCode = "IC",
            platformPrefix = "Idea",
            executableFileName = "idea",
            fullName = "IntelliJ IDEA Community Edition",
            version = ideVersion
        )

        val testContext = Starter
            .newContext(testName, TestCase(ide, GitHubProject.fromGithub(
                branchName = "main",
                repoRelativeUrl = "TeamPraxidike/CAIT.git",
                commitHash = "55f28ffb3dcc340679fa3eec4e87412b4446abce"
            )).useRelease(ideVersion))
            .prepareProjectCleanImport()

        testContext.runIdeWithDriver().useDriverAndCloseIde {
            ideFrame {
                waitForIndicators(3.minutes)

                // open settings view, option available in IdeaFrameUI
                openSettingsDialog()

                // mark settings component by title and class using a query builder function
                val settings: UiComponent = x { byTitle("Settings") and byClass("MyDialog") }
                settings.waitFound()

                // mark the left-hand scroll pane (where the version control section is)
                val tree: UiComponent = settings.x { byClass("MyTree") }
                tree.waitFound()

                // waitOneText returns a UiText which has a click method
                val versionControlText: UiText = tree.waitOneText("Version Control")
                versionControlText.click()

                // selects the panel that pops up on the right
                val vcPanel = settings.x { componentWithChild(byClass("DialogPanel"), byText("Changelists")) }
                vcPanel.waitFound()

                val changeLists: UiComponent = vcPanel.x { byText("Changelists") }
                changeLists.click()

                // scope to the panel that contains the changelists settings
                val changelistsPanel: UiComponent = settings.x { componentWithChild(byClass("DialogPanel"), byText("Create changelists automatically")) }
                changelistsPanel.waitFound()

                // find and click the checkbox
                val checkbox: UiComponent = changelistsPanel.x { byText("Create changelists automatically") }
                checkbox.waitFound()

                // this casts the remote component to JCheckBoxRef,
                // lets us call isSelected() on the actual JCheckBox running in the IDE process.
                val checkBoxReference: JCheckBoxRef = driver.cast(checkbox.component, JCheckBoxRef::class)

                // assert that it has not been selected yet
                assertFalse(checkBoxReference.isSelected()) { "Checkbox should not be selected before clicking" }

                checkbox.click()

                // assert that it has been selected
                assertTrue(checkBoxReference.isSelected()) { "Checkbox should be selected after clicking" }

                // click on ok to finish the test
                val southPanel: UiComponent = settings.x { byClass("SouthPanel") }
                southPanel.x { byText("OK") }.click()
            }
        }
    }
}