package fitnesse.idea.config

import com.intellij.openapi.components.{PersistentStateComponent, ServiceManager, Storage, StoragePathMacros, State}
import com.intellij.util.xmlb.XmlSerializerUtil

object FitNessePluginConfig {
  val DEFAULT_FIT_SERVER_URL = "http://localhost:8025"
  val DEFAULT_FIT_ROOT = "FitnesseRoot"
  val DEFAULT_SUFFIX = "Fixture"

  def getInstance: FitNessePluginConfig = {
    var config = ServiceManager.getService(classOf[FitNessePluginConfig])
    if (config == null) config = new FitNessePluginConfig
    config.setupDefaultValuesIfNeeded()
    config
  }
}

@State(name = "FitNessePluginConfig", storages = Array(new Storage(file = StoragePathMacros.APP_CONFIG + "/FitNessePluginConfig.xml")))
class FitNessePluginConfig(private var fitServerUrl0: String,
                           private var fitnesseRoot0: String,
                           private var fixtureSuffix0: String) extends PersistentStateComponent[FitNessePluginConfig] {
  def this() = this(null, null, null)

  import FitNessePluginConfig._

  def fitServerUrl: String = fitServerUrl0
  def fitServerUrl_=(url: String): Unit = fitServerUrl0 = url

  def fitnesseRoot: String = fitnesseRoot0
  def fitnesseRoot_=(root: String): Unit = fitnesseRoot0 = root

  def fixtureSuffix: String = fixtureSuffix0
  def fixtureSuffix_=(suffix: String): Unit = fixtureSuffix0 = suffix

  override def loadState(config: FitNessePluginConfig): Unit = {
    XmlSerializerUtil.copyBean(config, this)
    setupDefaultValuesIfNeeded()
  }

  private def setupDefaultValuesIfNeeded() = {
    if (fitServerUrl == null) fitServerUrl = DEFAULT_FIT_SERVER_URL
    if (fitnesseRoot == null) fitnesseRoot = DEFAULT_FIT_ROOT
    if (fixtureSuffix == null) fixtureSuffix = DEFAULT_SUFFIX
  }

  override def getState: FitNessePluginConfig = this
}
