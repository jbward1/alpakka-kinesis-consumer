package com.ward.stream

import software.amazon.awssdk.regions.Region

object Config {
  val localStackHostname = "http://localhost:4566"
  val region: Region = Region.US_EAST_1
}
