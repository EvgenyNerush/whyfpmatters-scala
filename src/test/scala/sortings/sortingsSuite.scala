// For tests, run `sbt test` command from `whyfpmatters-scala` directory

package sortings

import Qsort._
import Msort._
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties

class sortingsSuite extends Properties("sortings") {
  assert( qsort( List(3,2,1) ) == List(1,2,3) )
  assert( qsort( List(5,4,2,4,2,1) ) == List(1,2,2,4,4,5) )

  assert( qsortSiD( List(3,2,1) ) == List(1,2,3) )
  assert( qsortSiD( List(5,4,2,4,2,1) ) == List(1,2,2,4,4,5) )

  assert( join( List(1,3), List(2,4) ) == List(1,2,3,4) )
  assert( split( List(1,2) ) == (List(1), List(2)) )
  assert( msort( List(3,2,1) ) == List(1,2,3) )
  assert( msort( List(5,4,2,4,2,1) ) == List(1,2,2,4,4,5) )

  // makes 100 tests for random `i` and `j`
  property("qsort") = forAll { (i: Int, j: Int) =>
    qsort(List(i,j)) == ( if (i < j) List(i,j) else List(j,i) )
  }
}
