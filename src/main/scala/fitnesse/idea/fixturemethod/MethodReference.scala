package fitnesse.idea.fixturemethod

import com.intellij.openapi.util.TextRange
import com.intellij.psi._
import com.intellij.psi.search.PsiShortNamesCache
import fitnesse.idea.etc.Regracer
import fitnesse.idea.etc.SearchScope.searchScope
import fitnesse.idea.scenariotable.ScenarioName

import scala.collection.immutable.SortedSet

class MethodReference(referer: FixtureMethod) extends PsiPolyVariantReferenceBase[FixtureMethod](referer, new TextRange(0, referer.getTextLength)) {

  val project = referer.getProject

  // TODO: take into account Library and Import tables. Search for ancestors.
  override def getVariants = referer.fixtureClass match {
    case Some(fixtureClass) =>
      fixtureClass.getReference.resolve match {
        case c: PsiClass => c.getAllMethods.map(m => Regracer.regrace(m.getName))
        case _ => Array.emptyObjectArray
      }
    case None => Array.emptyObjectArray
  }

  override def multiResolve(b: Boolean): Array[ResolveResult] = getReferencedMethods.toArray

  protected def createReference(element: PsiElement): ResolveResult = new PsiElementResolveResult(element)

  protected def getReferencedMethods: Seq[ResolveResult] = referer.fixtureClass match {
    case Some(fixtureClass) =>
      // TODO: take into account Library and Import tables. Search for ancestors.
      fixtureClass.getReference.resolve match {
        case c: PsiClass =>
          val methods = c.findMethodsByName(referer.fixtureMethodName, true /* checkBases */ ).view

          val fields =
            fieldNames(referer.fixtureMethodName)
              .view
              .flatMap(name => Option(c.findFieldByName(name, true /* checkBases */)))
              .filter(isPublicField)

          (fields ++ methods).map(createReference).toSeq

        case s: ScenarioName => List(createReference(s))
        case _ => List.empty
      }
    case None =>
      val cache = PsiShortNamesCache.getInstance(project)
      cache.getMethodsByName(referer.fixtureMethodName, searchScope(project)).map(createReference)
  }

  private final val AccessorMethodPattern = "(?:set|get|is)(.+)".r

  private def fieldNames(methodName: String): Set[String] = methodName match {
    case AccessorMethodPattern(name) =>
      SortedSet(name, lowerFirst(name), upperFirst(name))
    case _ =>
      Set.empty
  }

  private def lowerFirst(name: String) = name(0).toLower + name.substring(1)
  private def upperFirst(name: String) = name(0).toUpper + name.substring(1)

  private def isPublicField(field: PsiField) =
    field.getModifierList.hasExplicitModifier(PsiModifier.PUBLIC)

  override def handleElementRename(newElementName: String): PsiElement = {
    referer.setName(newElementName)
  }

}
