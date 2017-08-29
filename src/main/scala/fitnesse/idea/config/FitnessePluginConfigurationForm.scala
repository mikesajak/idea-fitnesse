package fitnesse.idea.config

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{GridBagConstraints, GridBagLayout, Insets}
import javax.swing._
import javax.swing.border.{CompoundBorder, EmptyBorder}

import scala.language.implicitConversions

class TextRowBuilder(panel: JPanel, panelGridBagLayout: GridBagLayout) {

  def this(panel: JPanel) = this(panel, new GridBagLayout)

  private var currentCol = 0
  private var currentRow = 0

  var defInsets = new Insets(0,0,0,0)
  var defAnchor = GridBagConstraints.CENTER
  var defFill = GridBagConstraints.NONE

  panel.setLayout(panelGridBagLayout)

  def nextRow() {
    currentRow += 1
    currentCol = 0
  }

  def addCol(comp: JComponent, gridwidth: Int = 1, gridheight: Int = 1, weightx: Double = 0.0, weighty: Double = 0.0,
             fill: Int = defFill, anchor: Int = defAnchor, insets: Insets = defInsets) = {
    val constr = new GridBagConstraints()
    constr.fill = fill
    constr.anchor = anchor
    constr.insets = insets
    constr.gridx = currentCol
    constr.gridy = currentRow
    constr.gridwidth = gridwidth
    constr.gridheight = gridheight
    constr.weightx = weightx
    constr.weighty = weighty

    panel.add(comp, constr)
    currentCol += 1
    this
  }
}


class FitnessePluginConfigurationForm {
  private var rootPanel: JPanel = _
  private var fitServerUrl: JTextField = _
  private var fitnesseRoot: JTextField = _
  private var fixtureSuffix: JTextField = _
  private var mConfig: FitNessePluginConfig = _

  init()

  implicit def function0ToRunnable(f:() => Unit):Runnable =
    new Runnable{def run() = f()}

  implicit def function1ToActionListener(f: ActionEvent => Unit): ActionListener =
    new ActionListener{
      override def actionPerformed(e: ActionEvent): Unit = f(e)
    }

  private def init(): Unit = {
    rootPanel = new JPanel
    val border = rootPanel.getBorder
    val margin = new EmptyBorder(10, 10, 10, 10)
    rootPanel.setBorder(new CompoundBorder(border, margin))

    val panelGridBagLayout = new GridBagLayout
    panelGridBagLayout.columnWidths = Array[Int](86, 86, 0)
    panelGridBagLayout.rowHeights = Array[Int](20, 20, 20, 20, 20, 0)
    panelGridBagLayout.columnWeights = Array[Double](0.0, 1.0, Double.MinPositiveValue)
    panelGridBagLayout.rowWeights = Array[Double](0.0, 0.0, 0.0, 0.0, 0.0, Double.MinPositiveValue)

    val builder = new TextRowBuilder(rootPanel, panelGridBagLayout)
    builder.defInsets = new Insets(0,0,5,5)
    builder.defFill = GridBagConstraints.HORIZONTAL

    val fitServerUrlLabel = new JLabel("Fitnesse server URL:")
    fitServerUrlLabel.setToolTipText(
      """An URL of the Fitnesse server. It will be used to build URL when opening current
        | fitnesse test file in external browser.""".stripMargin)
    builder.addCol(fitServerUrlLabel, anchor = GridBagConstraints.EAST)
    fitServerUrl = new JTextField
    builder.addCol(fitServerUrl, anchor = GridBagConstraints.WEST).nextRow()

    val fitnesseRootLabel = new JLabel("Fitnesse root folder:")
    fitnesseRootLabel.setToolTipText(
      """A root folder (relative to project/module root) where fitnesse tests are stored.
        | It is home folder where Fitnesse server is looking for fitnesse test files.""".stripMargin)
    builder.addCol(fitnesseRootLabel, anchor = GridBagConstraints.EAST)
    fitnesseRoot = new JTextField
    builder.addCol(fitnesseRoot, anchor = GridBagConstraints.WEST).nextRow()

    val fixtureSuffixLabel = new JLabel("Fixture class suffix:")
    fixtureSuffixLabel.setToolTipText("""A suffix that is used on fixture classes.""")
    builder.addCol(fixtureSuffixLabel, anchor = GridBagConstraints.EAST, fill = GridBagConstraints.NONE)
    fixtureSuffix = new JTextField
    builder.addCol(fixtureSuffix, anchor = GridBagConstraints.WEST).nextRow()
  }

  def createUI(): Unit = {
    mConfig = FitNessePluginConfig.getInstance
    fitServerUrl.setText(mConfig.fitServerUrl)
    fitnesseRoot.setText(mConfig.fitnesseRoot)
    fixtureSuffix.setText(mConfig.fixtureSuffix)
  }

  def getRootPanel: JPanel = rootPanel

  def isModified: Boolean =
    mConfig.fitServerUrl != fitServerUrl.getText |
      mConfig.fitnesseRoot != fitnesseRoot.getText |
      mConfig.fixtureSuffix != fixtureSuffix.getText

  def apply(): Unit = {
    mConfig.fitServerUrl = fitServerUrl.getText
    mConfig.fitnesseRoot = fitnesseRoot.getText
    mConfig.fixtureSuffix = fixtureSuffix.getText
  }

  def reset(): Unit = {
    fitServerUrl.setText(mConfig.fitServerUrl)
    fitnesseRoot.setText(mConfig.fitnesseRoot)
    fixtureSuffix.setText(mConfig.fixtureSuffix)
  }
}
