import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

// Explanation of monads, as simple as possible //

// Let's start with an example: type Option[Int] (Int here marks the type that is stored in Option)
// and function `flatMap` for the Option type. `flatMap` is a part of the monadic interface.

// Monad can be defined in different ways (e.g. with bind operator, with fish operator), but
// we will define it with `flatMap` and `unit`. Scala does not provide monadic interface
// by-default, but many types provides `flatMap` function. Type constructors are used instead of
// `unit`.

// The value of type `Option` can be `Some(v)` (contains value `v`) or `None`.
val a: Option[Int] = Some(42)

// We simulate the server response to some request (which is of Int type) with Option. The response
// can be successful or not (e.g. no network), and if successful, it contains some Int:
def response(request: Int): Option[Int] = {
  if (request % 5 == 0) None // failure
  else Some(request + 1)     // success
}

response(42) // Some(43)
response(45) // None

// Say you have a function from Int to Option[Int] (as `response` does), but in the real life
// the request starts from another (previous) response, hence you rather have request of type
// Option[Int] than pure Int.
// `flatMap` function can deal exactly with this case. `flatMap` in our case consumes a function
// Int => Option[Int] and applies it to Option[Int], i.e. it allows us to make a request
// based on a value of the previous response:
Some(42).flatMap(response) // Some(43)

// Note that you don't get Some(Some(43)) here and also None values are handled naturally:
None.flatMap(response) // None

// hence with `flatMap` we can do chain requests easily:
Some(42)
  .flatMap(response) // Some(43)
  .flatMap(response) // Some(44)
  .flatMap(response) // Some(45)
  .flatMap(response) // None
  .flatMap(response) // None

// Without monadic interface one should check every time (with pattern matching or `if`) that the
// response is not None (such a wierd code for only two calls of `response`!):
val b: Option[Int] = Some(44)

b match {
  case Some(v) =>
    response(v) match {
      case Some(v) => response(v) // then match and so on
      case None => None
    }
  case None => None
}

if (b.isEmpty) {
  None
} else {
  val b1 = response(b.get)
  if (b1.isEmpty) {
    None
  } else {
    val b2 = response(b1.get) // etc.
  }
}

// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
// Thus, `flatMap` makes work with _context_ easy. The context in the example is that there can
// be no value. This is the first "face" of monads: the context remains the same,
// but the type of values in the context can change. Look:
Some("42")                                                                 // Some[String]
  .flatMap { s: String => s.toIntOption /* Some(value) if successful */ }  // Some[Int]
  .flatMap { i: Int => if (i!=0) Some(1 / i.toDouble) else None }          // Some[Double]
// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

// A lot of types also provide context. For instance, for `List` the context is
// that there are multiple values. Similarly to eliminating nested Some(Some(...))
// with `flatMap` one can flatten lists:
val xs = List(1,2,3)
def twice(x: Int) = List(x, x)
xs.map(twice)     // [[1,1], [2,2], [3,3]]
xs.flatMap(twice) // [1,1,2,2,3,3]

val ys = List(List(1,2), List(3,4)) // [[1,2], [3,4]]
ys.flatten            // [1,2,3,4]
ys.flatMap { x => x } // [1,2,3,4] (!)

// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
// Therefore, `flatMap` works as `map` followed by `flatten`.
// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

// It is not all `flatMap` magic. The next trick is that `flatMap` can pick elements from one
// context and the function to the other context. The context of the function can be somehow
// eliminated, or flattened:
val ys = List(44, 45, 46)
ys.map(response) // [Some(45), None, Some(47)]
ys.flatMap(response) // [45, 47]
//
val zs = List(1,2)
def somePi(x: Int): Option[Double] = Some(3.14 * x.toDouble)
zs.flatMap(somePi) // [3.14, 6.28]

// But, wait, how can it work for a function that returns List? How to "eliminate"
// context of multiple values? How to flatten Option[List]?
// Some(35).flatMap { x: Int => List(x, x+1) } yields the error:
// type mismatch;
// found   : List[Int]
// required: Option[?]

// Ok, it doesn't work for Option[List], so how did it worked for List(Option) then?
// The answer is that List and Option are both subtypes of IterableOnce, thus the contexts
// of both are inherited from the same trait. Now it's time to show the formal definition of a monad.

/*A monad is a parametric type M[T] with two operations: flatMap and unit.

trait M[T] {
  def flatMap[U](f: T => M[U]) : M[U]
  def unit[T](x: T) : M[T]
}

These operations must satisfy three important properties:

    Associativity: (x flatMap f) flatMap g == x flatMap (y => f(y) flatMap g)

    Left unit: unit(x) flatMap f == f(x)

    Right unit: m flatMap unit == m

Many standard Scala Objects like List, Set, Option, Gen are monads with identical implementation of flatMap and specialized implementation of unit

List(1,2).flatMap { x => Future {x} }*/

List("Ab", "Cd").flatMap(_.toLowerCase) // List('a','b','c','d')
List("1", "hi!", "2").flatMap(_.toIntOption).sum // 3
List(1,2).flatMap { x => List(x, -x) } // List(1, -1, 2, -2)
List(1 to 10).flatMap { x => if (x%2 == 1) List(x) else List() } // List(1,3,5,7,9)

val zs = List(44, 45, 46)
zs.map(response) // [Some(45), None, Some(47)]
zs.flatMap(response) // [45, 47]

// It's easy to make the `flatten` function combining `flatMap` and identical
// function (_), which returns its argument.
List(List(1,2), List(3,4)).flatMap { x: List[Int] => x } // [1,2,3,4]

// But how it can work? The context is not just a wrapper of a value which
// can be extracted.


//Namely, for monad M[A] and function f: A => N[B] the function `flatMap(f)` returns
// M[B], and N is "flattened". Roughly speaking,
// M[A].flatMap{A => N[B]} is of type M[B].
/* Formally, the polymorphic type `M` is a monad if it provides two functions:
   `unit` that maps type `X` to type `M[X]`, and
   `flatMap` that maps functions of type `X => M[Y]` to functions `M[X] => M[Y]`.
   These mappings should also satisfy so-called `monad laws` that are quite natural,
   and will be considered below (but not in detail). Instead, let's look at `flatMap`,
   the essence of monads.
 */

// The value of type `Option` can be `Some(v)` (contains value `v`) or `None`.
//val a: Option[Int] = Some(42)

// Let's consider functions of type `Int => Option[Int]`
// as some server response (which can be successful or not) to some request (Int):
/*def response(request: Int): Option[Int] = {
  if (request % 5 == 0) None // failure
  else Some(request + 1)     // success
}

response(42) // Some(43)
response(45) // None

// `flatMap` allows us to make a request based on a value of the previous response:
Some(42).flatMap(response) // Some(43)

// hence with `flatMap` we can do chain requests easily:
Some(42)
  .flatMap(response) // Some(43)
  .flatMap(response) // Some(44)
  .flatMap(response) // Some(45)
  .flatMap(response) // None
  .flatMap(response) // None */

// Without monadic interface one can do chain requests with
// a lot of `if`


//val xs = List(Some(1),Some(2),None)
//xs.flatMap { x => x }