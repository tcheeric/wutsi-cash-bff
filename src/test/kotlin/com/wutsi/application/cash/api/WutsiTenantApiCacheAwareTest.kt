package com.wutsi.application.cash.api

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.tenant.WutsiTenantApi
import com.wutsi.platform.tenant.dto.GetTenantResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cache.Cache
import kotlin.test.assertEquals

internal class WutsiTenantApiCacheAwareTest {
    private lateinit var delegate: WutsiTenantApi
    private lateinit var cache: Cache
    private lateinit var api: WutsiTenantApi

    @BeforeEach
    fun setUp() {
        delegate = mock()
        cache = mock()

        api = WutsiTenantApiCacheAware(delegate, cache)
    }

    @Test
    fun getTenantFromCache() {
        // GIVEN
        val result = GetTenantResponse()
        doReturn(result).whenever(cache).get("tenant_1", GetTenantResponse::class.java)

        // WHEN
        val response = api.getTenant(1L)

        // THEN
        assertEquals(result, response)
        verify(delegate, never()).getTenant(1L)
    }

    @Test
    fun getTenantFromBackend() {
        // GIVEN
        doReturn(null).whenever(cache).get("tenant_1")

        val result = GetTenantResponse()
        doReturn(result).whenever(delegate).getTenant(1L)

        // WHEN
        val response = api.getTenant(1L)

        // THEN
        assertEquals(result, response)
    }
}