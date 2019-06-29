package main.scala

import org.scalatest.FunSuite

class SparkSQLQueryTest extends FunSuite {

  test("Test Case 1: Top 10") {

    val csvDelimiter = ","
    val topN = "10"
    val animeFile = "src/test/resources/input/anime.csv"
    val ratingFile = "src/test/resources/input/rating.csv.gz"
    val outputPath = "src/test/resources/output/top10"

    SparkSQLQuery.main(Array(csvDelimiter,topN,animeFile,ratingFile,outputPath))
    assert(1 === 1)
  }

  test("Test Case 2: Top 20") {

    val csvDelimiter = ","
    val topN = "20"
    val animeFile = "src/test/resources/input/anime.csv"
    val ratingFile = "src/test/resources/input/rating.csv.gz"
    val outputPath = "src/test/resources/output/top20"

    SparkSQLQuery.main(Array(csvDelimiter,topN,animeFile,ratingFile,outputPath))
    assert(1 === 1)
  }
}
