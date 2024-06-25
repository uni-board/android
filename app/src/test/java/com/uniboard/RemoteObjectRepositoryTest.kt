package com.uniboard

import com.uniboard.data.RemoteObjectRepositoryImpl
import com.uniboard.domain.RemoteObjectRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import org.http4k.core.Response
import org.http4k.core.Status
import org.intellij.lang.annotations.Language
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@Language("JSON")
private val response = """
[
  {
    "path": "data",
    "uniboardData": {
      "id": "123",
      "type": "text"
    }
  }
]
""".trimIndent()

class RemoteObjectRepositoryTest {
    private lateinit var repository: RemoteObjectRepository

    @BeforeTest
    fun setUp() {
        repository =
            RemoteObjectRepositoryImpl(
                "f67e6113-370f-456a-9e7f-9904f9c710d2",
            ) { Response(Status.OK).body(response) }
    }

    @Test
    fun allObjectsTest(): Unit = runBlocking {
        val response = repository.allObjects().getOrThrow()
        assertEquals(response.size, 1)
        assertEquals(response.single().id, "123")
        assertEquals(response.single().type, "text")
        assertEquals(response.single().state["path"], JsonPrimitive("data"))
    }
}