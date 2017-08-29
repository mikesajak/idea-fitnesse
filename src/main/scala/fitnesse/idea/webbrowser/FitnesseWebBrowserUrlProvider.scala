package fitnesse.idea.webbrowser

import com.intellij.ide.browsers.{OpenInBrowserRequest, WebBrowserUrlProvider}
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.{ModuleRootManager, ProjectFileIndex}
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.{Url, UrlImpl}
import fitnesse.idea.config.FitNessePluginConfig
import fitnesse.idea.filetype.FitnesseLanguage

import scala.annotation.tailrec

class FitnesseWebBrowserUrlProvider extends WebBrowserUrlProvider {

  override def canHandleElement(request: OpenInBrowserRequest): Boolean = {
    if (isFitnesseLang(request.getElement.getLanguage)) super.canHandleElement(request)
    else false
  }

  override def getUrl(request: OpenInBrowserRequest, file: VirtualFile): Url = {
    val path = genUrl(file, request.getProject)

    path.map(p => new UrlImpl(p))
        .orNull
  }

  def isFitnesseLang(lang: Language): Boolean = lang.getID == FitnesseLanguage.INSTANCE.getID

  def genUrl(file: VirtualFile, project: Project): Option[String] = {
    val module = ProjectFileIndex.SERVICE.getInstance(project).getModuleForFile(file)
    val moduleRootManager = ModuleRootManager.getInstance(module)
    val contentRoots = moduleRootManager.getContentRoots.map(_.getCanonicalPath) ++
      moduleRootManager.getSourceRoots.map(_.getCanonicalPath)

    val fitnesseRoot = FitNessePluginConfig.getInstance.fitnesseRoot
    val fitnesseRoots =
      (if (fitnesseRoot != null && fitnesseRoot.length > 0)
        contentRoots.map(root => s"$root/$fitnesseRoot")
      else
        Array()
        ) ++ contentRoots

    val path = fitnesseRoots.find(root => file.getCanonicalPath.startsWith(root))
      .map(root => file.getCanonicalPath.substring(root.length))
      .map(p => stripPrefix("/", p))
      .map(p => stripSuffix(".wiki", p))
      .map(p => stripSuffix("/content.txt", p))
      .map(p => p.replaceAll("/|\\\\", "."))

    val localServerUrl = FitNessePluginConfig.getInstance.fitServerUrl
    path.map(p => s"$localServerUrl/$p")
  }

  @tailrec
  private def stripPrefix(prefix: String, text: String): String =
    if(text.startsWith(prefix))
      stripPrefix(prefix, text.substring(prefix.length))
    else text

  @tailrec
  private def stripSuffix(suffix: String, text: String): String =
    if (text.endsWith(suffix))
      stripSuffix(suffix, text.substring(0, text.length - suffix.length))
    else text
}
