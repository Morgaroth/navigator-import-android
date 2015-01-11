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
  "-dontobfuscate",
  "-keepattributes Signature,EnclosingMethod,InnerClasses",
  "-printseeds seeds.txt",
  "-keepattributes *Annotation*",
  "-printusage usage.txt",
  "-keep class spray.**",
//  "-keep class scala.refle**",
  "-keepclassmembers class io.github.** {*;}",
  "-keepclassmembers class scala.concurrent.impl.Future {*;}",
  "-keep class spray.** { *; }",
  "-dontwarn scala.collection.**", // required from Scala 2.11.4
  "-keepclassmembers class io.github.morgaroth.navigator_import.** implements android.os.Parcelable {public static final android.os.Parcelable$Creator *;}",
  "-dontnote spray.json.**", // actually
  "-dontnote org.scaloid.**", // actually
  "-dontnote io.github.morgaroth.**" // actually
)

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "org.scaloid" %% "scaloid" % "3.6.1-10" withSources(),
  "org.scaloid" %% "scaloid-support-v4" % "3.6.1-10" withSources(),
  "io.github.morgaroth" %% "navigator-import-core" % "1.1.1" withSources(),
  "io.spray" %% "spray-json" % "1.3.1" withSources(),
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
