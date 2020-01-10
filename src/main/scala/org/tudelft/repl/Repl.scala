package org.tudelft.repl

import org.codefeedr.pipeline.Pipeline
import org.tudelft.repl.commands.{MetaCommand, PipelineCommand}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
  * Case class representing the environment of the REPL
  *
  * @param pipelines a list of pipeliens
  */
case class ReplEnv(pipelines: List[(Pipeline, Boolean)]) {}

/**
  * Every kind of command will implement this trait to ensure the same functionality
  */
trait Command extends ((ReplEnv, String) => (ReplEnv, Try[String]))

/**
  * The parser trait takes care of user input matching
  */
trait Parser {
  val regex: String

  /**
    * From a given input, return the correct command
    *
    * @param expr The user input
    * @return The corresponding command, or None if it didn't match
    */
  def parse(expr: String): Option[Command]

  /**
    * Checks whether the input matches the regex
    *
    * @param input the input to match
    * @return true if input matches regex, else false
    */
  def matches(input: String): Boolean = input.matches(regex)
}


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

/**
  * The loop functionality of the REPL
  */
object Repl extends App {

  /**
    * Until the program is terminated keep asking for user input and process it
    *
    * @param env the current environment of the REPL
    */
  @tailrec def loop(env: ReplEnv): Unit = {
    printf("codefeedr> ")
    Console.flush()

    val input = scala.io.StdIn.readLine()

    Commands(input) match {
      case Some(cmd) => {
        val res = cmd(env, input)
        res._2 match {
          case Success(x) => println(x)
          case Failure(x) => System.err.println(x.getMessage)
        }
        loop(res._1)
      }
      case None => {
        println(s"No such command: $input")
        loop(env)
      }
    }
  }

  //Call loop with an empty env
  loop(ReplEnv(List[(Pipeline, Boolean)]()))
}
