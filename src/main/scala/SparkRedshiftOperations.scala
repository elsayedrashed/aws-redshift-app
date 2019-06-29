package main.scala

import main.scala.SparkUtils.{createSQLView, saveCSV}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{SparkSession}
import org.apache.spark.sql.SaveMode

/**
  * The following parameters need to be passed
  * 1. AWS Access Key
  * 2. AWS Secret Access Key
  * 3. Redshift Database Name
  * 4. Redshift UserId
  * 5. Redshift Password
  * 6. Redshift Endpoint (Ex. swredshift.czac2vcs84ci.us-east-1.redshift.amazonaws.com:5439)
  */

object SparkRedshiftOperations {

  def main(args: Array[String]): Unit = {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    if (args.length < 6) {
      println("Needs 6 parameters only passed " + args.length)
      println("parameters needed - $awsAccessKey $awsSecretKey $rsDbName $rsUser $rsPassword $rsURL")
    }

    val awsAccessKey = args(0)
    val awsSecretKey = args(1)
    val rsDbName = args(2)
    val rsUser = args(3)
    val rsPassword = args(4)
    val rsEndPoint = args(5)

    val tempS3Dir = "s3a://spark-redshift-test/temp/"

    val jdbcURL = s"""jdbc:redshift://$rsEndPoint/$rsDbName?user=$rsUser&password=$rsPassword"""
    println(jdbcURL)

    // Use SparkSession interface
    val sparkSession = SparkSession
      .builder
      .appName("SparkApp")
      .master("local[4]")
      .getOrCreate()

    sparkSession.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", awsAccessKey)
    sparkSession.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", awsSecretKey)

    // Load from a table
    val supplier1DF = sparkSession.read
      .format("com.databricks.spark.redshift")
      .option("url", jdbcURL)
      .option("aws_iam_role","arn:aws:iam::397198319869:role/RedshiftS3FullAccess")
      .option("tempFormat", "CSV GZIP")
      .option("encryption", "true")
      .option("tempdir", tempS3Dir)
      .option("dbtable", "s3data.supplier_v1")
      .load()
    println(supplier1DF.count())
    supplier1DF.show(10)
    supplier1DF.printSchema()

    // Load from a query
    val supplier2Query = "SELECT * FROM s3data.supplier_v2 LIMIT 10"
    val supplier2DF = sparkSession.read
      .format("com.databricks.spark.redshift")
      .option("url", jdbcURL)
      .option("aws_iam_role","arn:aws:iam::397198319869:role/RedshiftS3FullAccess")
      .option("tempFormat", "CSV GZIP")
      .option("encryption", "true")
      .option("tempdir", tempS3Dir)
      .option("query", supplier2Query)
      .load()
    supplier2DF.show(10)

    val supplier3Query = "SELECT * FROM s3data.supplier_v3 LIMIT 10"
    val supplier3DF = sparkSession.read
      .format("com.databricks.spark.redshift")
      .option("url", jdbcURL)
      .option("aws_iam_role","arn:aws:iam::397198319869:role/RedshiftS3FullAccess")
      .option("tempFormat", "CSV GZIP")
      .option("encryption", "true")
      .option("tempdir", tempS3Dir)
      .option("query", supplier3Query)
      .load()
    supplier3DF.show(10)

    /*
     * Register 'event' table as temporary table 'myevent'
     * so that it can be queried via sqlContext.sql
     */
    supplier3DF.createOrReplaceTempView("temp_supplier_v3")

    // Save to a Redshift table from a table registered in Spark
    sparkSession.sql("SELECT * FROM temp_supplier_v3").withColumnRenamed("s_name", "s_supplier_name")
      .write.format("com.databricks.spark.redshift")
      .option("url", jdbcURL)
      .option("aws_iam_role","arn:aws:iam::397198319869:role/RedshiftS3FullAccess")
      .option("tempFormat", "CSV GZIP")
      .option("encryption", "true")
      .option("tempdir", tempS3Dir)
      .option("dbtable", "s3data.supplier_spark_v1")
      .mode(SaveMode.Overwrite)
      .save()

    // Append to an existing table if it exists or create a new one if it does not exist
    sparkSession.sql("SELECT * FROM temp_supplier_v3").withColumnRenamed("s_name", "s_supplier_name")
      .write.format("com.databricks.spark.redshift")
      .option("url", jdbcURL)
      .option("aws_iam_role","arn:aws:iam::397198319869:role/RedshiftS3FullAccess")
      .option("tempFormat", "CSV GZIP")
      .option("encryption", "true")
      .option("tempdir", tempS3Dir)
      .option("dbtable", "s3data.supplier_spark_v1")
      .mode(SaveMode.Append)
      .save()

    /** Demonstration of interoperability */
    val spark1Query = "SELECT s_suppkey AS s_supplier_key FROM s3data.supplier_spark_v1"

    val spark1DF = sparkSession.read
      .format("com.databricks.spark.redshift")
      .option("url", jdbcURL)
      .option("aws_iam_role","arn:aws:iam::397198319869:role/RedshiftS3FullAccess")
      .option("tempFormat", "CSV GZIP")
      .option("encryption", "true")
      .option("tempdir", tempS3Dir)
      .option("query", spark1Query)
      .load()

    spark1DF.createOrReplaceTempView("temp_supplier_spark_v1")

    /*
     * Join two DataFrame instances. Each could be sourced from any
     * compatible Data Source
     */
    val spark2DF = spark1DF.join(supplier3DF, spark1DF("s_supplier_key") === supplier3DF("s_suppkey"))
      .select("s_supplier_key", "s_name", "s_address", "s_nationkey")

    spark2DF.createOrReplaceTempView("temp_supplier_spark_v2")

    sparkSession.sql("SELECT * FROM temp_supplier_spark_v2")
      .write.format("com.databricks.spark.redshift")
      .option("url", jdbcURL)
      .option("aws_iam_role","arn:aws:iam::397198319869:role/RedshiftS3FullAccess")
      .option("tempFormat", "CSV GZIP")
      .option("encryption", "true")
      .option("tempdir", tempS3Dir)
      .option("dbtable", "s3data.supplier_spark_v2")
      .mode(SaveMode.Overwrite)
      .save()
  }
}

