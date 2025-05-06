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
  IO(print("Hello ")).flatMap(_ => IO(println("world!")))

def printHelloWorldForComprehension(using Monad[IO]) =
  for
    _ <- IO(print("Hello "))
    _ <- IO(println("world!"))
  yield ()

def test(using Monad[IO]) =
  println("DEFINITION PHASE (nothing should be printed)")
  val io = printHelloWorld
  val ioFor = printHelloWorldForComprehension

  println("EVALUATION PHASE (\"Hello world\" should be printed twice)")
  io.run()
  ioFor.run()

@main def main(): Unit =
  println("*** Running the bad implementation ***\n")
  test(using badMonadImplementation)

  println("\n\n*** Running the good implementation ***\n")
  test(using goodMonadImplementation)
