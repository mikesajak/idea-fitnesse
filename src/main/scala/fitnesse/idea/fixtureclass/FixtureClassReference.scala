package fitnesse.idea.fixtureclass

import com.intellij.openapi.module.{Module, ModuleUtilCore}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi._
import com.intellij.psi.search.{GlobalSearchScope, PsiShortNamesCache}
import fitnesse.idea.config.FitNessePluginConfig
import fitnesse.idea.decisiontable.DecisionTable
import fitnesse.idea.etc.Regracer
import fitnesse.idea.etc.SearchScope.searchScope
import fitnesse.idea.scenariotable.ScenarioNameIndex
import fitnesse.idea.table.Table

import scala.collection.JavaConversions._

class FixtureClassReference(referer: FixtureClass) extends PsiPolyVariantReferenceBase[FixtureClass](referer, new TextRange(0, referer.getTextLength)) {

  val project = referer.getProject
  lazy val module = Option(ModuleUtilCore.findModuleForPsiElement(referer))

  private def table: Table = referer match {
    case impl: FixtureClassImpl => impl.table
    case _ => throw new IllegalStateException("Expected a FixtureClassImpl referer")
  }

  // Return array of String, {@link PsiElement} and/or {@link LookupElement}
  override def getVariants = {
    val allClassNames: Array[String] = PsiShortNamesCache.getInstance(project).getAllClassNames.filter(p => p != null).map(Regracer.regrace)
    table match {
      case _ : DecisionTable =>
        // TODO: Really? all keys from project are needed (performance?)
        val scenarioNames =  ScenarioNameIndex.INSTANCE.getAllKeys(project).map(Regracer.regrace).toArray
        Array.concat(allClassNames, scenarioNames).asInstanceOf[Array[AnyRef]]
      case _ =>
       allClassNames.asInstanceOf[Array[AnyRef]]
    }
  }

  override def multiResolve(b: Boolean): Array[ResolveResult] = table match {
    case _: DecisionTable =>
      (getReferencedScenarios ++ getReferencedClasses).toArray
    case _ =>
      getReferencedClasses.toArray
  }

  private def fixtureClassName = referer.fixtureClassName

  protected def isQualifiedName: Boolean = fixtureClassName match {
    case Some(name) =>
      val dotIndex: Int = name.indexOf(".")
      dotIndex != -1 && dotIndex != name.length - 1
    case None => false
  }

  protected def shortName: Option[String] = fixtureClassName match {
    case Some(name) => name.split('.').toList.reverse match {
      case "" :: n :: _ => Some(n)
      case n :: _ => Some(n)
      case _ => Some(name)
    }
    case None => None
  }

  private def createReference(element: PsiElement): ResolveResult = new PsiElementResolveResult(element)

  protected def getReferencedClasses: Iterable[ResolveResult] = fixtureClassName match {
    case Some(className) if isQualifiedName =>
      JavaPsiFacade.getInstance(project).findClasses(className, FixtureClassReference.moduleWithDependenciesScope(module)).map(createReference)
    case Some(className) =>
      findFixtureClass2(shortName.get)
    case None => Seq.empty
  }

  private def findFixtureClass2(name: String): Iterable[ResolveResult] = {
    val cache = PsiShortNamesCache.getInstance(project)
    val fixtureClassSuffix = FitNessePluginConfig.getInstance.fixtureSuffix
    val res1 = Option(cache.getClassesByName(s"$name$fixtureClassSuffix", FixtureClassReference.moduleWithDependenciesScope(module)))
    val res2 = Option(cache.getClassesByName(name, FixtureClassReference.moduleWithDependenciesScope(module)))
    (res1 ++ res2).flatten.map(createReference)
  }

// other version - check performance
//  private final val allClassNames = new HashSet[String]() // optimization
//  private def findFixtureClass(name: String): Seq[ResolveResult] = {
//    allClassNames.clear()
//    PsiShortNamesCache.getInstance(project).getAllClassNames(allClassNames)
//
//    if (allClassNames.contains(s"${name}Fixture")) mkClassRefsFor(s"${name}Fixture")
//    else if (allClassNames.contains(name)) mkClassRefsFor(name)
//    else Seq.empty
//  }
//
//  private def mkClassRefsFor(name: String) =
//    PsiShortNamesCache.getInstance(project)
//      .getClassesByName(name, FixtureClassReference.moduleWithDependenciesScope(module))
//      .map(createReference)

  protected def getReferencedScenarios: Seq[ResolveResult] = referer.fixtureClassName match {
    case Some(className) if isQualifiedName => Seq.empty
    case Some(className) =>
      ScenarioNameIndex.INSTANCE.get(className, project, FixtureClassReference.projectScope(project)).map(createReference).toSeq
    case None => Seq.empty
  }

  override def handleElementRename(newElementName: String): PsiElement = referer.setName(newElementName)
}

// This is a work-around for testing:

object FixtureClassReference {
  /**
   * Override `scopeForTesting` for testing.
   */
  var scopeForTesting: Option[GlobalSearchScope] = None

  def moduleWithDependenciesScope(module: Option[Module]): GlobalSearchScope = scopeForTesting match {
    case Some(scope) => scope
    case None => module match {
      case Some(m) => searchScope(m.getProject)
      case _ => GlobalSearchScope.EMPTY_SCOPE
    }
  }

  def projectScope(project: Project): GlobalSearchScope = scopeForTesting match {
    case Some(scope) => scope
    case None => searchScope(project)
  }
}