import android.Keys._
import android.{ApkSigningConfig, PromptPasswordsSigningConfig, PromptStorepassSigningConfig}
import sbtbuildinfo.Plugin._

android.Plugin.androidBuild

//android.Plugin.androidBuildAar

organization := "io.github.morgaroth"

name := "navigator-import-android"

scalaVersion := "2.11.5"

minSdkVersion := "8"

platformTarget := "android-21"

proguardCache in Android ++= Seq(
  ProguardCache("org.scaloid") % "org.scaloid"
)

proguardOptions in Android ++= Seq(
  "-dontobfuscate"
  //  ,"-dontpreverify"
  , "-dontoptimize"
  //  ,"-whyareyoukeeping class io.**"
  //  ,"-whyareyoukeeping class scala.**"
  //  ,"-whyareyoukeeping class spray.**"
  , "-printusage usage.txt"
  , "-printseeds seeds.txt"
  , "-keepattributes Signature,EnclosingMethod,InnerClasses"
  , "-keepattributes *Annotation*"
  //  ,"-keep class scala.reflect.api.Mirror"
  //  ,"-keep class scala.reflect.**"
  //  ,"-keep class scala.reflect.ScalaSignature"
  //  ,"-keep class scala.Option {*;}"
  //  ,"-keep class scala.collection.immutable.List {*;}"
  //  , "-keep class spray.** { *; }"
  //  ,"-keep public class io.github.morgaroth.navigator_import.core.global.** {*;}"
  , "-keep class io.github.morgaroth.navigator_import.core.modelsConversions** {*;}"
  , "-keep class io.github.morgaroth.navigator_import.android.** {*;}"
  //  ,"-keep class io.github.morgaroth.navigator_import.core.global.Waypoint {*;}"
  //  ,"-keepclassmembers class io.github.morgaroth.navigator_import.core.global.Route {*;}"
  //  ,"-keepclassmembers class io.github.** {*;}"
  //  ,"-keepclassmembers class io.github.morgaroth.navigator_import.core.global.Route {*;}"
  //  ,"-keepclassmembers class scala.concurrent.impl.Future {*;}"
  , "-keepclassmembers class io.github.morgaroth.navigator_import.** implements android.os.Parcelable {public static final android.os.Parcelable$Creator *;}"
  //  ,"-keepclassmembers class * { ** MODULE$; }"
  , "-dontwarn scala.collection.**" // required from Scala 2.11.4
  , "-dontnote scala.collection.**" // required from Scala 2.11.4
  , "-dontnote scala.runtime.**"
  , "-dontnote spray.json.**" // actually
  , "-dontnote org.scaloid.**" // actually "
  //  ,"-dontnote io.github.morgaroth.**" // actually
)

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "org.scaloid" %% "scaloid" % "3.6.1-10" withSources(),
  "org.scaloid" %% "scaloid-support-v4" % "3.6.1-10" withSources(),
  "io.github.morgaroth" %% "navigator-import-core" % "1.2.2-SNAPSHOT" withSources(),
  "io.spray" %% "spray-json" % "1.3.1" withSources(),
  "com.google.android" % "support-v4" % "r7"
)

useProguard in Android := true

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](version, buildInfoBuildNumber)

buildInfoPackage := "io.github.morgaroth.navigator_import.android.build"

scalacOptions in Compile += "-feature"

javacOptions in Compile ++= Seq("-source", "1.7", "-target", "1.7")

run <<= run in Android

install <<= install in Android

// like
//Some(new ApkSigningConfig {
//  override def keystore = new File("keystore/path")
//  override def alias = "key-alias in keystore"
//  override def storePass = "store-password"
//  override def keyPass = Some("key password")
//})
apkSigningConfig in Android := SigningConfig.signing
