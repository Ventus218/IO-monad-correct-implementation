object IO:
  opaque type IO[A] = IOImpl[A]
  case class IOImpl[A](exec: () => A)

  def apply[A](a: => A): IO[A] =
    IOImpl(() => a)

  extension [A](io: IO[A]) def run(): A = io.exec()
