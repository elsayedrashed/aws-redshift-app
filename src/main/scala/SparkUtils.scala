package main.scala

import org.apache.spark.sql._
import java.text.SimpleDateFormat
import java.util.Calendar

object SparkUtils {

  def printCurrentTime (msg:String,nc:Integer) {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val formattedDate = dateFormat.format(Calendar.getInstance().getTime())
    println(msg.padTo(nc,' ')+ " : " + formattedDate)

  }

  def loadCSV(sparkSession:SparkSession,csvDelimiter:String,filename:String): DataFrame = {

    var dataDF = sparkSession.read
      .format("com.databricks.spark.csv")
      .option("header", "true") //reading the headers
      .option("delimiter",csvDelimiter)
      .option("quote","\"")
      .option("mode", "DROPMALFORMED")
      .load(filename)

    return dataDF
  }

  def saveCSV(dataDF:DataFrame,singleFile:String,csvDelimiter:String,outputPath:String) {

    if (singleFile.equalsIgnoreCase("S")) {
      dataDF
        .coalesce(1)
        .write
        .mode("Overwrite")
        .format("com.databricks.spark.csv")
        .option("header", "true")
        .option("delimiter",csvDelimiter)
        .option("quote","\"")
        .option("quoteAll","true")
        .save(outputPath)
    } else {
      dataDF
        .write
        .mode("Overwrite")
        .format("com.databricks.spark.csv")
        .option("header", "true")
        .option("delimiter",csvDelimiter)
        .option("quote","\"")
        .option("quoteAll","true")
        .save(outputPath)
    }
  }

  def createSQLView(sparkSession:SparkSession,csvDelimiter:String,filename:String,viewname:String) {

    // Load CSV file
    loadCSV(sparkSession,csvDelimiter,filename)
      // Creates a temporary view using DataFrame
      .createOrReplaceTempView(viewname)
  }
}