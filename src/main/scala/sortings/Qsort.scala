package sortings

object Qsort {
  // just an example, far from optimal qsort
  def qsort(a: List[Int]): List[Int] = a match {
    case Nil => Nil
    case x :: xs =>
      val lhs = xs filter (_<x)
      val rhs = xs filter (_>=x)
      qsort(lhs) ++ (x :: qsort(rhs))
  }

  // from Scala in depth, p. 10, also not well enough
  def qsortSiD(a: List[Int]): List[Int] = a match {
    case Nil => Nil
    case x :: xs =>
      val (before, after) = xs partition ( _ < x )
      qsort(before) ++ (x :: qsort(after))
  }
}
