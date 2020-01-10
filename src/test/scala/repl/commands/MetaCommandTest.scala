package repl.commands

import org.scalatest.FunSuite
import org.tudelft.repl.commands.MetaCommand

class MetaCommandTest extends FunSuite{

  test("parseOkTest"){
    assert(MetaCommand.parse("exit").isDefined)
  }

  test("parseFailTest"){
    assert(MetaCommand.parse("error").isEmpty)
  }
}
