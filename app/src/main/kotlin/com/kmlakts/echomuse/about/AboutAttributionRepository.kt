/*
 * Echomuse (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.kmlakts.echomuse.about

import android.content.Context
import androidx.compose.runtime.Immutable
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class AboutDependencyLicense(
    val name: String,
    val version: String?,
    val licenses: String?,
)

@Immutable
data class AboutDependencyLicenseCollection private constructor(
    private val values: List<AboutDependencyLicense>,
) {
    val isEmpty: Boolean get() = values.isEmpty()
    val size: Int get() = values.size

    operator fun get(index: Int): AboutDependencyLicense = values[index]

    companion object {
        fun from(values: List<AboutDependencyLicense>): AboutDependencyLicenseCollection = AboutDependencyLicenseCollection(values.toList())
    }
}

class FetchAboutDependencyLicensesUseCase
    @Inject
    constructor(
        private val repository: AboutAttributionRepository,
    ) {
        suspend operator fun invoke(): Result<AboutDependencyLicenseCollection> = repository.dependencyLicenses()
    }

@Singleton
class AboutAttributionRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        suspend fun dependencyLicenses(): Result<AboutDependencyLicenseCollection> =
            withContext(Dispatchers.IO) {
                try {
                    val libs =
                        Libs
                            .Builder()
                            .withContext(context)
                            .build()
                    val licenses =
                        libs.libraries
                            .map { library ->
                                AboutDependencyLicense(
                                    name = library.name.ifBlank { library.uniqueId },
                                    version = library.artifactVersion?.takeIf(String::isNotBlank),
                                    licenses =
                                        library.licenses
                                            .map { license -> license.name }
                                            .filter { license -> license.isNotBlank() }
                                            .distinct()
                                            .joinToString(separator = ", ")
                                            .takeIf(String::isNotBlank),
                                )
                            }.filter { library -> library.name.isNotBlank() }
                    val collection = AboutDependencyLicenseCollection.from(licenses)
                    if (collection.isEmpty) {
                        Result.failure(IllegalStateException("No dependency licenses found"))
                    } else {
                        Result.success(collection)
                    }
                } catch (throwable: Throwable) {
                    if (throwable is CancellationException) throw throwable
                    Result.failure(throwable)
                }
            }
    }
