package com.fongmi.android.tv.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for repository bindings.
 * Repositories will be added here as we migrate each feature.
 *
 * Example:
 * ```
 * @Binds
 * @Singleton
 * abstract fun bindVodRepository(impl: VodRepositoryImpl): VodRepository
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // TODO: Add repository bindings as features are migrated
    // Example bindings:
    // @Binds
    // @Singleton
    // abstract fun bindVodRepository(impl: VodRepositoryImpl): VodRepository
    //
    // @Binds
    // @Singleton
    // abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository
    //
    // @Binds
    // @Singleton
    // abstract fun bindLiveRepository(impl: LiveRepositoryImpl): LiveRepository
}
