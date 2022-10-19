package sortings

// just an examplt, far from optimal
object Msort {
  // (a, b) is a tuple
  def join(a: List[Int], b: List[Int]): List[Int] = (a, b) match {
    case (Nil, ys) => ys
    case (xs, Nil) => xs
    case (x :: xs, y :: ys) =>
      if (x <= y)
        x :: join(xs, y :: ys)
      else
        y :: join(x :: xs, ys)
  }

  def split(a: List[Int]): (List[Int], List[Int]) = a match {
    case Nil => (Nil, Nil)
    case x :: Nil => (x :: Nil, Nil)
    case x :: y :: xs =>
      val (vs, ws) = split(xs)
      (x :: vs, y :: ws)
  }

  def msort(a: List[Int]): List[Int] = a match {
    case Nil => Nil
    case x :: Nil => x :: Nil
    case _ =>
      val (xs, ys) = split(a)
      join(msort(xs), msort(ys))
  }
}
