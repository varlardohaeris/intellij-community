// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.util.indexing.roots

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ContentIterator
import com.intellij.openapi.roots.OrderRootType
import com.intellij.util.containers.ConcurrentBitSet
import com.intellij.util.indexing.IndexingBundle

internal class SdkIndexableFilesProvider(val sdk: Sdk) : IndexableFilesProvider {
  override fun getPresentableName() = IndexingBundle.message("indexable.files.provider.sdk.name", sdk.name)

  override fun iterateFiles(project: Project, fileIterator: ContentIterator, visitedFileSet: ConcurrentBitSet): Boolean {
    val roots = runReadAction {
      val rootProvider = sdk.rootProvider
      rootProvider.getFiles(OrderRootType.SOURCES).toList() + rootProvider.getFiles(OrderRootType.CLASSES)
    }
    return IndexableFilesIterationMethods.iterateNonExcludedRoots(project, roots, fileIterator, visitedFileSet)
  }
}