# IO monad correc flatMap implementation

This repository aims to prove that what may seem the most intuitive way of implementing `flatMap` for the IO monad is actually not correct.

## Presenting the issue

The following runnable example can be found [here](./src/main/scala/Main.scala#L23):

```scala
def printHelloWorld(using Monad[IO]) =
  println("DEFINITION PHASE (nothing should be printed)")
  val io = IO(print("Hello ")).flatMap(_ => IO(println("world!")))

  println("\nEVALUATION PHASE (\"Hello world\" should be printed)")
  io.run()

@main def main(): Unit =
  println("*** Running the bad implementation ***\n")
  printHelloWorld(using badMonadImplementation)
  // *** Running the bad implementation ***

  // DEFINITION PHASE (nothing should be printed)
  // Hello
  // EVALUATION PHASE ("Hello world" should be printed)
  // world!

  println("\n\n*** Running the good implementation ***\n")
  printHelloWorld(using goodMonadImplementation)
  // *** Running the good implementation ***

  // DEFINITION PHASE (nothing should be printed)

  // EVALUATION PHASE ("Hello world" should be printed)
  // Hello world!
```

A computation described by a IO monad should only be evaluated when explicitly run and not while being defined.

The example shows that when using the bad implementation something gets print to the console and that should not happen.

### Different behavior when using for comprehension

Another strange issue is that the bad implementation also behaves differently when using a for comprehension.

The following runnable example can be found [here](./src/main/scala/Main.scala#L40):

```scala
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
  // *** Running the bad implementation ***

  // DEFINITION PHASE (nothing should be printed)
  // Hello world!

  // EVALUATION PHASE ("Hello world" should be printed)

  println("\n\n*** Running the good implementation ***\n")
  printHelloWorldFor(using goodMonadImplementation)
  // *** Running the good implementation ***

  // DEFINITION PHASE (nothing should be printed)

  // EVALUATION PHASE ("Hello world" should be printed)
  // Hello world!
```

## Eplanation

### Structure of the example repository

A basic Monad typeclass is defined [here](./src/main/scala/Monad.scala)

A basic IO datatype is defined [here](./src/main/scala/IO.scala)

In the [Main.scala](./src/main/scala/Main.scala) file are provided two given instances of Monad for IO. They only differ in the implementation of the flatMap function.

### Implementation differences

```scala
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
```

Note that object IO defines an apply function with one call-by-name parameter.

This means that writing `IO(println("Hello"))` will construct an instance of IO without running `println`.

The error resides here, when the flatMap function of the bad implementation is called it will immediately evaluate `f` and therefore `m.run()` too.

While the good implementation will just wrap those calls into a new IO instance without executing them.

### Why the for comprehension behaves differently?
