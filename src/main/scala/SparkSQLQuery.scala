package main.scala

import main.scala.SparkUtils.{createSQLView, saveCSV}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkSQLQuery {

  def sparkSQLtopN(sparkSession:SparkSession,csvDelimiter:String,topn:String,animeFile:String,ratingFile:String): DataFrame = {

    // Load anime CSV file and create SQL view
    createSQLView(sparkSession,csvDelimiter,animeFile,"v_anime")

    // Load rating CSV file and create SQL view
    createSQLView(sparkSession,csvDelimiter,ratingFile,"v_rating")

    // Filter anime table by TV series with over 10 episodes
    sparkSession.sql(
      "SELECT anime_id, name, genre " +
        "FROM v_anime " +
        "WHERE TYPE = 'TV' " +
        "AND episodes > 10 ")
      .createOrReplaceTempView("v_anime_filter")

    // Calculate rating from rating table
    sparkSession.sql(
      "SELECT anime_id, AVG(rating) AS rating, COUNT(*) AS num_rating " +
        "FROM v_rating " +
        "GROUP BY anime_id")
      .createOrReplaceTempView("v_rating_group")

    // Determines genres for the top N most rated
    sparkSession.sql(
      "SELECT a.anime_id, a.name, a.genre, b.rating, b.num_rating " +
        "FROM v_anime_filter a, v_rating_group b " +
        "WHERE a.anime_id = b.anime_id " +
        "ORDER BY b.num_rating DESC " +
        "LIMIT " + topn )
      .createOrReplaceTempView("v_topn")

    // Convert to DF
    val resultDF = sparkSession.table("v_topn").toDF()

    // drop the temp views
    sparkSession.catalog.dropTempView("v_anime")
    sparkSession.catalog.dropTempView("v_rating")
    sparkSession.catalog.dropTempView("v_anime_filter")
    sparkSession.catalog.dropTempView("v_rating_group")
    sparkSession.catalog.dropTempView("v_topn")

    return resultDF
  }

  // main function where the action happens
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Set constants
    val appName = "SparkApp"
    val sparkMaster = "local[4]"

    // Get command line arguments
    val csvDelimiter = args(0)
    val topN = args(1)
    val animeFile = args(2)
    val ratingFile = args(3)
    var outputPath = args(4)

    println("Determines the top " + topN + " most rated TV series")
    // Use SparkSession interface
    val sparkSession = SparkSession
      .builder
      .appName(appName)
      .master(sparkMaster)
      .getOrCreate()

    // Show the result
    val resultDF = sparkSQLtopN(sparkSession,csvDelimiter,topN,animeFile,ratingFile)
    resultDF
      .select("anime_id", "genre", "rating", "num_rating")
      .show(false)

    // Save Result
    saveCSV(resultDF,"Y",csvDelimiter,outputPath)
  }
}