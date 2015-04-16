package com.gshakhn.idea.idea.fitnesse.lang.psi

import com.gshakhn.idea.idea.fitnesse.lang.psi.Disgracer.disgraceClassName
import com.gshakhn.idea.idea.fitnesse.lang.reference.DecisionInputReference
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.search.{GlobalSearchScope, PsiShortNamesCache}

class DecisionInput(node: ASTNode) extends Cell(node) {

  def fixtureClassName = getRow.getTable.getFixtureClass.fixtureClassName

  def fixtureMethodName = "set" + disgraceClassName(node.getText)

  override def getReferences: Array[PsiReference] = {
    // PsiShortNamesCache: Allows to retrieve files and Java classes, methods and fields in a project by non-qualified names
    val cache = PsiShortNamesCache.getInstance(getProject)
    val classes = cache.getClassesByName(fixtureClassName, GlobalSearchScope.projectScope(getProject))

    classes.flatMap(_.findMethodsByName(fixtureMethodName, true /* checkBases */))
      .map(new DecisionInputReference(_, this))
  }
}