package org.tudelft.repl.commands

import org.tudelft.repl.commands.PipelineCommand.matches
import org.tudelft.repl.{Command, Parser, ReplEnv}

import scala.util.{Success, Try}

object MetaCommand extends Parser with Command{
  //Check regex
  val regex =
    """(?i)^(exit|quit).*$"""

  override def parse(expr: String): Option[Command] = matches(expr) match {
    case true => Option(MetaCommand)
    case _ => None
  }

  override def apply(env: ReplEnv, input: String): (ReplEnv, Try[String]) = {
    println("Exiting system")
    System.exit(0)
    //Just to keep the compiler happy
    (ReplEnv(Nil), Success("exited"))
  }
}
