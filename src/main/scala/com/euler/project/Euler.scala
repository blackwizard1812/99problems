package com.euler.project

import com.example._99problems.ArithmeticProblems.{PrimeFactorContext, isPrime}

import scala.annotation.tailrec
import scala.collection.mutable


object Euler {

  def fibonacci: Stream[Int] = {
    def nextFibonacci(n2: Int, n1: Int): Stream[Int] = {
      val n = n2 + n1
      n #:: nextFibonacci(n1, n)
    }
    0 #:: 1 #:: nextFibonacci(0,1)
  }

  def evenFibonacciSum(until: Int): Long = {
    fibonacci
      .filter(_ % 2 == 0)
      .takeWhile(_ < until)
      .sum
  }


  def primesUntil(n: Long): Stream[Long] = Stream.from(2).filter(isPrime).takeWhile(_< n).map(_.toLong)

  def primeFactors(n: Long): List[Long] = {
    val primes = primesUntil(n.toLong).toList
    @tailrec def findPrimeFactors(n: Long, acc: List[Long] = Nil): List[Long] = {
      if (n < 2) acc
      else {
        primes.find(n % _ == 0) match {
          case None => acc
          case Some(p) => findPrimeFactors(n/p, acc :+ p)
        }
      }
    }
    findPrimeFactors(n)
  }

  def largestPrimeFactor(n: Long): Long = {
    primeFactors(n).last
  }

  def isPalindrome(n: Int): Boolean = {
    n.toString == n.toString.reverse
  }

  def largestPalindromeProduct(min: Int, max: Int): (Int, Int) = {
    (for {
      i <- min to max
      j <- i to max
    } yield (i, j))
      .collect { case terms@(i, j) if isPalindrome(i * j) => (i * j) -> terms }
      .maxBy { case (k, _) => k } match {
      case (_, terms) => terms
    }
  }

  def smallestMultiple(a: Int, b: Int): Int = {
    val pfc = PrimeFactorContext(b)
    (a to b)
      .filter(_ > 1)
      .flatMap(pfc.primeFactorsCardinality)
      .groupBy{ case (prime, _) => prime }
      .map{ case (prime, l) => (prime, l.map{case(_, c) => c}.max) }
      .map { case (prime, multiplicity) => math.pow(prime, multiplicity).toInt }
      .product
  }

  def sum(seq: Seq[Int]): Int = {
    seq.sum
  }

  def square(i: Int): Int = {
    math.pow(i, 2).toInt
  }

  def sumSquareDifference(min: Int, max: Int): Int = {
    val sumOfSquares = (min to max).map(square).sum
    val squareOfSum = (sum _ andThen square) (min to max)
    squareOfSum - sumOfSquares
  }

  def nthPrime(n: Int): Int = {
    Stream.from(2)
      .filter(isPrime)
      .zip(Stream.from(1))
      .collect{case(p, i) if i >= n => p}
      .head
  }

  def largestProductInSeries(series: String, digits: Int): (String, Long) = {
    Stream.from(0)
      .takeWhile(_ < series.length - digits)
      .map(startIndex => series.substring(startIndex, startIndex + digits))
      .filterNot(_.contains("0"))
      .map(s => (s, s.toCharArray.map(_.toString).map(_.toLong).product))
      .maxBy{case(_, p) => p}
  }

  def intHypothenus(a: Int, b: Int): Option[Int] = {
    Option(math.sqrt(square(a) + square(b))).collect{ case c if c.isValidInt => c.toInt}
  }

  implicit class RichInt(i: Int) {
    def **(p: Int): Int = { math.pow(i, p).toInt}
  }

  def pythagoreanTriplet(targetSum: Int): Option[Vector[Int]] = {
    (
      for {
        aa <- Stream.from(1).takeWhile(_< targetSum)
        arr <- Stream.from(aa)
          .map(b => (aa, b, targetSum - aa - b))
          .dropWhile{ case (a, b, c) => a + b < c }
          .takeWhile{ case (_, b, c) => b < c }
          .filter{ case (a,b,c) => a+b+c == targetSum && a**2+b**2 == c**2 }
          .map{ case (a,b,c) => Vector(a, b,c) }
      } yield arr
    ).headOption
  }


