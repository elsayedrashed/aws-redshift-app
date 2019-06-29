package main.scala

import org.scalatest.FunSuite

class SparkRedshiftOperationsTest extends FunSuite {

  test("Test Case 1: s3data.supplier_v1") {

    val awsAccessKey = "AKIAIZA7ZQRAYFL5HDPA"
    val awsSecretKey = "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"
    val rsDbName = "redshiftdb"
    val rsUser = "masteruser"
    val rsPassword = "Password123"
    val rsEndPoint = "redshiftcluster.ckthjk7zdncb.ap-southeast-2.redshift.amazonaws.com:5439"

    SparkRedshiftOperations.main(Array(awsAccessKey,awsSecretKey,rsDbName,rsUser,rsPassword,rsEndPoint))
    assert(1 === 1)
  }
}