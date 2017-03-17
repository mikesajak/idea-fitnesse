package fitnesse.idea.fixtureclass

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubBase
import fitnesse.idea.psi.PsiSuite
import fitnesse.idea.scenariotable.{ScenarioName, ScenarioNameElementType, ScenarioNameIndex, ScenarioNameStubImpl}
import fitnesse.idea.table.Table
import org.mockito.Matchers.{any, eq => m_eq}
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

import scala.collection.JavaConverters._

class FixtureClassReferenceTest extends PsiSuite with BeforeAndAfter {

  before {
    Mockito.reset(myPsiShortNamesCache, myStubIndex)
  }

  test("resolve simple class name") {
    val table = createTable("| script | table name |")
    val myPsiClass = mock[PsiClass]("TableName")
    when(myPsiShortNamesCache.getAllClassNames()).thenReturn(Array("TableName"))
    when(myPsiShortNamesCache.getClassesByName(m_eq("TableName"), any[GlobalSearchScope])).thenReturn(Array(myPsiClass))

    val result = table.fixtureClass.get.getReference.multiResolve(false)

    result should not be empty
    result.map(_.getElement) should contain only myPsiClass
  }

  test("resolve simple class name with Fixture suffix") {
    val table = createTable("| script | table name |")
    val myPsiClass = mock[PsiClass]("TabbleName")
    val myPsiClass2 = mock[PsiClass]("TableNameFixture")

    when(myPsiShortNamesCache.getAllClassNames()).thenReturn(Array("TableName", "TableNameFixture"))
    when(myPsiShortNamesCache.getClassesByName(m_eq("TableName"), any[GlobalSearchScope])).thenReturn(Array(myPsiClass))
    when(myPsiShortNamesCache.getClassesByName(m_eq("TableNameFixture"), any[GlobalSearchScope])).thenReturn(Array(myPsiClass2))

    val result = table.fixtureClass.get.getReference.multiResolve(false)

    result should not be empty
    result.map(_.getElement) should contain inOrderOnly (myPsiClass2, myPsiClass)
  }

  test("resolve fully qualified class name") {
    val table = createTable("| script | eg.SampleTable |")
    val myPsiClass = mock[PsiClass]("SampleTable")
    when(myJavaPsiFacade.findClasses(m_eq("eg.SampleTable"), any[GlobalSearchScope])).thenReturn(Array(myPsiClass))

    val result = table.fixtureClass.get.getReference.multiResolve(false)

    result should not be empty
    result.map(_.getElement) should contain only myPsiClass
  }

  test("resolve class + scenario for decision table") {
    val table = createTable("| script | table name |")
    val myPsiClass = mock[PsiClass]("TableName")
    val myScenario: ScenarioName = ScenarioNameElementType.INSTANCE.createPsi(new ScenarioNameStubImpl(mock[StubBase[Table]], "decision table", List()))
    when(myPsiShortNamesCache.getAllClassNames()).thenReturn(Array("TableName"))
    when(myPsiShortNamesCache.getClassesByName(m_eq("TableName"), any[GlobalSearchScope])).thenReturn(Array(myPsiClass))
    when(myStubIndex.get(m_eq(ScenarioNameIndex.KEY), m_eq("TableName"), any[Project], any[GlobalSearchScope])).thenReturn(List(myScenario).asJava)
    val result = table.fixtureClass.get.getReference.multiResolve(false)

    result should not be empty
    result.map(_.getElement) should contain only myPsiClass
  }

  test("completion options for a fixture class") {
    val table = createTable("| script | table name |")
    when(myPsiShortNamesCache.getAllClassNames()).thenReturn(Array("FixtureClass"))
    when(myStubIndex.getAllKeys(m_eq(ScenarioNameIndex.KEY), any[Project])).thenReturn(List("Scenario").asJava)

    val result = table.fixtureClass.get.getReference.getVariants()

    result should contain only "fixture class"
  }

  test("completion options for decision table should contain both fixture classes and scenarios") {
    val table = createTable("| table name |")
    when(myPsiShortNamesCache.getAllClassNames()).thenReturn(Array("FixtureClass"))
    when(myStubIndex.getAllKeys(m_eq(ScenarioNameIndex.KEY), any[Project])).thenReturn(List("Scenario").asJava)

    val result = table.fixtureClass.get.getReference.getVariants()

    result should contain inOrderOnly("fixture class", "Scenario")
  }

}
