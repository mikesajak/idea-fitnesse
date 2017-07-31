package fitnesse.idea.lexer

class MiscLexerSuite extends LexerSuite {
  test("Regular text followed by Wiki Word") {
    assertResult(
      List(
        (FitnesseTokenType.TEXT, "Some"),
        (FitnesseTokenType.WHITE_SPACE, " "),
        (FitnesseTokenType.TEXT, "text"),
        (FitnesseTokenType.WHITE_SPACE, " "),
        (FitnesseTokenType.TEXT, "and"),
        (FitnesseTokenType.WHITE_SPACE, " "),
        (FitnesseTokenType.WIKI_WORD, "WikiWord")
      )) {
      lex("Some text and WikiWord")
    }
  }

  test("Wiki Word followed by a LF") {
    assertResult(
      List(
        (FitnesseTokenType.WIKI_WORD, "WikWord"),
        (FitnesseTokenType.LINE_TERMINATOR, "\n")
      )) {
      lex("WikWord\n")
    }
  }

  test("Wiki Word followed by a CR LF") {
    assertResult(
      List(
        (FitnesseTokenType.WIKI_WORD, "WikWord"),
        (FitnesseTokenType.LINE_TERMINATOR, "\n")
      )) {
      lex("WikWord\n")
    }
  }

  test("Wiki Word that ends in a capital letter followed by a LF") {
    assertResult(
      List(
        (FitnesseTokenType.WIKI_WORD, "WikiWordThisIsA"),
        (FitnesseTokenType.LINE_TERMINATOR, "\n")
      )) {
      lex("WikiWordThisIsA\n")
    }
  }

  test("Wiki Word that ends in a capital letter followed by a CR LF") {
    assertResult(
      List(
        (FitnesseTokenType.WIKI_WORD, "WikiWordThisIsA"),
        (FitnesseTokenType.LINE_TERMINATOR, "\r\n")
      )) {
      lex("WikiWordThisIsA\r\n")
    }
  }

  test("Table that has regular text right after it") {
    assertResult(
      List(
        (FitnesseTokenType.TABLE_START, "|abc|\n|xyz|\n"),
        (FitnesseTokenType.ROW_START, "abc|\n|"),
        (FitnesseTokenType.CELL_START, "abc|\n|"),
        (FitnesseTokenType.TEXT, "abc"),
        (FitnesseTokenType.CELL_END, "|\n|"),
        (FitnesseTokenType.ROW_END, "|\n|"),
        (FitnesseTokenType.ROW_START, "xyz|\n"),
        (FitnesseTokenType.CELL_START, "xyz|\n"),
        (FitnesseTokenType.TEXT, "xyz"),
        (FitnesseTokenType.CELL_END, "|\n"),
        (FitnesseTokenType.ROW_END, "|\n"),
        (FitnesseTokenType.TABLE_END, "|\n"),
        (FitnesseTokenType.TEXT, "some"),
        (FitnesseTokenType.WHITE_SPACE, " "),
        (FitnesseTokenType.TEXT, "text")
      )) {
      lex("|abc|\n|xyz|\nsome text")
    }
  }

  test("Table that has spaces regular text right after it") {
    assertResult(
      List(
        (FitnesseTokenType.TABLE_START, "| abc |\n|xyz|\n"),
        (FitnesseTokenType.ROW_START, " abc |\n|"),
        (FitnesseTokenType.CELL_START, " abc |\n|"),
        (FitnesseTokenType.WHITE_SPACE, " "),
        (FitnesseTokenType.TEXT, "abc"),
        (FitnesseTokenType.WHITE_SPACE, " "),
        (FitnesseTokenType.CELL_END, "|\n|"),
        (FitnesseTokenType.ROW_END, "|\n|"),
        (FitnesseTokenType.ROW_START, "xyz|\n"),
        (FitnesseTokenType.CELL_START, "xyz|\n"),
        (FitnesseTokenType.TEXT, "xyz"),
        (FitnesseTokenType.CELL_END, "|\n"),
        (FitnesseTokenType.ROW_END, "|\n"),
        (FitnesseTokenType.TABLE_END, "|\n"),
        (FitnesseTokenType.TEXT, "some"),
        (FitnesseTokenType.WHITE_SPACE, " "),
        (FitnesseTokenType.TEXT, "text")
      )) {
      lex("| abc |\n|xyz|\nsome text")
    }
  }

  test("Table that has a WikiWord right after it") {
    assertResult(
      List(
        (FitnesseTokenType.TABLE_START, "|abc|\n|xyz|\n"),
        (FitnesseTokenType.ROW_START, "abc|\n|"),
        (FitnesseTokenType.CELL_START, "abc|\n|"),
        (FitnesseTokenType.TEXT, "abc"),
        (FitnesseTokenType.CELL_END, "|\n|"),
        (FitnesseTokenType.ROW_END, "|\n|"),
        (FitnesseTokenType.ROW_START, "xyz|\n"),
        (FitnesseTokenType.CELL_START, "xyz|\n"),
        (FitnesseTokenType.TEXT, "xyz"),
        (FitnesseTokenType.CELL_END, "|\n"),
        (FitnesseTokenType.ROW_END, "|\n"),
        (FitnesseTokenType.TABLE_END, "|\n"),
        (FitnesseTokenType.WIKI_WORD, "WikiWord")
      )) {
      lex("|abc|\n|xyz|\nWikiWord")
    }
  }

  test("Collapsible section") {
    assertResult(
      List(
        (FitnesseTokenType.COLLAPSIBLE_START, "!* abc\ndef\n*!"),
        (FitnesseTokenType.TEXT, "abc"),
        (FitnesseTokenType.TEXT, "def"),
        (FitnesseTokenType.LINE_TERMINATOR, "\n"),
        (FitnesseTokenType.COLLAPSIBLE_END, "*!"),
        (FitnesseTokenType.WHITE_SPACE, " "),
        (FitnesseTokenType.TEXT, "word")
      )) {
      lex("!* abc\ndef\n*! word")
    }
  }

  test("Collapsible section containing a table") {
    assertResult(
      List(
        (FitnesseTokenType.COLLAPSIBLE_START, "!* title\n|abc|\n|xyz|\n*!"),
        (FitnesseTokenType.TEXT, "title"),
        (FitnesseTokenType.TABLE_START, "|abc|\n|xyz|\n"),
        (FitnesseTokenType.ROW_START, "abc|\n|"),
        (FitnesseTokenType.CELL_START, "abc|\n|"),
        (FitnesseTokenType.TEXT, "abc"),
        (FitnesseTokenType.CELL_END, "|\n|"),
        (FitnesseTokenType.ROW_END, "|\n|"),
        (FitnesseTokenType.ROW_START, "xyz|\n"),
        (FitnesseTokenType.CELL_START, "xyz|\n"),
        (FitnesseTokenType.TEXT, "xyz"),
        (FitnesseTokenType.CELL_END, "|\n"),
        (FitnesseTokenType.ROW_END, "|\n"),
        (FitnesseTokenType.TABLE_END, "|\n"),
        (FitnesseTokenType.COLLAPSIBLE_END, "|\n*!")
      )) {
      lex("!* title\n|abc|\n|xyz|\n*!")
    }
  }

}
