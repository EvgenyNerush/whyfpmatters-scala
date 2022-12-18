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

// Thus, `flatMap` makes work with _context_ easy. The context in the example is that there can
// be no value. A lot of types also provide context. For instance, for `List` the context is
// that there are multiple values. Thus, `flatMap` for lists is written such that it
// works as follows:
val xs = List(1,2,3)
def twice(x: Int) = List(x, x)
xs.map(twice) // [[1,1], [2,2], [3,3]]
xs.flatMap(twice) // [1,1,2,2,3,3]
xs.flatMap(twice).flatMap(twice) // [1,1,1,1,2,2,2,2,..]

// It is not the all `flatMap` magic. The next trick is that it can pick elements from one
// context and the function to the other context. The context of the function is just eliminated,
// or flattened. Namely, for monad M[A] and function f: A => N[B] the function `flatMap(f)` returns
// M[B], and N is "flattened". Roughly speaking,
// M[A].flatMap{A => N[B]} is of type M[B].
val ys = List(1,2)
def somePi(x: Int): Option[Double] = Some(3.14 * x.toDouble)
ys.flatMap(somePi) // [3.14, 6.28]

// Thus, flatMap(f) drops the context of f result
val zs = List(44, 45, 46)
zs.map(response) // [Some(45), None, Some(47)]
zs.flatMap(response) // [45, 47]

// It's easy to make the `flatten` function combining `flatMap` and identical
// function (_), which returns its argument.
List(List(1,2), List(3,4)).flatMap { x: List[Int] => x } // [1,2,3,4]

// But how it can work? The context is not just a wrapper of a value which
// can be extracted.



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