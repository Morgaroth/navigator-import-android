import android.Keys._
import android.{ApkSigningConfig, PromptPasswordsSigningConfig, PromptStorepassSigningConfig}
import sbtbuildinfo.Plugin._

android.Plugin.androidBuild

//android.Plugin.androidBuildAar

organization := "io.github.morgaroth"

name := "navigator-import-android"

scalaVersion := "2.11.4"

minSdkVersion := "8"

platformTarget := "android-21"

proguardCache in Android ++= Seq(
  ProguardCache("org.scaloid") % "org.scaloid"
)

proguardOptions in Android ++= Seq(
  //  "-dontobfuscate",
  //  "-dontoptimize",
  "-keepattributes Signature",
  "-printseeds proguard_out/seeds.txt",
  "-printusage proguard_out/usage.txt",
  "-dontwarn scala.collection.**" // required from Scala 2.11.4
)

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "org.scaloid" %% "scaloid" % "3.6.1-10" withSources(),
  "org.scaloid" %% "scaloid-support-v4" % "3.6.1-10" withSources(),
  "io.github.morgaroth" %% "navigator-import-core" % "1.1.0" withSources(),
  "com.google.android" % "support-v4" % "r7"
)

useProguard in Android := true

buildInfoSettings

sourceGenerators in Compile <+= buildInfo


buildInfoKeys := Seq[BuildInfoKey](version, buildInfoBuildNumber)

buildInfoPackage := "io.github.morgaroth.navigator_import.android.build"

scalacOptions in Compile += "-feature"

run <<= run in Android

install <<= install in Android

apkSigningConfig in Android := Some(new ApkSigningConfig {
  override def keystore = new File("/home/mateusz/NavigatorImportKS")
  override def alias = "import-navigator"
  override def storePass = "Tisebyg1!"
  override def keyPass = Some("Tisebyg1!")
})
