package fitnesse.idea.config

import javax.swing.JComponent

import com.intellij.openapi.options.SearchableConfigurable

class FitNessePluginConfigurable extends SearchableConfigurable {
  private var mGui: FitnessePluginConfigurationForm = _

  override def getId: String = "preference.FitNessePluginConfigurable"
  override def getDisplayName: String = "FitNesse Plugin Configuration"
  override def getHelpTopic: String = getId

  override def createComponent(): JComponent = {
    mGui = new FitnessePluginConfigurationForm
    mGui.createUI()
    mGui.getRootPanel
  }

  override def enableSearch(s: String): Runnable = null

  override def apply(): Unit = mGui.apply()

  override def isModified: Boolean = mGui.isModified

  override def reset(): Unit = mGui.reset()

  override def disposeUIResources(): Unit = mGui = null
}
