import Monad.*
import IO.*

given goodMonadImplementation: Monad[IO] with
  def unit[A](a: A): IO[A] = IO(a)
  extension [A](m: IO[A])
    def flatMap[B](f: A => IO[B]): IO[B] =
      IO(f(m.run()).run()) // The only difference is here

given badMonadImplementation: Monad[IO] with
  def unit[A](a: A): IO[A] = IO(a)
  extension [A](m: IO[A])
    def flatMap[B](f: A => IO[B]): IO[B] =
      f(m.run()) // The only difference is here

def printHelloWorld(using Monad[IO]) =
  println("DEFINITION PHASE (nothing should be printed)")
  val io = IO(print("Hello ")).flatMap(_ => IO(println("world!")))

  println("\nEVALUATION PHASE (\"Hello world\" should be printed)")
  io.run()

@main def main(): Unit =
  println("*** Running the bad implementation ***\n")
  printHelloWorld(using badMonadImplementation)

  println("\n\n*** Running the good implementation ***\n")
  printHelloWorld(using goodMonadImplementation)

def printHelloWorldFor(using Monad[IO]) =
  println("DEFINITION PHASE (nothing should be printed)")
  val ioFor = for
    _ <- IO(print("Hello "))
    _ <- IO(println("world!"))
  yield ()

  println("\nEVALUATION PHASE (\"Hello world\" should be printed)")
  ioFor.run()

@main def mainFor(): Unit =
  println("*** Running the bad implementation ***\n")
  printHelloWorldFor(using badMonadImplementation)

  println("\n\n*** Running the good implementation ***\n")
  printHelloWorldFor(using goodMonadImplementation)
