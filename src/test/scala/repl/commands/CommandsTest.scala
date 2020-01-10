package repl.commands

import org.scalatest.FunSuite
import org.tudelft.repl.commands.Commands

class CommandsTest extends FunSuite{

  test("CommandsApplyOkTest"){
    //.isInstanceOf doesnt work, probably because MetaCommand is an object and not a class
    assert(Commands.apply("quit").get.getClass.toString == "class org.tudelft.repl.commands.MetaCommand$")
  }

  test("CommandsApplyFailTest"){
    assert(Commands.apply("error").isEmpty)
  }

}
