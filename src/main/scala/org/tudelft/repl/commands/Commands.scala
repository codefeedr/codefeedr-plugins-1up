package org.tudelft.repl.commands

import org.tudelft.repl.{Command, Parser}

/**
  * From a list of commands, select the correct command
  */
object Commands {
  val parsers: List[Parser] = List[Parser](
    MetaCommand,
    //    SQLCommand,
    PipelineCommand
  )

  /**
    * From a user input, parse the correct Command
    *
    * @param expr the user input
    * @return The corresponding command, or None if no Command matched
    */
  def apply(expr: String): Option[Command] =
    parsers.find(p => p.matches(expr)) match {
      case Some(parser) => parser.parse(expr)
      case _ => None
    }
}