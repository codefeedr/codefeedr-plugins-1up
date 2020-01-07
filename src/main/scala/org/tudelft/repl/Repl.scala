package org.tudelft.repl

import org.codefeedr.pipeline.Pipeline
import org.tudelft.repl.commands.{MetaCommand, PipelineCommand}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

case class ReplEnv(pipelines: List[(Pipeline, Boolean)]){}

trait Command extends ((ReplEnv, String) => (ReplEnv, Try[String]))

trait Parser {
  val regex: String
  def parse(expr: String): Option[Command]

  def matches(input: String): Boolean = input.matches(regex)
}


object Commands {
  val parsers: List[Parser] = List[Parser](
    MetaCommand,
//    SQLCommand,
    PipelineCommand
  )

  def apply(expr: String): Option[Command] =
    parsers.find(p => p.matches(expr)) match {
      case Some(parser) => parser.parse(expr)
      case _ => None
    }
}

object Repl extends App{

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

  loop(ReplEnv(List[(Pipeline, Boolean)]()))
}