  def groups(terms: Int)(series: Vector[Int]): Vector[Vector[Int]] = {
      (0 to series.length - terms)
        .toVector
        .map(startIndex => series.slice(startIndex, startIndex + terms))
  }

  def transpose(grid: Vector[Vector[Int]]): Vector[Vector[Int]] = {
    grid.head.indices.toVector.map {
      col => grid.indices.toVector.map {
        row => grid(row)(col)
      }
    }
  }

  case class Point(x: Int, y: Int)

  def incrementedDiagonal(xss: Vector[Vector[Int]], start: Point): Vector[Int] = {
    @tailrec def inner(p: Point, acc: List[Int]): List[Int] = {
      p match {
        case Point(i, j) if j >= xss.length -start.y || i >= xss.length -start.x  => acc
        case Point(i, j) => inner(Point(i+1, j+1), acc :+ xss(j+start.y)(i+start.x))
      }
    }
    inner(Point(0,0), Nil).toVector
  }


  def decrementedYDiagonal(xss: Vector[Vector[Int]], start: Point): Vector[Int] = {
    @tailrec def inner(p: Point, acc: List[Int]): List[Int] = {
      p match {
        case Point(i, j) if start.y + j < 0 || i >= xss.length - start.x  => acc
        case Point(i, j) => inner(Point(i+1, j-1), acc :+ xss(j+start.y)(i+start.x))
      }
    }
    inner(Point(0, 0), Nil).toVector
  }

  def allIncDiag(grid: Vector[Vector[Int]]): Vector[Vector[Int]] = {
    Vector(
      grid.indices.reverse.init.map{ j => incrementedDiagonal(grid, Point(0, j)) },
      grid.indices.map { i => incrementedDiagonal(grid, Point(i, 0)) }
    ).flatten
  }

  def allDecDiag(grid: Vector[Vector[Int]]): Vector[Vector[Int]] = {
    Vector(
      grid.indices.map { j => decrementedYDiagonal(grid, Point(0, j)) },
      grid.indices.tail.map { i => decrementedYDiagonal(grid, Point(i, grid.length - 1))}
    ).flatten
  }

  def productInGrid(grid: Vector[Vector[Int]], terms: Int): (Vector[Int], Int) = {
    Vector(
      grid,
      transpose(grid),
      allIncDiag(grid),
      allDecDiag(grid)
    ).flatten
      .filter(_.length >= terms)
      .flatMap(groups(terms))
      .map(v => (v, v.product))
      .maxBy{case(_, p) => p }
  }



  def triangularNumber(n: Int): Int = {
    n * (n + 1) / 2
  }

  def highlyDivisibleTriangular(minDivisorCount: Int)(implicit pfc: PrimeFactorContext): Long = {
    Stream.from(1)
      .map(triangularNumber)
      .map(t => (t, pfc.divisorCount(t)))
      .dropWhile{ case (_, d) => d < minDivisorCount }
      .take(1)
      .head._1
  }

  @tailrec def sumBigInts(bigInts: Vector[String]): String = {
    val intMatrix = bigInts
      .map(_.toCharArray.map(_.toString.toInt).toVector.reverse)

    intMatrix.head.indices.toVector
      .map { i => intMatrix.flatMap(_.lift(i)).sum }

    match {
      case sums if sums.forall(_ <= 9) => sums.reverse.mkString
      case sums => sumBigInts(intermediateSum(sums))
    }
  }

  private def intermediateSum(sums: Vector[Int]): Vector[String] = {
    val bufferSize = sums.indices.map(i => sums(i).toString.length + i).max
    sums
      .map(_.toString.toCharArray.map(_.toString.toInt).toVector.reverse)
      .zipWithIndex
      .map { case (vectorOfInt, position) =>
        val buffer = mutable.ArrayBuffer.fill(bufferSize)(0)
        vectorOfInt.indices.foreach(idx => buffer.update(idx + position, vectorOfInt(idx)))
        buffer.toVector.reverse.mkString
      }
  }
}